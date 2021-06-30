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
package org.dubhe.data.machine.state.specific.file;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.machine.enums.FileStateEnum;
import org.dubhe.data.machine.state.AbstractFileState;
import org.dubhe.data.machine.statemachine.FileStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * @description 未标注状态类
 * @date 2020-08-27
 */
@Component
public class NotAnnotationFileState extends AbstractFileState {

    @Autowired
    private FileStateMachine fileStateMachine;

    @Autowired
    private DatasetVersionFileMapper datasetVersionFileMapper;

    /**
     * 文件 未标注-->手动标注文件点击保存-->标注中
     *
     * @param datasetVersionFile 数据集版本文件详情
     */
    @Override
    public void manualAnnotationSaveEvent(DatasetVersionFile datasetVersionFile) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 :{} ", fileStateMachine.getMemoryFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", datasetVersionFile.toString());
        LambdaUpdateWrapper<DatasetVersionFile> updatawrapper = new LambdaUpdateWrapper<>();
        if ((datasetVersionFile.getVersionName() == null)) {
            updatawrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            updatawrapper.eq(DatasetVersionFile::getVersionName, datasetVersionFile.getVersionName());
        }
        updatawrapper.eq(DatasetVersionFile::getDatasetId, datasetVersionFile.getDatasetId());
        updatawrapper.eq(DatasetVersionFile::getFileId, datasetVersionFile.getFileId());
        datasetVersionFileMapper.update(new DatasetVersionFile() {{
                                            setAnnotationStatus(FileStateEnum.MANUAL_ANNOTATION_FILE_STATE.getCode());
                                            if (datasetVersionFile.getVersionName() != null) {
                                                setChanged(Constant.CHANGED);
                                            }
                                        }},
                updatawrapper);
        fileStateMachine.setMemoryFileState(fileStateMachine.getManualAnnotationFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", fileStateMachine.getMemoryFileState());
    }

    /**
     * 文件 未标注-->点击完成-->标注完成
     *
     * @param datasetVersionFile 数据集版本文件详情
     */
    @Override
    public void saveCompleteEvent(DatasetVersionFile datasetVersionFile) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 : {} ", fileStateMachine.getMemoryFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", datasetVersionFile);
        LambdaUpdateWrapper<DatasetVersionFile> updatawrapper = new LambdaUpdateWrapper<>();
        if ((datasetVersionFile.getVersionName() == null)) {
            updatawrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            updatawrapper.eq(DatasetVersionFile::getVersionName, datasetVersionFile.getVersionName());
        }
        updatawrapper.eq(DatasetVersionFile::getDatasetId, datasetVersionFile.getDatasetId());
        updatawrapper.eq(DatasetVersionFile::getFileId, datasetVersionFile.getFileId());
        datasetVersionFileMapper.update(new DatasetVersionFile() {{
                                            setAnnotationStatus(FileStateEnum.ANNOTATION_COMPLETE_FILE_STATE.getCode());
                                            if (datasetVersionFile.getVersionName() != null) {
                                                setChanged(Constant.CHANGED);
                                            }
                                        }},
                updatawrapper);
        fileStateMachine.setMemoryFileState(fileStateMachine.getAnnotationCompleteFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", fileStateMachine.getMemoryFileState());
    }

    /**
     * 自动标注完成批量处理事件
     *
     * @param filesId       文件ID
     * @param datasetId     数据集ID
     * @param versionName   版本名称
     */
    @Override
    public void doFinishAutoAnnotationBatchEvent(HashSet<Long> filesId, Long datasetId, String versionName) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 : {} ", fileStateMachine.getMemoryFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", filesId.toString(), versionName, datasetId);
        LambdaUpdateWrapper<DatasetVersionFile> updatawrapper = new LambdaUpdateWrapper<>();
        if (StringUtils.isBlank(versionName)) {
            updatawrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            updatawrapper.eq(DatasetVersionFile::getVersionName, versionName);
        }
        updatawrapper.eq(DatasetVersionFile::getDatasetId, datasetId);
        updatawrapper.in(DatasetVersionFile::getFileId, filesId);
        int update = datasetVersionFileMapper.update(new DatasetVersionFile() {{
                                                         setAnnotationStatus(FileStateEnum.AUTO_TAG_COMPLETE_FILE_STATE.getCode());
                                                         if (versionName != null) {
                                                             setChanged(Constant.CHANGED);
                                                         }
                                                     }},
                updatawrapper);
        fileStateMachine.setMemoryFileState(fileStateMachine.getAnnotationCompleteFileState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", fileStateMachine.getMemoryFileState());
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
        LogUtil.info(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 : {} ", fileStateMachine.getMemoryFileState());
        LogUtil.info(LogEnum.STATE_MACHINE, " 接受参数： {} ", filesId.toString(), versionName, datasetId);
        LambdaUpdateWrapper<DatasetVersionFile> updatawrapper = new LambdaUpdateWrapper<>();
        if (StringUtils.isBlank(versionName)) {
            updatawrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            updatawrapper.eq(DatasetVersionFile::getVersionName, versionName);
        }
        updatawrapper.eq(DatasetVersionFile::getDatasetId, datasetId);
        updatawrapper.in(DatasetVersionFile::getFileId, filesId);
        datasetVersionFileMapper.update(new DatasetVersionFile() {{
                                                         setAnnotationStatus(FileStateEnum.ANNOTATION_NOT_DISTINGUISH_FILE_STATE.getCode());
                                                         if (versionName != null) {
                                                             setChanged(Constant.CHANGED);
                                                         }
                                                     }},
                updatawrapper);
        fileStateMachine.setMemoryFileState(fileStateMachine.getAnnotationNotDistinguishFileState());
        LogUtil.info(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", fileStateMachine.getMemoryFileState());
    }

}
