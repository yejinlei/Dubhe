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
package org.dubhe.tadl.service;

import org.dubhe.tadl.domain.dto.ExperimentAndTrailDTO;
import org.dubhe.tadl.domain.dto.TrialK8sPodCallBackCreateDTO;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.domain.entity.Trial;


/**
 * @description redis 存储数据接口
 * @date 2021-03-05
 */
public interface TadlRedisService {

    /**
     * 通过调用接口更新数据， 继续补发任务
     *
     * @param times 回调请求次数
     * @param req 回调请求对象
     * @return boolean 返回回调结果
     */
    boolean trialCallback(int times, TrialK8sPodCallBackCreateDTO req);

    /**
     * 删除实验在redis缓存
     * @param experimentId 实验ID
     */
    void delRedisExperimentInfo(Long experimentId);

    /**
     *删除正在运行中的trial
     * @param stageId
     */
    void deleteRunningTrial(Long stageId);

    /**
     * 保存最佳精度
     *
     * @param trial
     * @param experimentStage
     */
    void saveBestData(Trial trial, ExperimentStage experimentStage);

    /**
     * 确认消息
     *
     * @param experimentId 实验ID
     * @param stageId      阶段ID
     * @param messageId    消息ID
     * @return true/false
     */
    long confirmationAckMessage(Long experimentId, Long stageId, String messageId);

    /**
     * 推送消息
     * @param experimentAndTrailDTO 实体trial消息体
     */
    void pushDataToConsumer(ExperimentAndTrailDTO experimentAndTrailDTO);
}
