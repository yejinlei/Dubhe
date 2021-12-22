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
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnknownTrialState extends AbstractTrialState {
    @Lazy
    @Autowired
    private TrialStateMachine trialStateMachine;

    @Autowired
    private TadlTrialService trialService;

    @Autowired
    private ToRunTrialState toRunTrialState;


    @Override
    public void toRunBatchTrialEvent(List<Long> trialIds) {

        trialService.updateTrial(new LambdaUpdateWrapper<Trial>()
                .in(Trial::getId,trialIds)
                        .set(Trial::getStatus, TrialStatusEnum.TO_RUN.getVal())
                        .set(Trial::getEndTime, null)
        );
        trialStateMachine.setMemoryTrialState(toRunTrialState);
    }

    @Override
    public void toRunTrialEvent(Long trialId) {
        trialService.updateTrial(new LambdaUpdateWrapper<Trial>()
                .eq(Trial::getId,trialId)
                        .set(Trial::getStatus,TrialStatusEnum.TO_RUN.getVal())
        );
        trialStateMachine.setMemoryTrialState(toRunTrialState);
    }

    @Override
    public String currentStatus() {
        return TrialStatusEnum.UNKNOWN.getMsg();
    }
}
