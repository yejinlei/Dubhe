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
package org.dubhe.tadl.machine.statemachine;

import lombok.Data;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentStageState;
import org.dubhe.tadl.machine.state.specific.experimentstage.FailedExperimentStageState;
import org.dubhe.tadl.machine.state.specific.experimentstage.FinishedExperimentStageState;
import org.dubhe.tadl.machine.state.specific.experimentstage.RunningExperimentStageState;
import org.dubhe.tadl.machine.state.specific.experimentstage.ToRunExperimentStageState;
import org.dubhe.tadl.service.ExperimentStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @description 数据状态机
 * @date 2020-08-27
 */
@Data
@Component
public class ExperimentStageStateMachine extends AbstractExperimentStageState implements Serializable {

    @Autowired
    private ExperimentStageService experimentStageService;

    @Autowired
    private RunningExperimentStageState runningExperimentStageState;

    @Autowired
    private ToRunExperimentStageState toRunExperimentStageState;

    @Autowired
    private FinishedExperimentStageState finishedExperimentStageState;

    @Autowired
    private FailedExperimentStageState failedExperimentStageState;

    /**
     * 内存中的状态机
     */
    private AbstractExperimentStageState memoryExperimentStageState;

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    /**
     * 初始化状态机的状态
     * @param stageId 实验阶段id
     * @return ExperimentStage
     */
    public ExperimentStage initMemoryExperimentStage(Long stageId){
        if (stageId ==null){
            LogUtil.error(LogEnum.TADL,  "未找到实验阶段id."+TadlConstant.EXPERIMENT_STAGE_FLOW_LOG ,stageId);
            throw new StateMachineException("未找到实验阶段id");
        }
        ExperimentStage experimentStage = experimentStageService.selectById(stageId);
        if (experimentStage == null || experimentStage.getStatus() == null){
            LogUtil.error(LogEnum.TADL, "未找到实验阶段数据."+TadlConstant.EXPERIMENT_STAGE_FLOW_LOG ,stageId);
            throw new StateMachineException("未找到实验阶段数据");
        }
        memoryExperimentStageState = SpringContextHolder.getBean(ExperimentStageStateEnum.getStateMachine(experimentStage.getStatus()));
        return experimentStage;
    }

    @Override
    public void runningExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState == runningExperimentStageState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG +"当前实验阶段状态已经是运行中，不允再进行变更状态未运行中." ,experimentStage.getExperimentId(),stageId);
            return;
        }
       if (memoryExperimentStageState!=toRunExperimentStageState){
           LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG +"当前实验阶段状态非待运行状态，不允变更为运行中." ,experimentStage.getExperimentId(),stageId);
           throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
       }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "运行事件开始执行，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        memoryExperimentStageState.runningExperimentStageEvent(stageId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "运行事件执行结束，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + " start running");
    }

    @Override
    public void finishedExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState == finishedExperimentStageState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态已经是已完成，不需要继续变更为已完成." , experimentStage.getExperimentId(),stageId);
            return;
        }
        if (memoryExperimentStageState!=runningExperimentStageState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态非运行中状态，不允变更为已完成." , experimentStage.getExperimentId(),stageId);
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "已完成事件开始执行，当前实验阶段状态：{}." +  experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());

        memoryExperimentStageState.finishedExperimentStageEvent(stageId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "已完成事件执行结束，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + " finished");

    }

    @Override
    public void failedExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState == failedExperimentStageState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态已经是运行失败，不需要继续变更为运行失败." , experimentStage.getExperimentId(),stageId);
            return;
        }
        if (memoryExperimentStageState!=runningExperimentStageState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态非运行中，不允变更为运行失败." , experimentStage.getExperimentId(),stageId);
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "失败事件开始执行，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());

        memoryExperimentStageState.failedExperimentStageEvent(stageId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "失败事件执行结束，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + " failed");

    }

    @Override
    public void toRunExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState == toRunExperimentStageState ){
           LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态已经是待运行状态，不需要继续变更为待运行." , experimentStage.getExperimentId(),stageId);
           return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "待运行事件开始执行，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        memoryExperimentStageState.toRunExperimentStageEvent(stageId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "待运行事件执行结束，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + " waiting to run");

    }

    @Override
    public void toRunBatchExperimentStageEvent(List<Long> stageIds) {
        Long stageId = stageIds.stream().findFirst().get();
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState == toRunExperimentStageState ){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态已经是待运行状态，不需要继续变更为待运行." , experimentStage.getExperimentId(),stageId);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "待运行事件开始执行，当前实验阶段状态：{}." , experimentStage.getExperimentId(), StringUtils.join(stageIds,","),memoryExperimentStageState.currentStatus());
        memoryExperimentStageState.toRunBatchExperimentStageEvent(stageIds);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "待运行事件执行结束，当前实验阶段状态：{}." , experimentStage.getExperimentId(), StringUtils.join(stageIds,","),memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + "is re-run. The state of unfinished trials is changed to to be run.");

    }

    @Override
    public void timeoutExperimentStageEvent(Long stageId) {
        ExperimentStage experimentStage = initMemoryExperimentStage(stageId);
        if (memoryExperimentStageState != runningExperimentStageState ){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG+"当前实验阶段状态非运行中，不允变执行实验阶段超时事件." , experimentStage.getExperimentId(),stageId);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "超时事件开始执行，当前实验阶段状态：{}." , experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        memoryExperimentStageState.timeoutExperimentStageEvent(stageId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_STAGE_KEYWORD_LOG + "超时事件执行结束，当前实验状态：{}." +  experimentStage.getExperimentId(),stageId,memoryExperimentStageState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentStage.getExperimentId(),"Stage " + experimentStage.getStageName() + " timed out. Change the running trial status to failed. Fail the current stage.");

    }
}
