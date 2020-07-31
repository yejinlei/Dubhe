/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.dao.PtTrainJobMapper;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.dto.callback.AlgorithmK8sPodCallbackCreateDTO;
import org.dubhe.dto.callback.BaseK8sPodCallbackCreateDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.service.PodCallbackAsyncService;
import org.dubhe.service.abstracts.AbstractPodCallback;
import org.dubhe.utils.K8sNameTool;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.TrainUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
        // 根据namespace和podName找到job
        QueryWrapper<PtTrainJob> queryTrainJonWrapper = new QueryWrapper<>();
        Long userId = k8sNameTool.getUserIdFromNameSpace(req.getNamespace());
        String podName = req.getPodName();
        if (null == podName || podName.length() <= MagicNumConstant.SIX) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "podName={} too short", podName);
            return false;
        }
        String k8sJobName = podName.substring(MagicNumConstant.ZERO, podName.length() - MagicNumConstant.SIX);
        queryTrainJonWrapper.eq("k8s_job_name", k8sJobName).
                eq("create_user_id", userId);
        PtTrainJob ptTrainJob = ptTrainJobMapper.selectOne(queryTrainJonWrapper);
        if (null == ptTrainJob) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "k8s_job_name={} not found", k8sJobName);
            return false;
        }
        String phase = req.getPhase();
        // 对于当前状态为结束状态或上报状态为删除的任务不做处理
        if (TrainJobStatusEnum.isEnd(ptTrainJob.getTrainStatus()) || TrainJobStatusEnum.DELETED.getMessage().equalsIgnoreCase(phase)) {
            return true;
        }
        PtTrainJob updatePtTrainJob = new PtTrainJob();

        // 更新job运行时间和状态
        updatePtTrainJob.setId(ptTrainJob.getId())
                .setTrainStatus(TrainJobStatusEnum.get(phase).getStatus());
        // 如果上报状态是结束状态并没指定过运行时间，则更新运行时间
        if (TrainJobStatusEnum.isEnd(phase) && "".equals(ptTrainJob.getRuntime())) {
            long timeDelta = System.currentTimeMillis() - ptTrainJob.getCreateTime().getTime();
            String runTime = String.format(TrainUtil.RUNTIME,
                    TimeUnit.MILLISECONDS.toHours(timeDelta),
                    TimeUnit.MILLISECONDS.toMinutes(timeDelta) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeDelta) % TimeUnit.MINUTES.toSeconds(1)
            );
            updatePtTrainJob.setRuntime(runTime);
        }
        int updateResult = ptTrainJobMapper.updateById(updatePtTrainJob);
        if (updateResult < 1) {
            LogUtil.error(LogEnum.BIZ_TRAIN, "update trainJob_id={} failed, phase={}", ptTrainJob.getId(), req.getPhase());
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
