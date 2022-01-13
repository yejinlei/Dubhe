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


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.tadl.constant.RedisKeyConstant;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.dubhe.tadl.enums.TrialStatusEnum;
import org.dubhe.tadl.machine.state.AbstractTrialState;
import org.dubhe.tadl.machine.state.specific.trial.*;
import org.dubhe.tadl.service.TadlTrialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @description 数据集状态类
 * @date 2020-08-27
 */
@Data
@Component
public class TrialStateMachine extends AbstractTrialState implements Serializable {



    /**
     * 内存中的状态机
     */
    private AbstractTrialState memoryTrialState;

    @Autowired
    private TadlTrialService tadlTrialService;

    @Autowired
    private ToRunTrialState toRunTrialState;

    @Autowired
    private RunningTrialState runningTrialState;

    @Autowired
    private FailedTrialState failedTrialState;

    @Autowired
    private FinishedTrialState finishedTrialState;

    @Autowired
    private UnknownTrialState unknownTrialState;

    @Autowired
    private WaitingTrialState waitingTrialState;

    @Autowired
    private LogMonitoringApi logMonitoringApi;

    @Autowired
    private RedisUtils redisUtils;



    /**
     * 初始化状态机的状态
     *
     * @param trialId trial实验ID
     * @return Trial
     */
    public Trial initMemoryTrialState(Long trialId) {
        if(trialId ==null){
            LogUtil.error(LogEnum.TADL, "未找到trial实验详情id" + TadlConstant.EXPERIMENT_TRIAL_FLOW_LOG,trialId);
            throw new StateMachineException("未找到trial实验详情id");
        }
        Trial trial = tadlTrialService.selectOne(trialId);
        if (trial == null || trial.getStatus() == null) {
            LogUtil.error(LogEnum.TADL, "未找到trial实验数据" + TadlConstant.EXPERIMENT_TRIAL_FLOW_LOG,trialId);
            throw new StateMachineException("未找到trial实验数据");
        }
        memoryTrialState = SpringContextHolder.getBean(TrialStatusEnum.getStateMachine(trial.getStatus()));
        return trial;
    }

    /**
     * 批量初始化状态机的状态
     * @param trialIds trial 实验id集合
     * @return List<Trial>
     */
    public List<Trial> initMemoryTrialListState(List<Long> trialIds){
        if (CollectionUtils.isEmpty(trialIds)){
            LogUtil.error(LogEnum.TADL, "未找到trial实验详情id集合" + TadlConstant.EXPERIMENT_TRIAL_FLOW_LOG, StringUtils.join(trialIds,","));
            throw new StateMachineException("未找到trial实验详情id集合");
        }
        List<Trial> trials = tadlTrialService.getTrialList(new LambdaQueryWrapper<Trial>()
                .in(Trial::getId,trialIds)
        );
        Long count = trials.stream().map(Trial::getStatus).distinct().count();
        if (CollectionUtils.isEmpty(trials) || count != NumberConstant.NUMBER_1){
            LogUtil.error(LogEnum.TADL, "trial实验数据异常" + TadlConstant.EXPERIMENT_TRIAL_FLOW_LOG,StringUtils.join(trialIds,","));
            throw new StateMachineException("trial实验数据异常");
        }
        Trial trial = trials.stream().findFirst().get();
        memoryTrialState = SpringContextHolder.getBean(TrialStatusEnum.getStateMachine(trial.getStatus()));
        return trials;
    }

    @Override
    public void runningTrialEvent(Long trialId) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == runningTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验状态是运行中，不用变更." ,trial.getExperimentId(),trial.getStageId(),trial.getId());
            return;
        }
        if(memoryTrialState!=waitingTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验状态非等待中，不能变更为运行中." ,trial.getExperimentId(),trial.getStageId(),trial.getId());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "运行事件开始执行，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());

        memoryTrialState.runningTrialEvent(trialId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "运行事件执行结束，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + " is running.");
    }

    @Override
    public void finishedTrialEvent(Long trialId) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == finishedTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial 实验已经是运行成功状态，不需要进行变更" ,trial.getExperimentId(),trial.getStageId(),trial.getId());
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "已完成事件开始执行，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());

        memoryTrialState.finishedTrialEvent(trialId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "已完成事件执行结束，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + " is completed.");
    }

    @Override
    public void failedTrialEvent(Long trialId,String statusDetail) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == failedTrialState){
         LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验已经是运行失败状态，不需要进行变更" ,trial.getExperimentId(),trial.getStageId(),trial.getId());
         return;
     }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "运行失败事件开始执行，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());

        memoryTrialState.failedTrialEvent(trialId,statusDetail);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "运行失败事件执行结束，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + " failed.");
        //从zset中删除元素
        redisUtils.zRem(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, trial.getExperimentId() + RedisKeyConstant.COLON + trial.getStageId());
    }

    @Override
    public void toRunTrialEvent(Long trialId) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == toRunTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验已经是待运行状态，不需要进行变更.",trial.getExperimentId(),trial.getStageId(),trial.getId());
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + " 待运行事件开始执行，当前trial状态：{}." +trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        memoryTrialState.toRunTrialEvent(trialId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "待运行事件执行结束，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + "  waiting to run");
    }

    @Override
    public void toRunBatchTrialEvent(List<Long> trialIds){
        Trial trial = initMemoryTrialListState(trialIds).stream().findFirst().get();
        if (memoryTrialState == toRunTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验已经是待运行状态，不需要进行变更",trial.getExperimentId(),trial.getStageId(),StringUtils.join(trialIds,","));
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "批量待运行事件开始执行，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), StringUtils.join(trialIds,","),memoryTrialState.currentStatus());
        memoryTrialState.toRunBatchTrialEvent(trialIds);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "批量待运行事件执行结束，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), StringUtils.join(trialIds,","),memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"The state of unfinished trials is changed to to be run");
    }

    @Override
    public void unknownTrialEvent(Long trialId,String statusDetail) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == unknownTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验已经是未知异常状态，不需要进行变更.",trial.getExperimentId(),trial.getStageId(),trial.getId());
            return;
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "未知异常事件开始执行，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        memoryTrialState.unknownTrialEvent(trialId,statusDetail);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "异常事件执行结束，当前trial状态：{}." ,trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + " status is unknown");
        //从zset中删除元素
        redisUtils.zRem(RedisKeyConstant.EXPERIMENT_STAGE_EXPIRED_TIME_SET, trial.getExperimentId() + RedisKeyConstant.COLON + trial.getStageId());
    }

    @Override
    public void waitingTrialEvent(Long trialId) {
        Trial trial = initMemoryTrialState(trialId);
        if (memoryTrialState == waitingTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验已经是等待中状态，不需要进行变更",trial.getExperimentId(),trial.getStageId(),trial.getId());
            return;
        }
        if (memoryTrialState != toRunTrialState){
            LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"trial实验非待运行状态，不能变更状态为等待中",trial.getExperimentId(),trial.getStageId(),trial.getId());
            throw new BusinessException(TadlErrorEnum.EXPERIMENT_CHANGE_ERR_MESSAGE);
        }
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "等待运行事件开始执行，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        memoryTrialState.waitingTrialEvent(trialId);
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG + "等待运行事件执行结束，当前trial状态：{}.",trial.getExperimentId(),trial.getStageId(), trialId,memoryTrialState.currentStatus());
        logMonitoringApi.addTadlLogsToEs(trial.getExperimentId(),"Trial " + trial.getSequence() + " is waiting");
    }
}
