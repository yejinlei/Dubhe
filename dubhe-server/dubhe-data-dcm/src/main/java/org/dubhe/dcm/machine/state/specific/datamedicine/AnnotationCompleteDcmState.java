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
package org.dubhe.dcm.machine.state.specific.datamedicine;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.dcm.dao.DataMedicineMapper;
import org.dubhe.dcm.domain.entity.DataMedicine;
import org.dubhe.dcm.machine.enums.DcmDataStateEnum;
import org.dubhe.dcm.machine.state.AbstractDataMedicineState;
import org.dubhe.dcm.machine.statemachine.DcmDataMedicineStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @description 标注完成状态类
 * @date 2020-08-27
 */
@Component
public class AnnotationCompleteDcmState extends AbstractDataMedicineState {


    @Autowired
    @Lazy
    private DcmDataMedicineStateMachine dcmDataMedicineStateMachine;

    @Autowired
    private DataMedicineMapper dataMedicineMapper;

    /**
     * 数据集 标注完成-->标注-->标注中
     *
     * @param medical 医学数据集对象
     */
    @Override
    public void annotationSaveEvent(DataMedicine medical) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dcmDataMedicineStateMachine.getMemoryDataMedicineState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", medical);
        dataMedicineMapper.updateStatus(medical.getId(), DcmDataStateEnum.ANNOTATION_DATA_STATE.getCode());
        dcmDataMedicineStateMachine.setMemoryDataMedicineState(dcmDataMedicineStateMachine.getAnnotationDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dcmDataMedicineStateMachine.getMemoryDataMedicineState());
    }


    /**
     * 数据集 标注完成-->完成-->标注完成
     *
     * @param medical 医学数据集对象
     */
    @Override
    public void annotationCompleteEvent(DataMedicine medical) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dcmDataMedicineStateMachine.getMemoryDataMedicineState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", medical);
        dcmDataMedicineStateMachine.setMemoryDataMedicineState(dcmDataMedicineStateMachine.getAnnotationCompleteDcmState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dcmDataMedicineStateMachine.getMemoryDataMedicineState());
    }

}