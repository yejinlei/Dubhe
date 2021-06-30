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
package org.dubhe.dcm.machine.statemachine;

import lombok.Data;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.data.machine.constant.ErrorMessageConstant;
import org.dubhe.dcm.dao.DataMedicineMapper;
import org.dubhe.dcm.domain.entity.DataMedicine;
import org.dubhe.dcm.machine.enums.DcmDataStateEnum;
import org.dubhe.dcm.machine.state.AbstractDataMedicineState;
import org.dubhe.dcm.machine.state.specific.datamedicine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @description 数据状态机
 * @date 2020-08-27
 */
@Data
@Component
public class DcmDataMedicineStateMachine extends AbstractDataMedicineState implements Serializable {

    /**
     * 未标注
     */
    @Autowired
    private NotAnnotationDcmState notAnnotationDcmState;

    /**
     * 标注中
     */
    @Autowired
    private AnnotationDataState annotationDataState;

    /**
     * 自动标注中
     */
    @Autowired
    private AutomaticLabelingDcmState automaticLabelingDcmState;

    /**
     * 自动标注完成
     */
    @Autowired
    private AutoAnnotationCompleteDcmState autoAnnotationCompleteDcmState;

    /**
     * 标注完成
     */
    @Autowired
    private AnnotationCompleteDcmState annotationCompleteDcmState;


    /**
     * 内存中的状态机
     */
    private AbstractDataMedicineState memoryDataMedicineState;

    @Autowired
    private DataMedicineMapper dataMedicineMapper;


    /**
     * 初始化状态机的状态
     *
     * @param primaryKeyId 业务ID
     * @return dataMedicine  状态机实体
     */
    public DataMedicine initMemoryDataState(Long primaryKeyId) {
        if (primaryKeyId == null) {
            throw new StateMachineException("未找到业务ID");
        }
        DataMedicine dataMedicine = dataMedicineMapper.selectById(primaryKeyId);
        if (dataMedicine == null || dataMedicine.getStatus() == null) {
            throw new StateMachineException("未找到业务数据");
        }
        memoryDataMedicineState = SpringContextHolder.getBean(DcmDataStateEnum.getStateMachine(dataMedicine.getStatus()));
        return dataMedicine;
    }

    /**
     * 初始化状态机的状态
     *
     * @param medical        医学数据集对象
     * @return dataMedicine  状态机实体
     */
    public DataMedicine initMemoryDataState(DataMedicine medical) {
        if (medical == null) {
            throw new StateMachineException("医学影像服务状态机参数对象为空");
        }
        if (medical.getStatus() == null) {
            throw new StateMachineException("未找到业务数据");
        }
        memoryDataMedicineState = SpringContextHolder.getBean(DcmDataStateEnum.getStateMachine(medical.getStatus()));
        return medical;
    }

    /**
     * 标注事件  标注中/自动标注完成/完成/未标注-->保存-->标注中
     *
     * @param medical 医学数据集对象
     */
    @Override
    public void annotationSaveEvent(DataMedicine medical) {
        initMemoryDataState(medical);
        if (memoryDataMedicineState != notAnnotationDcmState &&
                memoryDataMedicineState != annotationDataState &&
                memoryDataMedicineState != autoAnnotationCompleteDcmState &&
                memoryDataMedicineState != annotationCompleteDcmState
        ) {

            throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        memoryDataMedicineState.annotationSaveEvent(medical);
    }
    /**
     * 标注事件  未标注-->自动标注-->自动标注中
     *
     * @param medical 业务对象
     */
    @Override
    public void autoAnnotationSaveEvent(DataMedicine medical) {
        initMemoryDataState(medical);
        if (memoryDataMedicineState != notAnnotationDcmState) {

            throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        memoryDataMedicineState.autoAnnotationSaveEvent(medical);
    }

    /**
     * 标注事件 注中/自动标注完成/完成/未标注-->完成-->标注完成
     *
     * @param medical 业务对象
     */
    @Override
    public void annotationCompleteEvent(DataMedicine medical) {
        initMemoryDataState(medical.getId());
        if (memoryDataMedicineState != notAnnotationDcmState &&
                memoryDataMedicineState != annotationDataState &&
                memoryDataMedicineState != autoAnnotationCompleteDcmState &&
                memoryDataMedicineState != annotationCompleteDcmState
        ) {
            throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        memoryDataMedicineState.annotationCompleteEvent(medical);
    }

    /**
     * 标注事件 标注中-->自动标注-->自动标注中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void autoAnnotationEvent(Long primaryKeyId) {
        initMemoryDataState(primaryKeyId);
        if (memoryDataMedicineState != annotationDataState) {
            throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        memoryDataMedicineState.autoAnnotationEvent(primaryKeyId);
    }

    /**
     * 标注事件 自动标注中-->自动标注-->自动标注完成
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void autoAnnotationCompleteEvent(Long primaryKeyId) {
        initMemoryDataState(primaryKeyId);
        if (memoryDataMedicineState != automaticLabelingDcmState) {
            throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        memoryDataMedicineState.autoAnnotationCompleteEvent(primaryKeyId);
    }


}
