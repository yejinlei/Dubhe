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
package org.dubhe.data.machine.state.specific.data;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.machine.constant.ErrorMessageConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.state.AbstractDataState;
import org.dubhe.data.machine.statemachine.DataStateMachine;
import org.dubhe.data.machine.utils.StateIdentifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @description 自动标注中状态类
 * @date 2020-08-27
 */
@Component
public class AutomaticLabelingState extends AbstractDataState {

    @Autowired
    @Lazy
    private DataStateMachine dataStateMachine;

    @Autowired
    private StateIdentifyUtil stateIdentify;

    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * 自动标注中   自动标注中-->自动标注算法-->自动标注完成
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void automaticLabelingEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAutoTagCompleteState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 自动标注中   自动标注中-->自动标注算法-->自动标注完成/手动标注中
     *
     * @param dataset 数据集详情
     */
    @Override
    public void doFinishAutoAnnotationEvent(Dataset dataset) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        DataStateEnum status = stateIdentify.getStatusForRollback(dataset.getId(),dataset.getCurrentVersionName());
        switch (status){
            case AUTO_TAG_COMPLETE_STATE:
                //自动标注完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode(),dataStateMachine.getAutoTagCompleteState());
                break;
            case MANUAL_ANNOTATION_STATE:
                //手动标注中
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.MANUAL_ANNOTATION_STATE.getCode(),dataStateMachine.getManualAnnotationState());
                break;
            default:
                throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

}
