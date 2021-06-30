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
package org.dubhe.data.service;

import io.swagger.models.auth.In;
import org.dubhe.data.domain.dto.DatasetVersionFileDTO;
import org.dubhe.data.domain.entity.DataFileAnnotation;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;

import java.util.List;

/**
 * @description 数据文件标注服务
 * @date 2021-01-06
 */
public interface DataFileAnnotationService {

    /**
     * 批量保存数据文件标注数据
     *
     * @param dataFileAnnotations   数据文件标注集合
     */
    void insertDataFileBatch(List<DataFileAnnotation> dataFileAnnotations);

    /**
     * 根据版本文件ID批量删除标注数据
     *
     * @param versionIds    版本文件IDS
     */
    void deleteBatch(Long datasetId,List<Long> versionIds);


    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签列表
     */
    List<Long> findInfoByVersionId(Long datasetId,Long versionFileId);

    /**
     * 批量新增标注文件数据
     *  @param datasetId    数据集ID
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     * @param fileName      文件名称
     */
    void insertAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds, String fileName);


    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void deleteAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds);


    /**
     * 根据版本文件 id 修改状态
     *
     * @param ids        版本文件ID
     * @param deleteFlag 删除标识
     */
    void updateStatusByVersionIds(Long datasetId,List<Long> ids, boolean deleteFlag);

    /**
     * 数据集需备份标注数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionFiles  版本文件
     */
    void backupDataFileAnnotationDataByDatasetId(Dataset originDataset, Dataset targetDataset, List<DatasetVersionFile> versionFiles);

    /**
     * 根据标签Id,数据集Id,数据集文件版本id查询标签标注信息
     *
     * @param labelId           标签id
     * @param datasetId         数据集Id
     * @return DataFileAnnotation
     */
    List<DataFileAnnotation> getLabelIdByDatasetIdAndVersionId(Long[] labelId, Long datasetId, Long offset, Integer limit,String versionName);

    /**
     * 获取发布时版本标注信息
     *
     * @param datasetId         数据集ID
     * @param versionName       版本名称
     * @param status            版本文件状态
     * @return List<DataFileAnnotation>   标注信息
     */
    List<DataFileAnnotation> getAnnotationByVersion(Long datasetId, String versionName, Integer status);

    /**
     *  更新标注信息
     *
     * @param dataFileAnnotations 标注信息列表
     */
    void updateDataFileAnnotations(List<DataFileAnnotation> dataFileAnnotations);

    /**
     * 回退标注信息
     *
     * @param datasetId             数据集ID
     * @param versionSource         源版本
     * @param status                标注状态
     * @param invariable            是否为版本标注
     */
    void rollbackAnnotation(Long datasetId, String versionSource, Integer status, Integer invariable);

    /**
     * 筛选查询数量
     *
     * @param datasetId         数据集id
     * @param versionName       版本名称
     * @param labelId           标签id
     */
    Long selectDetectionCount(Long datasetId, String versionName, Long[] labelId);
}
