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
package org.dubhe.tadl.machine.utils.identify;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.machine.constant.TrialEventMachineConstant;
import org.dubhe.tadl.service.TadlRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * @description 状态变更工具类
 * @date 2020-08-27
 */
@Component
public class StateMachineStatusUtil {

    @Autowired
    private TadlRedisService tadlRedisService;

    /**
     * trial运行失败状态变更
     * @param experimentId 实验id
     * @param stageId 实验阶段id
     * @param trialId trial 实验 id
     * @param statusDetail 实验失败状态详情信息
     */
    public void trialExperimentFailedState(Long experimentId,Long stageId,Long trialId,String statusDetail){
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Details of experiment failure:{}. ", experimentId, stageId,trialId,statusDetail);
        StateMachineUtil.stateChange(new StateChangeDTO(new Object[]{trialId,statusDetail},TrialEventMachineConstant.TRIAL_STATE_MACHINE,TrialEventMachineConstant.FAILED_TRIAL_EVENT));
        // 调用异步方法，删除正在运行的任务
        tadlRedisService.deleteRunningTrial(stageId);
        //删除redis缓存信息
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Prepare to delete redis cache information. ", experimentId, stageId,trialId);
        tadlRedisService.delRedisExperimentInfo(experimentId);
        //删除相关信息完成
        LogUtil.info(LogEnum.TADL, TadlConstant.PROCESS_TRIAL_KEYWORD_LOG+"Redis cache deletion completed. ", experimentId, stageId, trialId);
    }
}
