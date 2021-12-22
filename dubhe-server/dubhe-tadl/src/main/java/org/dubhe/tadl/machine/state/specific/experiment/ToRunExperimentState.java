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
package org.dubhe.tadl.machine.state.specific.experiment;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentState;
import org.dubhe.tadl.machine.statemachine.ExperimentStateMachine;
import org.dubhe.tadl.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class ToRunExperimentState extends AbstractExperimentState {
    @Lazy
    @Autowired
    private ExperimentStateMachine experimentStateMachine;

    @Autowired
    private ExperimentService experimentService;


    @Override
    public void runningExperimentEvent(Long experimentId) {
        experimentService.updateExperiment(new LambdaUpdateWrapper<Experiment>()
                .eq(Experiment::getId,experimentId)
                        .set(Experiment::getStatus,ExperimentStatusEnum.RUNNING_EXPERIMENT_STATE.getValue()));
        experimentStateMachine.setMemoryExperimentState(experimentStateMachine.getRunningExperimentState());
    }

    @Override
    public void waitingExperimentEvent(Long experimentId) {
        experimentService.updateExperiment(new LambdaUpdateWrapper<Experiment>()
                .eq(Experiment::getId,experimentId)
                        .set(Experiment::getStatus,ExperimentStatusEnum.WAITING_EXPERIMENT_STATE.getValue())
                        .set(Experiment::getStatusDetail, null)
                        .set(Experiment::getStartTime,new Timestamp(System.currentTimeMillis())));
        experimentStateMachine.setMemoryExperimentState(experimentStateMachine.getWaitingExperimentState());
    }

    @Override
    public String currentStatus() {
        return ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getMsg();
    }
}
