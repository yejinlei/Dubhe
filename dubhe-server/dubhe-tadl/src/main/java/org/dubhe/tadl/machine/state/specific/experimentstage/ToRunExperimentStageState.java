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
package org.dubhe.tadl.machine.state.specific.experimentstage;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentStageState;
import org.dubhe.tadl.machine.statemachine.ExperimentStageStateMachine;
import org.dubhe.tadl.service.ExperimentStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Objects;

@Component
public class ToRunExperimentStageState extends AbstractExperimentStageState {

    @Autowired
    private ExperimentStageService experimentStageService;

    @Autowired
    private ExperimentStageStateMachine experimentStageStateMachine;

    @Autowired
    private RunningExperimentStageState runningExperimentStageState;

    @Override
    public void runningExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = experimentStageService.selectById(stageId);
        //实验阶段开始时间，用于记录时间阶段第一次变更为运行的时间，beginTime用于记录每次变更为运行的时间
        Timestamp startTime = Objects.isNull(experimentStage.getStartTime()) ? new Timestamp(System.currentTimeMillis()):experimentStage.getStartTime();
        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>().
                eq(ExperimentStage::getId,stageId)
                        .set(ExperimentStage::getStatus,ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode())
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getStartTime,startTime)
                        .set(ExperimentStage::getBeginTime,new Timestamp(System.currentTimeMillis()))
        );
        experimentStageStateMachine.setMemoryExperimentStageState(runningExperimentStageState);
        experimentStageService.saveExpiredTimeToRedis(experimentStage.getExperimentId(),experimentStage.getId());
    }

    @Override
    public String currentStatus() {
        return ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getDescription();
    }
}
