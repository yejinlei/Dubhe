/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
import org.dubhe.data.constant.Constant;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.state.AbstractDataState;
import org.dubhe.data.machine.statemachine.DataStateMachine;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @description 标注完成状态类
 * @date 2020-08-27
 */
@Component
public class AnnotationCompleteState extends AbstractDataState {

    @Autowired
    @Lazy
    private DataStateMachine dataStateMachine;

    @Autowired
    private DatasetVersionFileMapper datasetVersionFileMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * 标注完成事件  标注完成-->上传新的图片，点击自动标注按钮-->自动标注中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void uploadSavePicturesEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.MANUAL_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getManualAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件  标注完成-->调用增强算法-->增强中
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void completeStrengthenEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.STRENGTHENING_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getSamplingState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件  标注完成-->删除图片-->未标注
     *
     * @param primaryKeyId 业务ID
     */
    @Override
    public void deletePicturesEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", primaryKeyId);
        datasetMapper.updateStatus(Long.valueOf(primaryKeyId), DataStateEnum.NOT_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getNotAnnotationState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件 标注完成-->未标注
     *
     * @param primaryKeyId 数据集ID
     */
    @Override
    public void deleteAnnotatingEvent(Integer primaryKeyId) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
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
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }

    /**
     * 标注完成事件 自动标注完成-->手动进行标注，点击保存-->标注中
     *
     * @param dataset 数据集详情
     */
    @Override
    public void manualAnnotationSaveEvent(Dataset dataset) {
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        datasetMapper.updateStatus(dataset.getId(), DataStateEnum.MANUAL_ANNOTATION_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getAutomaticLabelingState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }


    /**
     * 标注完成事件 标注完成-->算法目标跟踪-->目标跟踪中
     *
     * @param dataset 数据集详情
     */
    @Override
    public void trackEvent(Dataset dataset){
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件前内存中状态机的状态 :{} ", dataStateMachine.getMemoryDataState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 接受参数： {} ", dataset);
        datasetMapper.updateStatus(dataset.getId(), DataStateEnum.TARGET_FOLLOW_STATE.getCode());
        dataStateMachine.setMemoryDataState(dataStateMachine.getTargetFollowState());
        LogUtil.debug(LogEnum.STATE_MACHINE, " 【标注完成】 执行事件后内存状态机的切换： {}", dataStateMachine.getMemoryDataState());
    }


}
