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
package org.dubhe.tadl.machine.state;


import org.dubhe.tadl.domain.entity.ExperimentStage;

import java.util.List;

/**
 * @description 数据集状态类
 * @date 2020-08-27
 */
public abstract class AbstractExperimentStageState {
    /**
     *
     * 实验阶段开始运行事件
     * @param stageId 实验阶段id
     */
    public void runningExperimentStageEvent(Long stageId){};
    /**
     * 实验阶段运行成功事件
     * @param stageId 实验阶段id
     */
    public void finishedExperimentStageEvent(Long stageId){}

    /**
     * 实验阶段运行失败事件
     * @param stageId 实验阶段id
     */
    public void failedExperimentStageEvent(Long stageId){}

    /**
     * 实验阶段待运行事件
     * @param stageId 实验阶段id
     */
    public void toRunExperimentStageEvent(Long stageId){}

    /**
     * 实验阶段超时事件
     * @param stageId 实验阶段id
     */
    public void timeoutExperimentStageEvent(Long stageId){}

    /**
     * 实验阶段重启时间
     * @param stageIds
     */
    public void toRunBatchExperimentStageEvent(List<Long> stageIds){}

    /**
     * 实验阶段暂停/停止/失败时，计算出已运行的时间，公式为：
     * 当前时间 - 数据库中的update_time + 数据库中保存的暂停前已运行时间
     * @param experimentStage
     * @return
     */
    public Long getRunTime(ExperimentStage experimentStage){
        return System.currentTimeMillis() - experimentStage.getBeginTime().getTime() + experimentStage.getRunTime();
    }

    public String currentStatus(){return null;};

}
