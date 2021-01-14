/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.dao.PtJobParamMapper;
import org.dubhe.dao.PtTrainJobMapper;
import org.dubhe.domain.entity.PtJobParam;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.dto.callback.AlgorithmK8sPodCallbackCreateDTO;
import org.dubhe.dto.callback.BaseK8sPodCallbackCreateDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.enums.TrainTypeEnum;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.ContainerStatusesStateEnum;
import org.dubhe.k8s.utils.PodUtil;
import org.dubhe.service.PodCallbackAsyncService;
import org.dubhe.service.abstracts.AbstractPodCallback;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @description 训练任务回调
 * @date 2020-06-03
 */
@Service(value = "algorithmAsyncServiceImpl")
public class AlgorithmAsyncServiceImpl extends AbstractPodCallback implements PodCallbackAsyncService {

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private PodApi podApi;

    @Autowired
    private PtJobParamMapper ptJobParamMapper;

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * pod 异步回调具体实现处理类
     *
     * @param times                   第n次处理
     * @param k8sPodCallbackCreateDTO k8s回调实体类
     * @param <R>                     BaseK8sPodCallbackReq     k8s回调基类
     * @return boolean                true：处理成功    false：处理失败
     */
    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        // 强制转型
        AlgorithmK8sPodCallbackCreateDTO req = (AlgorithmK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Thread {} try {} time.Request: {}", Thread.currentThread(), times, req.toString());
        // 匹配训练任务
        PtTrainJob ptTrainJob = getPtTrainJob(req);
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "req={} not found", req);
            return false;
        }
        String phase = req.getPhase();
        if (TrainJobStatusEnum.isEnd(ptTrainJob.getTrainStatus())) {
            // 对于当前状态为结束状态的任务不做处理
            return true;
        }
        // 处理启动异常日志
        dealFailed(req,times);
        if (undoDistributeTrain(ptTrainJob, req)){
            // 不需要做回调处理的分布式训练场景
            return true;
        }
        // 如果上报状态是结束状态并没指定过运行时间，则更新运行时间
        if (TrainJobStatusEnum.isEnd(phase) && TrainUtil.INIT_RUNTIME.equals(ptTrainJob.getRuntime())) {
            //获取训练运行参数
            QueryWrapper<PtJobParam> jobParamQueryWrapper = new QueryWrapper<>();
            jobParamQueryWrapper.eq("train_job_id", ptTrainJob.getId()).last(" limit 1 ");
            PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQueryWrapper);
            if (ptJobParam == null) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "the data of table pt_job_param queried by trainJobId does not exist {}", ptTrainJob.getId());
                return false;
            }
            //判断训练是否延时启动，并更新运行时长
            long timeDelta = (ptJobParam.getDelayCreateTime() != null && ptJobParam.getDelayCreateTime().getTime() > ptTrainJob.getUpdateTime().getTime()) ? System.currentTimeMillis() - ptJobParam.getDelayCreateTime().getTime() : System.currentTimeMillis() - ptTrainJob.getUpdateTime().getTime();
            String runTime = String.format(TrainUtil.RUNTIME,
                    TimeUnit.MILLISECONDS.toHours(timeDelta),
                    TimeUnit.MILLISECONDS.toMinutes(timeDelta) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeDelta) % TimeUnit.MINUTES.toSeconds(1)
            );
            ptTrainJob.setRuntime(runTime);
        }
        // 更新job运行时间和状态
        ptTrainJob.setTrainStatus(TrainJobStatusEnum.transferStatus(phase).getStatus());
        int updateResult = ptTrainJobMapper.updateById(ptTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "update trainJob_id={} failed, phase={}", ptTrainJob.getId(), req.getPhase());
            return false;
        }
        return true;
    }

    /**
     * 记录异常情况的日志
     * @param req
     * @param times     尝试次数
     */
    private void dealFailed(AlgorithmK8sPodCallbackCreateDTO req, int times) {
        if (times != 1){
            // 仅第一次执行，避免重复产生日志
            return;
        }
        TrainJobStatusEnum trainJobStatusEnum = TrainJobStatusEnum.getByMessage(req.getPhase());
        if(TrainJobStatusEnum.FAILED != trainJobStatusEnum || StringUtils.isBlank(req.getMessages())){
            // 必须是回调FAILED且有日志才执行日志记录
            return;
        }
        // 生成资源唯一标识，避免并发调用重复执行
        String key = req.getNamespace() + "#" + req.getResourceName();
        // 线程唯一身份标识
        String uuid = UUID.randomUUID().toString();
        try {
            if (!redisUtils.getDistributedLock(key,uuid,MagicNumConstant.TEN)){
                return;
            }
            if(logMonitoringApi.searchLogByPodName(
                    0,
                    1,
                    new LogMonitoringBO(req.getNamespace(),req.getResourceName())
                ).getTotalLogs() > 0){
                // 已有失败日志，不执行
                return;
            }
            List<String> logList = new ArrayList<>(2);
            logList.add(DateUtil.getCurrentTimeStr() + ": Pod startup failure!");
            logList.add("Reason: "+ ContainerStatusesStateEnum.getStateMessage(req.getMessages()));
            logMonitoringApi.addLogsToEs(req.getPodName(), req.getNamespace(),logList);
        }finally {
            redisUtils.releaseDistributedLock(key,uuid);
        }
    }

    /**
     * 验证是否是不需要做回调处理的分布式训练场景
     *  1，RUNNING回调时有Pod还没启动成功
     *  2，非 Master Pod的回调的结束状态状态
     * @param ptTrainJob
     * @param req
     * @return true 不需要做回调处理，false，需要做回调处理
     */
    private boolean undoDistributeTrain(PtTrainJob ptTrainJob,AlgorithmK8sPodCallbackCreateDTO req){
        String phase = req.getPhase();
        if (TrainTypeEnum.isDistributeTrain(ptTrainJob.getTrainType())) {
            // 分布式训练
            if (ptTrainJob.getResourcesPoolNode() > MagicNumConstant.ONE
                    && TrainJobStatusEnum.RUNNING == TrainJobStatusEnum.getByMessage(phase)
                    && !validateDistributedRunningPod(req.getNamespace(), ptTrainJob)) {
                // 节点数大于1 且 其回调状态为RUNNING时，需要做 多节点是否都已RUNNING的判断，以保证分布式训练任务已经处于运行状态
                // 没有启动完毕,等待下次Pod回调
                return true;
            }
            if (TrainJobStatusEnum.isEnd(phase)
                    && !PodUtil.isMaster(req.getPodName())) {
                // 仅是主节点结束状态才需要更新分布式训练结束状态信息
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配训练任务
     * @param req
     * @return PtTrainJob
     */
    private PtTrainJob getPtTrainJob(AlgorithmK8sPodCallbackCreateDTO req) {
        // 根据namespace和podName找到job
        Long userId = k8sNameTool.getUserIdFromNamespace(req.getNamespace());
        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("create_user_id", userId);
        if (K8sKindEnum.DISTRIBUTETRAIN.getKind().equals(req.getPodParentType())
                || K8sKindEnum.JOB.getKind().equals(req.getPodParentType())) {
            queryTrainJonWrapper.eq("k8s_job_name", req.getPodParentName());
        } else {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Pod parent type [{}] not support in callback!", req.getPodParentType());
            return null;
        }
        return ptTrainJobMapper.selectOne(queryTrainJonWrapper);
    }


    /**
     * 验证分布式训练节点是否都已启动
     * @param namespace
     * @param ptTrainJob
     * @return true 完全启动，false 没有启动完毕
     */
    private boolean validateDistributedRunningPod(String namespace, PtTrainJob ptTrainJob) {
        List<BizPod> podList = podApi.getListByResourceName(namespace, ptTrainJob.getJobName());
        if (podList.size() != ptTrainJob.getResourcesPoolNode()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "k8s pod num ne resources pod num {}/{} ！", podList.size(), ptTrainJob.getResourcesPoolNode());
            return false;
        }
        int runningPodSize = podList.stream()
                .filter(p -> TrainJobStatusEnum.RUNNING == TrainJobStatusEnum.getByMessage(p.getPhase()))
                .collect(Collectors.toList())
                .size();
        if (runningPodSize != ptTrainJob.getResourcesPoolNode()) {
            LogUtil.warn(LogEnum.BIZ_TRAIN, "k8s running pod num {}/{} ", runningPodSize, ptTrainJob.getResourcesPoolNode());
            return false;
        }
        return true;
    }

    /**
     * pod 异步回调具体实现处理类
     *
     * @param retryTimes              总处理次数
     * @param k8sPodCallbackCreateDTO k8s回调实体类
     * @param <R>                     BaseK8sPodCallbackReq     k8s回调基类
     */
    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        // 强制转型
        AlgorithmK8sPodCallbackCreateDTO req = (AlgorithmK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Thread {}try {} times FAILED! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
    }
}
