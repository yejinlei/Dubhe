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
package org.dubhe.dcm.machine.state.specific.file;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.dcm.dao.DataMedicineFileMapper;
import org.dubhe.dcm.machine.enums.DcmFileStateEnum;
import org.dubhe.dcm.machine.state.AbstractDcmFileState;
import org.dubhe.dcm.machine.statemachine.DcmFileStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @description 自动标注完成
 * @date 2020-08-27
 */
@Component
public class AutoAnnotationCompleteDcmFileState extends AbstractDcmFileState {

    @Autowired
    @Lazy
    private DcmFileStateMachine dcmFileStateMachine;

    @Autowired
    private DataMedicineFileMapper dataMedicineFileMapper;

    /**
     * 文件 自动标注完成-->标注-->标注完成
     *
     * @param fileIds 医学数据集文件ID
     */
    @Override
    public void annotationCompleteEvent(List<Long> fileIds) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dcmFileStateMachine.getMemoryFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", fileIds);
        dataMedicineFileMapper.updateStatusByIds(fileIds, DcmFileStateEnum.ANNOTATION_COMPLETE_FILE_STATE.getCode());
        dcmFileStateMachine.setMemoryFileState(dcmFileStateMachine.getAnnotationCompleteDcmFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dcmFileStateMachine.getMemoryFileState());
    }

    /**
     * 文件事件 自动标注完成-->保存-->标注中
     *
     * @param fileIds 医学数据集文件ID
     */
    @Override
    public void annotationSaveEvent(List<Long> fileIds){
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件前内存中状态机的状态 :{} ", dcmFileStateMachine.getMemoryFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", fileIds);
        dataMedicineFileMapper.updateStatusByIds(fileIds, DcmFileStateEnum.ANNOTATION_FILE_STATE.getCode());
        dcmFileStateMachine.setMemoryFileState(dcmFileStateMachine.getAnnotationFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【自动标注完成】 执行事件后内存状态机的切换： {}", dcmFileStateMachine.getMemoryFileState());
    }

}