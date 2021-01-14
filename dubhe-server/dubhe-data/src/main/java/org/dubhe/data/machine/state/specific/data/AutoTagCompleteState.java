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

import org.dubhe.constant.ErrorMessageConstant;
import org.dubhe.data.constant.DatatypeEnum;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.state.AbstractDataState;
import org.dubhe.data.machine.statemachine.DataStateMachine;
import org.dubhe.data.machine.utils.identify.service.StateIdentify;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.exception.StateMachineException;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @description 自动标注完成类
 * @date 2020-08-27
 */
@Component
public class AutoTagCompleteState extends AbstractDataState {

    @Autowired
    @Lazy
    private DataStateMachine dataStateMachine;

    @Autowired
    private DatasetVersionFileMapper datasetVersionFileMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private StateIdentify stateIdentify;

    /**
     * 自动标注完成   自动标注完成-->调用增强算法-->增强中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void strengthenEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.STRENGTHENING_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getStrengtheningState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 自动标注完成   自动标注完成-->上传图片-->标注中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void uploadPicturesEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.MANUAL_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getManualAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 自动标注完成事件 自动标注完成-->未标注
     *
     * @param primaryKeyId 数据集Id
     */
    @Override
    public void deletePictrueNotMarkedEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 自动标注完成 标注完成-->清除标注-->未标注
     *
     * @param primaryKeyId 数据集ID
     */
    @Override
    public void deleteAnnotatingEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        Dataset dataset = datasetMapper.selectById(primaryKeyId);
        if (dataset.getDataType().equals(DatatypeEnum.IMAGE.getValue())) {
            datasetMapper.updateStatus(dataset.getId(), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
            dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
            LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
            return;
        }
        throw new BusinessException(ErrorEnum.DATASET_VIDEO_HAS_NOT_BEEN_AUTOMATICALLY_TRACKED);
    }

    /**
     * 自动标注完成 自动标注完成-->手动进行标注，点击保存-->标注中
     *
     * @param dataset 数据集详情
     */
    @Override
    public void manualAnnotationSaveEvent(Dataset dataset) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        datasetMapper.updateStatus(dataset.getId(), DataStateEnum.MANUAL_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAutomaticLabelingState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件 未标注/手动标注中/自动标注完成/目标跟踪完成-->点击完成-->标注完成
     *
     * @param dataset 数据集详情
     */
    @Override
    public void finishManualEvent(Dataset dataset){
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        DataStateEnum status = stateIdentify.getStatus(dataset.getId(),dataset.getCurrentVersionName(),true);
        switch (status){
            case AUTO_TAG_COMPLETE_STATE:
                return;
            case ANNOTATION_COMPLETE_STATE:
                //标注完成
                datasetMapper.updateStatus(dataset.getId(), DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode());
                dataStateMachine.setMemoryDataState(dataStateMachine.getAnnotationCompleteState());
                LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
                return;
            case NOT_ANNOTATION_STATE:
                //未标注
                datasetMapper.updateStatus(dataset.getId(), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
                dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
                LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
                return;
            case MANUAL_ANNOTATION_STATE:
                //手动标注中
                datasetMapper.updateStatus(dataset.getId(), DataStateEnum.MANUAL_ANNOTATION_STATE.getCode());
                dataStateMachine.setMemoryDataState(dataStateMachine.getManualAnnotationState());
                LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
                return;
            default:
                throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
    }

    /**
     * 目标跟踪中事件 自动标注完成-->算法目标跟踪-->目标跟踪中
     *
     * @param dataset 数据集对象
     */
    @Override
    public void trackEvent(Dataset dataset){
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        datasetMapper.updateStatus(dataset.getId(), DataStateEnum.TARGET_FOLLOW_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getTargetFollowState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

}
