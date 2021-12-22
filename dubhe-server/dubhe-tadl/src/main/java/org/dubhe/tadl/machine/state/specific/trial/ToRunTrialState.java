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
package org.dubhe.tadl.machine.state.specific.trial;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.machine.state.AbstractTrialState;
import org.dubhe.tadl.machine.statemachine.TrialStateMachine;
import org.dubhe.tadl.service.ExperimentService;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ToRunTrialState extends AbstractTrialState {


    @Resource
    private TrialStateMachine trialStateMachine;

    @Resource
    private FailedTrialState failedTrialState;

    @Resource
    private UnknownTrialState unknownTrialState;

    @Resource
    private WaitingTrialState waitingTrialState;

    @Resource
    private TadlTrialService trialService;

    @Resource
    private ExperimentService experimentService;

    @Override
    public void failedTrialEvent(Long trialId,String statusDetail) {
        //trial 失败
        experimentService.updateExperimentFailedByTrialId(trialId,TrialStatusEnum.FAILED.getVal(),statusDetail);
        trialStateMachine.setMemoryTrialState(failedTrialState);
    }

    @Override
    public void unknownTrialEvent(Long trialId,String statusDetail) {
        experimentService.updateExperimentFailedByTrialId(trialId,TrialStatusEnum.UNKNOWN.getVal(),statusDetail);
        trialStateMachine.setMemoryTrialState(unknownTrialState);
    }

    @Override
    public void waitingTrialEvent(Long trialId) {
        //实验状态从待运行变更成等待中，记录开始运行时间
        trialService.updateTrial(new LambdaUpdateWrapper<Trial>()
                .eq(Trial::getId,trialId)
                        .set(Trial::getStatus,TrialStatusEnum.WAITING.getVal())
        );
        trialStateMachine.setMemoryTrialState(waitingTrialState);
    }

    @Override
    public String currentStatus() {
        return TrialStatusEnum.TO_RUN.getMsg();
    }
}
