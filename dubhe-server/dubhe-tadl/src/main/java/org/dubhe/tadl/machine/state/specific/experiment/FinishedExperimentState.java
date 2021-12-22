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

import org.dubhe.tadl.dao.ExperimentMapper;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FinishedExperimentState extends AbstractExperimentState {
    @Autowired
    private ExperimentMapper experimentMapper;


    @Override
    public void deleteExperimentInfoEvent(Long experimentId) {

        experimentMapper.updateExperimentDeletedById(experimentId,Boolean.TRUE);

    }
    @Override
    public String currentStatus() {
        return ExperimentStatusEnum.FINISHED_EXPERIMENT_STATE.getMsg();
    }


}
