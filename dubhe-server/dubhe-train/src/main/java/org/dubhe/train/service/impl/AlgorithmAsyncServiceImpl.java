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

package org.dubhe.train.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.abstracts.AbstractPodCallback;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.k8s.utils.PodUtil;
import org.dubhe.train.dao.PtJobParamMapper;
import org.dubhe.train.dao.PtTrainJobMapper;
import org.dubhe.train.domain.dto.callback.AlgorithmK8sPodCallbackCreateDTO;
import org.dubhe.train.domain.entity.PtJobParam;
import org.dubhe.train.domain.entity.PtTrainJob;
import org.dubhe.train.enums.TrainJobStatusEnum;
import org.dubhe.train.enums.TrainTypeEnum;
import org.dubhe.train.utils.DubheDateUtil;
import org.dubhe.train.utils.TrainUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * @description ??????????????????
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
     * pod ?????????????????????????????????
     *
     * @param times                   ???n?????????
     * @param k8sPodCallbackCreateDTO k8s???????????????
     * @param <R>                     BaseK8sPodCallbackReq     k8s????????????
     * @return boolean                true???????????????    false???????????????
     */
    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        // ????????????
        AlgorithmK8sPodCallbackCreateDTO req = (AlgorithmK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Thread {} try {} time.Request: {}", Thread.currentThread(), times, req.toString());
        // ??????????????????
        PtTrainJob ptTrainJob = getPtTrainJob(req);
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "req={} not found", req);
            return false;
        }
        String phase = req.getPhase();
        if (TrainJobStatusEnum.isEnd(ptTrainJob.getTrainStatus())) {
            // ??????????????????????????????????????????????????????
            return true;
        }
        // ?????? Pod ??????????????????
        updateStatusDetail(req, times, ptTrainJob);


        if (undoDistributeTrain(ptTrainJob, req)) {
            // ????????????????????????????????????????????????
            return true;
        }
        // ????????????????????????????????????????????????????????????????????????????????????
        if (TrainJobStatusEnum.isEnd(phase) && TrainUtil.INIT_RUNTIME.equals(ptTrainJob.getRuntime())) {
            //????????????????????????
            QueryWrapper<PtJobParam> jobParamQueryWrapper = new QueryWrapper<>();
            jobParamQueryWrapper.eq("train_job_id", ptTrainJob.getId()).last(" limit 1 ");
            PtJobParam ptJobParam = ptJobParamMapper.selectOne(jobParamQueryWrapper);
            if (ptJobParam == null) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "ptJobParam is not exist, trainJob id is {}", ptTrainJob.getId());
                return false;
            }
            //????????????????????????
            long currentTime = System.currentTimeMillis();
            long timeDelta = 0;
            if (TrainJobStatusEnum.STOP.getMessage().equalsIgnoreCase(phase)) {
                //???????????????????????????????????????????????????
                boolean delayFlag = ptJobParam.getDelayCreateTime() != null && ptJobParam.getDelayDeleteTime() != null;
                //???????????????????????????????????????
                boolean delayCreateFlag = ptJobParam.getDelayDeleteTime() == null
                        && ptJobParam.getDelayCreateTime() != null;
                //???????????????????????????????????????
                boolean delayDeleteFlag = ptJobParam.getDelayCreateTime() == null
                        && ptJobParam.getDelayDeleteTime() != null;
                if (delayFlag) {
                    timeDelta = ptJobParam.getDelayDeleteTime().getTime() - ptJobParam.getDelayCreateTime().getTime();
                } else if (delayCreateFlag) {
                    timeDelta = currentTime - ptJobParam.getDelayCreateTime().getTime();
                } else if (delayDeleteFlag) {
                    timeDelta = currentTime - ptTrainJob.getCreateTime().getTime();
                }
            } else {
                timeDelta = currentTime - ptTrainJob.getUpdateTime().getTime();
            }
            ptTrainJob.setRuntime(DubheDateUtil.secondConvertString(timeDelta));
        }
        // ??????job?????????????????????
        ptTrainJob.setTrainStatus(TrainJobStatusEnum.transferStatus(phase).getStatus());
        int updateResult = ptTrainJobMapper.updateById(ptTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Update train job failed, id is {} , phase is {}", ptTrainJob.getId(), req.getPhase());
            return false;
        }
        return true;
    }

    /**
     * ?????? Pod ??????????????????
     * @param req
     * @param times     ????????????
     * @param ptTrainJob
     */
    private void updateStatusDetail(AlgorithmK8sPodCallbackCreateDTO req, int times, PtTrainJob ptTrainJob){
        // ?????????????????????????????????????????????????????????
        String key = req.getNamespace() + "#" + req.getResourceName();
        // ????????????????????????
        String uuid = UUID.randomUUID().toString();
        try {
            if (!redisUtils.getDistributedLock(key, uuid, MagicNumConstant.TEN)) {
                return;
            }
            // ???????????????????????????
            dealFailed(req, times);
            String podName = req.getPodName();
            // ?????? Pod ?????????
            if (PodUtil.isMaster(podName)) {
                podName = "Master";
            } else if (PodUtil.isSlave(podName)) {
                int slaveIndex =  podName.indexOf("slave");
                podName = "S" + podName.substring(slaveIndex + 1, slaveIndex + 5)
                        + podName.substring(slaveIndex + 11);
            } else {
                podName = "Pod";
            }
            String statusDetailValue = ptTrainJob.getStatusDetailValue(podName);
            LogUtil.info(LogEnum.BIZ_TRAIN, "Thread {} try {} status details is {}", Thread.currentThread(), times, statusDetailValue);
            if (statusDetailValue == null && StringUtils.isEmpty(req.getMessages())){
                return;
            }
            if (StringUtils.isEmpty(req.getMessages())){
                ptTrainJob.removeStatusDetail(podName);
            }else {
                ptTrainJob.putStatusDetail(podName, req.getMessages());
            }

            // ????????????????????????????????????
            int updateResult = ptTrainJobMapper.updateById(ptTrainJob);
            if (updateResult < 1) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "Update train job failed, id is {} , phase is {}", ptTrainJob.getId(), req.getPhase());
                return;
            }
        } finally {
            redisUtils.releaseDistributedLock(key, uuid);
        }

    }

    /**
     * ???????????????????????????
     * @param req
     * @param times     ????????????
     */
    private void dealFailed(AlgorithmK8sPodCallbackCreateDTO req, int times) {
        if (times != 1) {
            // ?????????????????????????????????????????????
            return;
        }
        TrainJobStatusEnum trainJobStatusEnum = TrainJobStatusEnum.getByMessage(req.getPhase());
        if (TrainJobStatusEnum.FAILED != trainJobStatusEnum || StringUtils.isBlank(req.getMessages())) {
            // ???????????????FAILED?????????????????????????????????
            return;
        }
        if (logMonitoringApi.searchLogByPodName(
                0,
                1,
                new LogMonitoringBO(req.getNamespace(), req.getResourceName())
        ).getTotalLogs() > 0) {
            // ??????????????????????????????
            return;
        }
        List<String> logList = new ArrayList<>(2);
        logList.add(DateUtil.getCurrentTimeStr() + ": Pod startup failure!");
        logList.add("Reason: " + req.getMessages());
        logMonitoringApi.addLogsToEs(req.getPodName(), req.getNamespace(), logList);

    }



    /**
     * ???????????????????????????????????????????????????????????????
     *  1???RUNNING????????????Pod??????????????????
     *  2?????? Master Pod??????????????????????????????
     * @param ptTrainJob
     * @param req
     * @return true ???????????????????????????false????????????????????????
     */
    private boolean undoDistributeTrain(PtTrainJob ptTrainJob, AlgorithmK8sPodCallbackCreateDTO req) {
        String phase = req.getPhase();
        if (TrainTypeEnum.isDistributeTrain(ptTrainJob.getTrainType())) {
            // ???????????????
            if (ptTrainJob.getResourcesPoolNode() > MagicNumConstant.ONE
                    && TrainJobStatusEnum.RUNNING == TrainJobStatusEnum.getByMessage(phase)
                    && !validateDistributedRunningPod(req.getNamespace(), ptTrainJob)) {
                // ???????????????1 ??? ??????????????????RUNNING??????????????? ?????????????????????RUNNING??????????????????????????????????????????????????????????????????
                // ??????????????????,????????????Pod??????
                return true;
            }
            if (TrainJobStatusEnum.isEnd(phase)
                    && !PodUtil.isMaster(req.getPodName())) {
                // ???????????????????????????????????????????????????????????????????????????
                return true;
            }
        }
        return false;
    }

    /**
     * ??????????????????
     * @param req
     * @return PtTrainJob
     */
    private PtTrainJob getPtTrainJob(AlgorithmK8sPodCallbackCreateDTO req) {
        // ??????namespace???podName??????job
        Long userId = k8sNameTool.getUserIdFromNamespace(req.getNamespace());
        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        queryTrainJonWrapper.eq("create_user_id", userId);
        if (K8sKindEnum.DISTRIBUTETRAIN.getKind().equals(req.getPodParentType())
                || K8sKindEnum.JOB.getKind().equals(req.getPodParentType())) {
            queryTrainJonWrapper.eq("k8s_job_name", req.getPodParentName());
        } else {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Pod parent type ???{}??? not support in callback!", req.getPodParentType());
            return null;
        }
        return ptTrainJobMapper.selectOne(queryTrainJonWrapper);
    }


    /**
     * ?????????????????????????????????????????????
     * @param namespace
     * @param ptTrainJob
     * @return true ???????????????false ??????????????????
     */
    private boolean validateDistributedRunningPod(String namespace, PtTrainJob ptTrainJob) {
        List<BizPod> podList = podApi.getListByResourceName(namespace, ptTrainJob.getJobName());
        if (podList.size() != ptTrainJob.getResourcesPoolNode()) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "k8s pod num not equal to resources pod num, pod num is {}, resource num is  {} ???", podList.size(), ptTrainJob.getResourcesPoolNode());
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
     * pod ?????????????????????????????????
     *
     * @param retryTimes              ???????????????
     * @param k8sPodCallbackCreateDTO k8s???????????????
     * @param <R>                     BaseK8sPodCallbackReq     k8s????????????
     */
    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        // ????????????
        AlgorithmK8sPodCallbackCreateDTO req = (AlgorithmK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.BIZ_TRAIN, "Thread {} try {} times failed! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
    }

}
