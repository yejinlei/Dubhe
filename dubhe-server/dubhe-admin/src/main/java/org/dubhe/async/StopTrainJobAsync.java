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
package org.dubhe.async;

import org.dubhe.config.TrainJobConfig;
import org.dubhe.dao.PtTrainJobMapper;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.entity.PtTrainJob;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.enums.TrainTypeEnum;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * @description 停止训练任务异步处理
 * @date 2020-08-13
 */
@Component
public class StopTrainJobAsync {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Autowired
    private PodApi podApi;

    @Autowired
    private TrainJobApi trainJobApi;

    @Autowired
    private TrainJobConfig trainJobConfig;

    @Autowired
    private DistributeTrainApi distributeTrainApi;

    @Autowired
    private PtTrainJobMapper ptTrainJobMapper;

    /**
     * 停止任务
     *
     * @param currentUser 用户
     * @param jobList     任务集合
     */
    @Async("trainExecutor")
    public void stopJobs(UserDTO currentUser, List<PtTrainJob> jobList) {
        String namespace = k8sNameTool.generateNamespace(currentUser.getId());
        jobList.forEach(job -> {
            BizPod bizPod = podApi.getWithResourceName(namespace, job.getJobName());
            if (!bizPod.isSuccess()) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stops training Job return code:{},message:{}", currentUser.getUsername(), Integer.valueOf(bizPod.getCode()), bizPod.getMessage());
            }
            boolean bool = TrainTypeEnum.isDistributeTrain(job.getTrainType()) ?
                    distributeTrainApi.deleteByResourceName(namespace, job.getJobName()).isSuccess() :
                    trainJobApi.delete(namespace, job.getJobName());
            if (!bool) {
                LogUtil.error(LogEnum.BIZ_TRAIN, "User {} stops training Job and K8S fails in the stop process, namespace为{}, resourceName为{}",
                        currentUser.getUsername(), namespace, job.getJobName());
            }
            //更新训练状态
            job.setRuntime(calculateRuntime(bizPod))
                    .setTrainStatus(TrainJobStatusEnum.STOP.getStatus());
            ptTrainJobMapper.updateById(job);

        });
    }


    /**
     * 计算job训练时长
     *
     * @param bizPod   pod信息
     * @return String 训练时长
     */
    private String calculateRuntime(BizPod bizPod) {
        return calculateRuntime(bizPod, (x) -> {
        });
    }


    /**
     * 计算job训练时长
     *
     * @param bizPod
     * @param consumer  pod已经完成状态的回调函数
     * @return res      返回训练时长
     */
    private String calculateRuntime(BizPod bizPod, Consumer<String> consumer) {
        Long completedTime;
        if (StringUtils.isBlank(bizPod.getStartTime())) {
            return TrainUtil.INIT_RUNTIME;
        }
        Long startTime = transformTime(bizPod.getStartTime());
        boolean hasCompleted = StringUtils.isNotBlank(bizPod.getCompletedTime());
        completedTime = hasCompleted ? transformTime(bizPod.getCompletedTime()) : LocalDateTime.now().toEpochSecond(ZoneOffset.of(trainJobConfig.getPlusEight()));
        Long time = completedTime - startTime;
        String res = DubheDateUtil.convert2Str(time);
        if (hasCompleted) {
            consumer.accept(res);
        }
        return res;
    }


    /**
     * 时间转换
     *
     * @param  time 时间
     * @return Long 时间戳
     */
    private Long transformTime(String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        //没有根据时区做处理, 默认当前为东八区
        localDateTime = localDateTime.plusHours(Long.valueOf(trainJobConfig.getEight()));
        return localDateTime.toEpochSecond(ZoneOffset.of(trainJobConfig.getPlusEight()));
    }
}
