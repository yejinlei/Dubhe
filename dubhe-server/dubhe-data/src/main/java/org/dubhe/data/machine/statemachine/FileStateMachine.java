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
package org.dubhe.data.machine.statemachine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.machine.constant.ErrorMessageConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.enums.FileStateEnum;
import org.dubhe.data.machine.state.AbstractFileState;
import org.dubhe.data.machine.state.specific.file.*;
import org.dubhe.data.machine.utils.StateIdentifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 文件状态机
 * @date 2020-08-27
 */
@Data
@Component
public class FileStateMachine extends AbstractFileState implements Serializable {

    @Autowired
    private NotAnnotationFileState notAnnotationFileState;

    @Autowired
    private AnnotationCompleteFileState annotationCompleteFileState;

    @Autowired
    private AnnotationNotDistinguishFileState annotationNotDistinguishFileState;

    @Autowired
    private ManualAnnotationFileState manualAnnotationFileState;

    @Autowired
    private AutoTagCompleteFileState autoTagCompleteFileState;

    @Autowired
    private TargetCompleteFileState targetCompleteFileState;

    /**
     * 内存中的状态机
     */
    private AbstractFileState memoryFileState;

    @Autowired
    private DatasetVersionFileMapper datasetVersionFileMapper;

    @Autowired
    private StateIdentifyUtil stateIdentify;

    /**
     * 初始化状态机的状态
     *
     * @param id 文件ID
     */
    public void initMemoryFileState(Long id) {
        if (id == null) {
            throw new StateMachineException("未找到业务ID");
        }
        DatasetVersionFile datasetVersionFile = datasetVersionFileMapper.selectById(id);
        memoryFileState = SpringContextHolder.getBean(FileStateEnum.getStateMachine(datasetVersionFile.getAnnotationStatus()));
    }

    /**
     * 初始化状态机的状态
     *
     * @param datasetId    数据集ID
     * @param fileId       文件ID
     * @param versionName  数据集版本名称
     */
    public void initMemoryFileState(Long datasetId, Long fileId, String versionName) {
        if (datasetId == null && fileId == null) {
            throw new StateMachineException("参数为空");
        }
        LambdaQueryWrapper<DatasetVersionFile> wrapper = new LambdaQueryWrapper<>();
        if ((versionName == null)) {
            wrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            wrapper.eq(DatasetVersionFile::getVersionName, versionName);
        }
        wrapper.eq(DatasetVersionFile::getFileId, fileId);
        wrapper.eq(DatasetVersionFile::getDatasetId, datasetId);
        DatasetVersionFile datasetVersionFile = datasetVersionFileMapper.selectOne(wrapper);
        memoryFileState = SpringContextHolder.getBean(FileStateEnum.getStateMachine(datasetVersionFile.getAnnotationStatus()));
    }

    /**
     * 初始化状态机的状态
     *
     * @param filesId       文件ID
     * @param datasetId     数据集ID
     * @param versionName   数据集版本名称
     */
    public void initMemoryFileListState(HashSet<Long> filesId, Long datasetId, String versionName) {
        LambdaQueryWrapper<DatasetVersionFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(versionName)) {
            wrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            wrapper.eq(DatasetVersionFile::getVersionName, versionName);
        }
        wrapper.eq(DatasetVersionFile::getDatasetId, datasetId);
        wrapper.in(DatasetVersionFile::getFileId, filesId);
        Set<Integer> collect = datasetVersionFileMapper.selectList(wrapper)
                .stream().map(DatasetVersionFile::getAnnotationStatus).collect(Collectors.toSet());
        if (collect.size() != NumberConstant.NUMBER_1) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState = SpringContextHolder.getBean(FileStateEnum.getStateMachine(collect.iterator().next()));
    }

    /**
     * 初始化状态机的状态
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     */
    public void initMemoryFileState(Long datasetId, String versionName) {
        if (datasetId == null) {
            throw new StateMachineException("参数为空");
        }
        LambdaQueryWrapper<DatasetVersionFile> wrapper = new LambdaQueryWrapper<>();
        if ((versionName == null)) {
            wrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            wrapper.eq(DatasetVersionFile::getVersionName, versionName);
        }
        DataStateEnum status = stateIdentify.getStatus(datasetId, versionName,false);
        if (status==DataStateEnum.TARGET_COMPLETE_STATE) {
            memoryFileState = SpringContextHolder.getBean(FileStateEnum.AUTO_TAG_COMPLETE_FILE_STATE.getStateMachine());
        } else {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
    }

    /**
     * 文件 未标注-->自动标注完成手动标注点击保存-->标注中
     *
     * @param datasetVersionFile 数据集版本文件
     */
    @Override
    public void manualAnnotationSaveEvent(DatasetVersionFile datasetVersionFile) {
        initMemoryFileState(datasetVersionFile.getDatasetId(), datasetVersionFile.getFileId(), datasetVersionFile.getVersionName());
        if (
                memoryFileState != notAnnotationFileState &&
                        memoryFileState != autoTagCompleteFileState &&
                        memoryFileState != annotationCompleteFileState &&
                        memoryFileState != targetCompleteFileState &&
                        memoryFileState != annotationNotDistinguishFileState&&
                        memoryFileState != manualAnnotationFileState
        ) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.manualAnnotationSaveEvent(datasetVersionFile);
    }

    /**
     * 文件 未标注-->点击完成-->标注完成
     *
     * @param datasetVersionFile 数据集版本文件
     */
    @Override
    public void saveCompleteEvent(DatasetVersionFile datasetVersionFile) {
        initMemoryFileState(datasetVersionFile.getDatasetId(), datasetVersionFile.getFileId(), datasetVersionFile.getVersionName());
        if (
                memoryFileState != notAnnotationFileState &&
                        memoryFileState != autoTagCompleteFileState &&
                        memoryFileState != manualAnnotationFileState &&
                        memoryFileState != targetCompleteFileState &&
                        memoryFileState != annotationNotDistinguishFileState&&
                        memoryFileState != annotationCompleteFileState

        ) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.saveCompleteEvent(datasetVersionFile);
    }

    /**
     * 文件 自动标注完成-->点击保存-->手动标注中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void saveAutoAnnotationEvent(Long primaryKeyId) {
        initMemoryFileState(primaryKeyId);
        if (memoryFileState != autoTagCompleteFileState) {
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.saveAutoAnnotationEvent(primaryKeyId);
    }

    /**
     * 批量自动标注完成处理事件
     *
     * @param filesId       文件ID
     * @param datasetId     数据集ID
     * @param versionName   数据集版本名称
     */
    @Override
    public void doFinishAutoAnnotationBatchEvent(HashSet<Long> filesId, Long datasetId, String versionName) {
        initMemoryFileListState(filesId, datasetId, versionName);
        if (memoryFileState != notAnnotationFileState) {
            LogUtil.error(LogEnum.BIZ_DATASET,"doFinishAutoAnnotationInfoIsEmptyBatchEvent fail"+filesId+memoryFileState);
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.doFinishAutoAnnotationBatchEvent(filesId, datasetId, versionName);
    }

    /**
     * 自动跟踪完成事件
     *
     * @param dataset 数据集对象
     */
    @Override
    public void doFinishAutoTrackEvent(Dataset dataset) {
        initMemoryFileState(dataset.getId(), dataset.getCurrentVersionName());
        memoryFileState.doFinishAutoTrackEvent(dataset);
    }

    /**
     * 文件  未标注-->自动标注完成(批量保存图片状态)-->自动标注完成未识别
     *
     * @param filesId       文件ID
     * @param datasetId     数据集ID
     * @param versionName   数据集版本名称
     */
    @Override
    public void doFinishAutoAnnotationInfoIsEmptyBatchEvent(HashSet<Long> filesId, Long datasetId, String versionName) {
        initMemoryFileListState(filesId, datasetId, versionName);
        if (memoryFileState != notAnnotationFileState) {
            LogUtil.error(LogEnum.BIZ_DATASET,"doFinishAutoAnnotationInfoIsEmptyBatchEvent fail"+filesId+memoryFileState);
            throw new StateMachineException(ErrorMessageConstant.FILE_CHANGE_ERR_MESSAGE);
        }
        memoryFileState.doFinishAutoAnnotationInfoIsEmptyBatchEvent(filesId, datasetId, versionName);
    }


}
