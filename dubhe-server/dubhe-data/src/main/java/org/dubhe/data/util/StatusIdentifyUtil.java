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

package org.dubhe.data.util;

import org.dubhe.data.constant.DatasetStatusEnum;
import org.dubhe.data.constant.FileStatusEnum;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 根据文件状态获取实时数据集状态
 * @date 2020-06-04
 */
@Component
public class StatusIdentifyUtil {

    /**
     * 数据集版本文件关系服务类
     */
    @Autowired
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * 数据集服务类
     */
    @Autowired
    @Lazy
    private DatasetService datasetService;

    /**
     * 获取数据集状态(指定版本)
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return DatasetStatusEnum 数据集状态(指定版本)
     */
    public DatasetStatusEnum getStatus(Long datasetId, String versionName) {
        if (datasetId == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "datasetId is null");
            return null;
        }
        int datasetStatus = getDatasetStatus(datasetId);
        List<Integer> statusList = getFileStatusListByDatasetAndVersion(datasetId, versionName);
        return doJudgeStatus(datasetStatus, statusList);
    }

    public DatasetStatusEnum getStatus(Dataset dataset) {
        if (dataset == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "dataset is null");
            return null;
        }
        int datasetStatus = dataset.getStatus();
        List<Integer> statusList = getFileStatusListByDatasetAndVersion(dataset.getId(), dataset.getCurrentVersionName());
        return doJudgeStatus(datasetStatus, statusList);
    }


    /**
     * 标注任务失败后将数据集状态还原到标注中或者是未标注的状态
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return DatasetStatusEnum 标注任务失败后将数据集状态还原到标注中或者是未标注的状态
     */
    public DatasetStatusEnum failTaskGetStatus(Long datasetId, String versionName) {
        if (datasetId == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "datasetId is null");
            return null;
        }
        List<Integer> statusList = getFileStatusListByDatasetAndVersion(datasetId, versionName);
        if (statusList.contains(FileStatusEnum.AUTO_ANNOTATION.getValue()) || statusList.contains(FileStatusEnum.FINISH_AUTO_TRACK.getValue())) {
            if (statusList.contains(FileStatusEnum.INIT.getValue())) {
                return DatasetStatusEnum.MANUAL_ANNOTATING;
            } else if (isAutoFinished(statusList)) {
                return DatasetStatusEnum.AUTO_FINISHED;
            }
        }
        return DatasetStatusEnum.INIT;
    }

    /**
     * 获取数据集当前状态
     *
     * @param datasetId 数据集id
     * @return Integer 数据集当前状态
     */
    private Integer getDatasetStatus(Long datasetId) {
        return datasetService.getOneById(datasetId).getStatus();
    }

    /**
     * 获取数据集指定版本状态列表(去重)
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return List<Integer> 去重后的数据集指定版本状态列表
     */
    private List<Integer> getFileStatusListByDatasetAndVersion(Long datasetId, String versionName) {
        return datasetVersionFileService.getFileStatusListByDatasetAndVersion(datasetId, versionName);
    }

    /**
     * 数据集状态判断 基于文件状态
     *
     * @param datasetStatus 数据集状态
     * @param statusList    状态集合
     * @return DatasetStatusEnum 数据集状态
     */
    private DatasetStatusEnum doJudgeStatus(int datasetStatus, List<Integer> statusList) {
        DatasetStatusEnum status = null;
        status = doJudgeStatusByDataset(datasetStatus);
        if (status == null) {
            status = doJudgeStatusByVersionFile(statusList);
        }
        return status;
    }

    /**
     * 数据集状态判断 基于数据集当前状态
     *
     * @param datasetStatus 数据集状态
     * @return DatasetStatusEnum 返回当前状态
     */
    private DatasetStatusEnum doJudgeStatusByDataset(int datasetStatus) {
        if (isNotSample(datasetStatus)) {
            //未采样
            return DatasetStatusEnum.NOT_SAMPLE;
        } else if (isAutoAnnotating(datasetStatus)) {
            //自动标注中
            return DatasetStatusEnum.AUTO_ANNOTATING;
        } else if (isSampling(datasetStatus)) {
            //采样中
            return DatasetStatusEnum.SAMPLING;
        } else if (isEnhancing(datasetStatus)) {
            //增强中
            return DatasetStatusEnum.ENHANCING;
        }
        return null;
    }

    /**
     * 判断数据集状态 基于版本文件数据
     *
     * @param statusList 数据集状态集合
     * @return DatasetStatusEnum 数据集状态
     */
    private DatasetStatusEnum doJudgeStatusByVersionFile(List<Integer> statusList) {
        if (isInit(statusList)) {
            //未标注
            return DatasetStatusEnum.INIT;
        } else if (isManualAnnotating(statusList)) {
            //标注中
            return DatasetStatusEnum.MANUAL_ANNOTATING;
        } else if (isAutoFinished(statusList)) {
            //自动标注完成
            return DatasetStatusEnum.AUTO_FINISHED;
        } else if (isFinished(statusList)) {
            //标注完成
            return DatasetStatusEnum.FINISHED;
        } else if (isFinishedTrack(statusList)) {
            //目标跟踪完成
            return DatasetStatusEnum.FINISHED_TRACK;
        }
        return null;
    }

    /**
     * 未采样
     *
     * @param datasetStatus 数据集状态
     * @return boolean 确认结果
     */
    private boolean isNotSample(int datasetStatus) {
        return DatasetStatusEnum.NOT_SAMPLE.getValue() == datasetStatus;
    }

    /**
     * 采样中
     *
     * @param datasetStatus
     * @return boolean 确认结果
     */
    public boolean isSampling(int datasetStatus) {
        return DatasetStatusEnum.SAMPLING.getValue() == datasetStatus;
    }

    /**
     * 自动标注中
     *
     * @param datasetStatus 数据集状态
     * @return boolean 确认结果
     */
    private boolean isAutoAnnotating(int datasetStatus) {
        return DatasetStatusEnum.AUTO_ANNOTATING.getValue() == datasetStatus;
    }

    /**
     * 未标注
     *
     * 当前数据集不存在图片(删除)或者是当前数据集图片状态只包含未标注
     *
     * @param statusList 状态集
     * @return boolean 确认结果
     */
    private boolean isInit(List<Integer> statusList) {
        return statusList.isEmpty() || (statusList.size() == 1 && statusList.contains(FileStatusEnum.INIT.getValue()));
    }

    /**
     * 标注中
     *
     * @param statusList 状态集
     * @return boolean 确认结果
     */
    private boolean isManualAnnotating(List<Integer> statusList) {
        if (statusList.size() > 1 && statusList.contains(FileStatusEnum.INIT.getValue())) {
            return true;
        }
        return statusList.contains(FileStatusEnum.ANNOTATING.getValue());
    }

    /**
     * 自动标注完成
     *
     * @param statusList 状态集
     * @return boolean 确认结果
     */
    private boolean isAutoFinished(List<Integer> statusList) {
        if (statusList.size() > 1 && !statusList.contains(FileStatusEnum.FINISHED.getValue())) {
            return false;
        }
        return statusList.contains(FileStatusEnum.AUTO_ANNOTATION.getValue());
    }

    /**
     * 标注完成
     *
     * @param statusList 状态集
     * @return boolean 确认结果
     */
    private boolean isFinished(List<Integer> statusList) {
        return statusList.size() == 1 && statusList.contains(FileStatusEnum.FINISHED.getValue());
    }

    /**
     * 目标跟踪完成
     *
     * @param statusList 状态集
     * @return boolean 确认结果
     */
    private boolean isFinishedTrack(List<Integer> statusList) {
        if (statusList.size() > 1 && !statusList.contains(FileStatusEnum.FINISHED.getValue())) {
            return false;
        }
        return statusList.contains(FileStatusEnum.FINISH_AUTO_TRACK.getValue());
    }

    /**
     * 增强中
     *
     * @param datasetStatus
     * @return boolean 确认结果
     */
    private boolean isEnhancing(int datasetStatus) {
        return DatasetStatusEnum.ENHANCING.getValue() == datasetStatus;
    }

}
