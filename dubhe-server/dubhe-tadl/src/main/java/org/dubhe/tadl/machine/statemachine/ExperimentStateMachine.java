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
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.enums.ExperimentStatusEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.machine.state.AbstractExperimentState;
import org.dubhe.tadl.machine.state.specific.experiment.*;
import org.dubhe.tadl.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @description 文件状态机
 * @date 2020-08-27
 */
@Data
@Component
public class ExperimentStateMachine extends AbstractExperimentState implements Serializable {


    /**
     * 内存中的状态机
     */
    private AbstractExperimentState memoryExperimentState;

    @Autowired
    private ToRunExperimentState toRunExperimentState;

    @Autowired
    private WaitingExperimentState waitingExperimentState;

    @Autowired
    private RunningExperimentState runningExperimentState;

    @Autowired
    private PausedExperimentState pausedExperimentState;

    @Autowired
    private FailedExperimentState failedExperimentState;

    @Autowired
    private FinishedExperimentState finishedExperimentState;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    /**
     * 初始化状态机的状态
     *
     * @param experimentId 实验ID
     */
    public Experiment initMemoryExperimentState(Long experimentId) {
        if (experimentId == null) {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验id不存在." ,experimentId);
            throw new StateMachineException("实验id不存在");
        }
        Experiment experiment = experimentService.selectById(experimentId);
        if (experiment == null || experiment.getStatus() == null) {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "未找到实验数据." ,experimentId);
            throw new StateMachineException("未找到实验数据");
        }
        memoryExperimentState = SpringContextHolder.getBean(ExperimentStatusEnum.getStateMachine(experiment.getStatus()));
        return experiment;
    }

    @Override
    public void runningExperimentEvent(Long experimentId) {
        initMemoryExperimentState(experimentId);
        //若已是运行中，则不做处理
        if (memoryExperimentState == runningExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "当前已经是运行中，不允再进行变更状态." ,experimentId);
            return;
        }
        if (memoryExperimentState == waitingExperimentState ||
                memoryExperimentState == toRunExperimentState ||
                memoryExperimentState == pausedExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "运行中事件开始执行，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());

            memoryExperimentState.runningExperimentEvent(experimentId);
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "运行中事件执行结束，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());
        } else {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "当前实验状态不可变更，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
    }

    @Override
    public void pausedExperimentEvent(Long experimentId) {
        initMemoryExperimentState(experimentId);
        if (memoryExperimentState == pausedExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "当前已经是暂停中，不允再进行变更状态." ,experimentId);
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "暂停中事件开始执行，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());

        memoryExperimentState.pausedExperimentEvent(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "暂停中事件执行结束，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentId,"Experiment paused");

    }

    @Override
    public void finishedExperimentEvent(Long experimentId) {
        initMemoryExperimentState(experimentId);
        if (memoryExperimentState != runningExperimentState) {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "当前实验状态不可变更，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "已完成事件开始执行，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());
        memoryExperimentState.finishedExperimentEvent(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "已完成事件执行结束，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentId,"Experiment finished");
    }

    @Override
    public void failedExperimentEvent(Long experimentId,String statusDetail) {
        initMemoryExperimentState(experimentId);
        if (memoryExperimentState == failedExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "当前已经是运行失败，不允再进行变更状态." ,experimentId);
            logMonitoringApi.addTadlLogsToEs(experimentId,"当前已经是运行失败，不允再进行变更状态.");
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "运行失败事件开始执行，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());

        memoryExperimentState.failedExperimentEvent(experimentId,statusDetail);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "运行失败事件执行结束，当前实验状态：{}.", experimentId, memoryExperimentState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentId,"Experiment failed");
    }



    @Override
    public void waitingExperimentEvent(Long experimentId) {
        initMemoryExperimentState(experimentId);
        if (memoryExperimentState == waitingExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "实验状态是等待中，不用变更为等待中." ,experimentId);
            logMonitoringApi.addTadlLogsToEs(experimentId,"实验状态是等待中，不用变更为等待中.");
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "等待中事件开始执行，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());

        memoryExperimentState.waitingExperimentEvent(experimentId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "等待中事件执行结束，当前实验状态：{}.",experimentId,memoryExperimentState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(experimentId,"The experiment is starting...");
    }

    @Override
    public void deleteExperimentInfoEvent(Long experimentId) {
        Experiment experiment = initMemoryExperimentState(experimentId);
        if ( memoryExperimentState == pausedExperimentState ||
                memoryExperimentState == failedExperimentState ||
                        memoryExperimentState == finishedExperimentState) {
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "执行删除事件开始.",experimentId);
            memoryExperimentState.deleteExperimentInfoEvent(experimentId);
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "执行删除事件成功.",experimentId);
            logMonitoringApi.addTadlLogsToEs(experimentId,"Experiment deleted");
        } else {
            LogUtil.error(LogEnum.TADL, TadlConstant.PROCESS_EXPERIMENT_FLOW_LOG + "该实验状态：{}，不支持删除."  ,experimentId,ExperimentStatusEnum.getState(experiment.getStatus()).getMsg());
            throw new BusinessException("该实验非已失败，已完成或已暂停的状态，不支持删除");
        }
    }
}
