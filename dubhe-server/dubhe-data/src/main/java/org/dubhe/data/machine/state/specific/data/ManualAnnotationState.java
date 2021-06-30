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

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.statemachine.exception.StateMachineException;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.machine.constant.ErrorMessageConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.state.AbstractDataState;
import org.dubhe.data.machine.statemachine.DataStateMachine;
import org.dubhe.data.machine.utils.StateIdentifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @description 手动标注中状态类
 * @date 2020-08-27
 */
@Component
public class ManualAnnotationState extends AbstractDataState {

    @Autowired
    @Lazy
    private DataStateMachine dataStateMachine;

    @Autowired
    private DatasetVersionFileMapper datasetVersionFileMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private StateIdentifyUtil stateIdentify;

    /**
     * 手动标注事件   手动标注中-->手动标注完成-->标注完成
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void manualAnnotationCompleteEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAnnotationCompleteState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 手动标注事件   手动标注中-->删除文件，文件只包含自动标注完成-->自动标注完成
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void manualAutomaticLabelingCompletionEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAutoTagCompleteState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 手动标注事件   手动标注中-->文件删除-->未标注
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void manualNotMakedEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }


    /**
     * 清除标注 标注完成-->未标注
     *
     * @param primaryKeyId 数据集ID
     */
    @Override
    public void deleteAnnotatingEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        Dataset dataset = datasetMapper.selectById(primaryKeyId);
        datasetVersionFileMapper.update(
                new DatasetVersionFile() {{
                    setAnnotationStatus(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);
                    setChanged(Constant.CHANGED);
                }},
                new UpdateWrapper<DatasetVersionFile>()
                        .lambda()
                        .eq(DatasetVersionFile::getDatasetId, dataset.getId())
                        .eq(dataset.getCurrentVersionName() != null, DatasetVersionFile::getVersionName, dataset.getCurrentVersionName())
        );
        datasetMapper.updateStatus(dataset.getId(), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 自动标注事件处理方法
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void autoAnnotationsEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.AUTOMATIC_LABELING_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAutomaticLabelingState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件 未标注/手动标注中/自动标注完成/目标跟踪完成-->点击完成-->标注完成
     *
     * @param dataset 数据集详情
     */
    @Override
    public void finishManualEvent(Dataset dataset){
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        DataStateEnum status = stateIdentify.getStatus(dataset.getId(),dataset.getCurrentVersionName(),true);
        switch (status){
            case MANUAL_ANNOTATION_STATE:
                //手动标注中
                break;
            case AUTO_TAG_COMPLETE_STATE:
                //自动标注完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode(),dataStateMachine.getAutoTagCompleteState());
                break;
            case ANNOTATION_COMPLETE_STATE:
                //标注完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode(),dataStateMachine.getAnnotationCompleteState());
                break;
            case TARGET_COMPLETE_STATE:
                //目标跟踪完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.TARGET_COMPLETE_STATE.getCode(),dataStateMachine.getTargetCompleteState());
                break;
            case NOT_ANNOTATION_STATE:
                //未标注
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.NOT_ANNOTATION_STATE.getCode(),dataStateMachine.getNotAnnotationState());
                break;
            default:
                throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 删除文件事件
     *
     * @param dataset 数据集详情
     */
    @Override
    public void deleteFilesEvent(Dataset dataset) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        DataStateEnum status = stateIdentify.getStatus(dataset.getId(),dataset.getCurrentVersionName(),true);
        switch (status){
            case AUTO_TAG_COMPLETE_STATE:
                //自动标注完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode(),dataStateMachine.getAutoTagCompleteState());
                break;
            case ANNOTATION_COMPLETE_STATE:
                //标注完成
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode(),dataStateMachine.getAnnotationCompleteState());
                break;
            case NOT_ANNOTATION_STATE:
                //未标注
                dataStateMachine.doStateChange(dataset.getId(),DataStateEnum.NOT_ANNOTATION_STATE.getCode(),dataStateMachine.getNotAnnotationState());
                break;
            case MANUAL_ANNOTATION_STATE:
                //手动标注中
                break;
            default:
                throw new StateMachineException(ErrorMessageConstant.DATASET_CHANGE_ERR_MESSAGE);
        }
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【手动标注中】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 表格导入事件 未标注 --> 导入表格 --> 导入中
     *
     * @param primaryKeyId 数据集详情
     */
    @Override
    public void tableImportEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.IN_THE_IMPORT_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getImportState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 多视频导入事件  标注中 --> 导入视频 --> 采样中
     *
     * @param primaryKeyId 数据集详情
     */
    @Override
    public void sampledEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.SAMPLING_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getSamplingState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【未标注】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }
}