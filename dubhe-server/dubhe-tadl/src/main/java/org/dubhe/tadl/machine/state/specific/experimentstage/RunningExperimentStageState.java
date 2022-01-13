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
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.dao.ExperimentStageMapper;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentStageState;
import org.dubhe.tadl.machine.statemachine.ExperimentStageStateMachine;
import org.dubhe.tadl.service.ExperimentService;
import org.dubhe.tadl.service.ExperimentStageService;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class RunningExperimentStageState extends AbstractExperimentStageState {
    @Lazy
    @Autowired
    private ExperimentStageStateMachine experimentStageStateMachine;

    @Autowired
    private FinishedExperimentStageState finishedExperimentStageState;

    @Autowired
    private FailedExperimentStageState failedExperimentStageState;

    @Autowired
    private ToRunExperimentStageState toRunExperimentStageState;

    @Autowired
    private ExperimentStageService experimentStageService;

    @Autowired
    private ExperimentStageMapper experimentStageMapper;

    @Autowired
    private TadlTrialService tadlTrialService;

    @Autowired
    private ExperimentService experimentService;

    @Override
    public void finishedExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = experimentStageMapper.selectById(stageId);

        Long runTime = getRunTime(experimentStage);
        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>()
                .eq(ExperimentStage::getId,stageId)
                        .set(ExperimentStage::getRunTime,runTime)
                        .set(ExperimentStage::getStatus,ExperimentStageStateEnum.FINISHED_EXPERIMENT_STAGE_STATE.getCode())
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getEndTime,new Timestamp(System.currentTimeMillis())));
        experimentStageStateMachine.setMemoryExperimentStageState(finishedExperimentStageState);

        experimentStageService.removeExpiredTimeToRedis(experimentStage.getExperimentId(),experimentStage.getId());
    }

    @Override
    public void failedExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = experimentStageMapper.selectById(stageId);

        Long runTime = getRunTime(experimentStage);
        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>()
                .eq(ExperimentStage::getId,stageId)
                        .set(ExperimentStage::getRunTime,runTime)
                        .set(ExperimentStage::getStatus,ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode())
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getEndTime,new Timestamp(System.currentTimeMillis())));
        experimentStageStateMachine.setMemoryExperimentStageState(failedExperimentStageState);

        experimentStageService.removeExpiredTimeToRedis(experimentStage.getExperimentId(),experimentStage.getId());
    }

    @Override
    public void toRunExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = experimentStageMapper.selectById(stageId);

        Long runTime = getRunTime(experimentStage);
        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>()
                .eq(ExperimentStage::getId,stageId)
                        .set(ExperimentStage::getRunTime,runTime)
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getStatus,ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode()));
        experimentStageStateMachine.setMemoryExperimentStageState(toRunExperimentStageState);

        experimentStageService.pauseExpiredTimeToRedis(experimentStage.getExperimentId(),experimentStage.getId());
    }



    @Override
    public void timeoutExperimentStageEvent(Long stageId){
        ExperimentStage experimentStage = experimentStageMapper.selectById(stageId);

        Long runTime = getRunTime(experimentStage);
        //结束正在运行和正在等待中的trial
        tadlTrialService.updateTrial(new LambdaUpdateWrapper<Trial>()
                .eq(Trial::getStageId,stageId)
                        .in(Trial::getStatus, TrialStatusEnum.RUNNING.getVal(),TrialStatusEnum.WAITING.getVal())
                        .set(Trial::getStatus,TrialStatusEnum.FAILED.getVal())
                        .set(Trial::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(Trial::getEndTime,new Timestamp(System.currentTimeMillis())));
        //修改实验阶段为运行失败
        experimentStageService.updateExperimentStage(new LambdaUpdateWrapper<ExperimentStage>()
                .eq(ExperimentStage::getId,stageId)
                        .set(ExperimentStage::getRunTime,runTime)
                        .set(ExperimentStage::getStatus,ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode())
                        .set(ExperimentStage::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(ExperimentStage::getEndTime,new Timestamp(System.currentTimeMillis())));
        experimentStageStateMachine.setMemoryExperimentStageState(failedExperimentStageState);

        String statusDetail = StringUtils.putIntoJsonStringMap(TadlConstant.EXPERIMENT_RUN_FAILED,TadlConstant.STAGE_OVERTIME,null);
        //修改实验状态为运行失败
        experimentService.updateExperiment(new LambdaUpdateWrapper<Experiment>()
                .eq(Experiment::getId,experimentStage.getExperimentId())
                        .set(Experiment::getStatus,ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue())
                        .set(Experiment::getUpdateTime,new Timestamp(System.currentTimeMillis()))
                        .set(Experiment::getStatusDetail,statusDetail)
                        .set(Experiment::getEndTime,new Timestamp(System.currentTimeMillis())));


    }

    @Override
    public String currentStatus() {
        return ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getDescription();
    }
}
