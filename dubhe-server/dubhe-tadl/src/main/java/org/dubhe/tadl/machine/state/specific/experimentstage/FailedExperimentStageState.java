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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class FailedExperimentStageState extends AbstractExperimentStageState {
    @Lazy
    @Autowired
    private ExperimentStageStateMachine experimentStageStateMachine;

    @Autowired
    private ExperimentStageService experimentStageService;


    @Autowired
    private ToRunExperimentStageState toRunExperimentStageState;

    @Override
    public void toRunBatchExperimentStageEvent(List<Long> stageIds) {

        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>()
                .in(ExperimentStage::getId,stageIds)
                        .set(ExperimentStage::getEndTime, null)
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getStatus, ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode()));
        experimentStageStateMachine.setMemoryExperimentStageState(toRunExperimentStageState);

    }

    @Override
    public String currentStatus() {
        return ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getDescription();
    }
}
