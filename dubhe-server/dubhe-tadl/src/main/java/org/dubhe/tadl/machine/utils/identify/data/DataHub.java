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
package org.dubhe.tadl.machine.utils.identify.data;

import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.service.ExperimentService;
import org.dubhe.tadl.service.ExperimentStageService;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 数据查询/加工
 * @date 2020-09-24
 */
@Component
public class DataHub {

    /**
     * trial
     */
    @Autowired
    @Lazy
    private TadlTrialService trial;

    /**
     * experiment
     */
    @Autowired
    @Lazy
    private ExperimentService experiment;


    /**
     * experimentStage
     */
    @Autowired
    @Lazy
    private ExperimentStageService experimentStage;


    /**
     * 获取实验状态
     */
    public ExperimentStatusEnum getExperimentState(Long experimentId){
        return ExperimentStatusEnum.getState(experiment.selectById(experimentId).getStatus());
    }


    /**
     * 获取实验阶段set
     */
    public  List<Integer> getExperimentStateByStage(Long experimentId) {
        return experimentStage.getExperimentStateByStage(experimentId);
    }

    /**
     * 获取实验阶段状态
     */
    public ExperimentStageStateEnum getExperimentStageState(Long experimentStageId){
        return ExperimentStageStateEnum.getState(experimentStage.selectById(experimentStageId).getStatus());
    }


    /**
     * 获取 trial set
     */
    public  List<Integer> getExperimentStageStateByTrial(Long experimentStageId) {
        return trial.getExperimentStageStateByTrial(experimentStageId);
    }




}
