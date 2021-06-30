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
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.data.machine.constant.ErrorMessageConstant;
import org.dubhe.dcm.dao.DataMedicineFileMapper;
import org.dubhe.dcm.domain.entity.DataMedicineFile;
import org.dubhe.dcm.machine.enums.DcmFileStateEnum;
import org.dubhe.dcm.machine.state.AbstractDcmFileState;
import org.dubhe.dcm.machine.state.specific.file.AnnotationCompleteDcmFileState;
import org.dubhe.dcm.machine.state.specific.file.AnnotationFileState;
import org.dubhe.dcm.machine.state.specific.file.AutoAnnotationCompleteDcmFileState;
import org.dubhe.dcm.machine.state.specific.file.NotAnnotationDcmFileState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 文件状态机
 * @date 2020-08-27
 */
@Data
@Component
public class DcmFileStateMachine extends AbstractDcmFileState implements Serializable {

    @Autowired
    private NotAnnotationDcmFileState notAnnotationFileState;

    @Autowired
    private AnnotationFileState annotationFileState;

    @Autowired
    private AutoAnnotationCompleteDcmFileState autoAnnotationCompleteDcmFileState;

    @Autowired
    private AnnotationCompleteDcmFileState annotationCompleteDcmFileState;

    /**
     * 内存中的状态机
     */
    private AbstractDcmFileState memoryFileState;


    @Autowired
    private DataMedicineFileMapper dataMedicineFileMapper;


    /**
     * 初始化状态机的状态
     *
     * @param fileIds 文件ID列表
     */
    public void initMemoryFileState(List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            throw new StateMachineException("未找到文件ID");
        }
        List<DataMedicineFile> dataMedicineFileList = dataMedicineFileMapper.selectByIds(fileIds);
        Map<Integer, List<DataMedicineFile>> dataMedicineFileMap = dataMedicineFileList.stream().collect(Collectors.groupingBy(DataMedicineFile::getStatus));
        if (dataMedicineFileMap.size() > MagicNumConstant.ONE) {
            throw new StateMachineException(" 文件中存在状态不一致：【" + dataMedicineFileMap.entrySet() + "】");
        }
        memoryFileState = SpringContextHolder.getBean(DcmFileStateEnum.getStateMachine(dataMedicineFileList.get(MagicNumConstant.ZERO).getStatus()));
    }

    /**
     * 初始化状态机的状态
     *
     * @param fileIds 文件ID列表
     */
    public Map<Integer, List<DataMedicineFile>> getFileStateGroup(List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            throw new StateMachineException("未找到文件ID");
        }
        List<DataMedicineFile> dataMedicineFileList = dataMedicineFileMapper.selectByIds(fileIds);
        return dataMedicineFileList.stream().collect(Collectors.groupingBy(DataMedicineFile::getStatus));
    }

    /**
     * 文件事件 未标注-->自动标注文件-->标注中
     *
     * @param fileIds 文件ID列表
     */
    @Override
    public void annotationEvent(List<Long> fileIds) {
        initMemoryFileState(fileIds);
        if (memoryFileState != notAnnotationFileState) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.annotationEvent(fileIds);
    }

    /**
     * 文件事件 未标注-->自动标注文件-->标注中
     *
     * @param fileIds 文件ID列表
     */
    @Override
    public void autoAnnotationSaveEvent(List<Long> fileIds) {
        initMemoryFileState(fileIds);
        if (memoryFileState != notAnnotationFileState) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.autoAnnotationSaveEvent(fileIds);
    }

    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->保存-->标注中
     *
     * @param fileIds 医学数据集文件ID
     */
    @Override
    public void annotationSaveEvent(List<Long> fileIds) {
        getFileStateGroup(fileIds).keySet().forEach(k -> {
            memoryFileState = SpringContextHolder.getBean(DcmFileStateEnum.getStateMachine(k));
            if (memoryFileState != notAnnotationFileState &&
                    memoryFileState != annotationFileState &&
                    memoryFileState != autoAnnotationCompleteDcmFileState &&
                    memoryFileState != annotationCompleteDcmFileState
            ) {
                throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
            }
        });
        getFileStateGroup(fileIds).keySet().forEach(k -> {
            memoryFileState = SpringContextHolder.getBean(DcmFileStateEnum.getStateMachine(k));
            memoryFileState.annotationSaveEvent(fileIds);
        });
    }


    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->完成-->标注完成
     *
     * @param fileIds 医学数据集文件ID
     */
    @Override
    public void annotationCompleteEvent(List<Long> fileIds) {
        getFileStateGroup(fileIds).keySet().forEach(k -> {
            memoryFileState = SpringContextHolder.getBean(DcmFileStateEnum.getStateMachine(k));
            if (memoryFileState != notAnnotationFileState &&
                    memoryFileState != annotationFileState &&
                    memoryFileState != autoAnnotationCompleteDcmFileState &&
                    memoryFileState != annotationCompleteDcmFileState
            ) {
                throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
            }
        });
        getFileStateGroup(fileIds).keySet().forEach(k -> {
            memoryFileState = SpringContextHolder.getBean(DcmFileStateEnum.getStateMachine(k));
            memoryFileState.annotationCompleteEvent(fileIds);
        });
    }

    /**
     * 文件事件 标注中-->自动标注文件-->自动标注完成
     *
     * @param fileIds 文件ID列表
     */
    @Override
    public void autoAnnotationEvent(List<Long> fileIds) {
        initMemoryFileState(fileIds);
        if (memoryFileState != notAnnotationFileState) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.autoAnnotationEvent(fileIds);
    }


}
