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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.dubhe.data.domain.entity.DataFileAnnotation;

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
    void deleteBatch(List<Long> versionIds);

    /**
     * 删除已标注的文本标签
     *
     * @param wrapper 数据文件注释标签删除条件
     */
    void deleteFileAnnotationLabel(LambdaUpdateWrapper<DataFileAnnotation> wrapper);

    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签列表
     */
    List<Long> findInfoByVersionId(Long versionFileId);

    /**
     * 批量新增标注文件数据
     *
     * @param datasetId     数据集ID
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void insertAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds);

    /**
     * 批量修改标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void updateAnnotationFileByVersionIdAndLabelIds(Long versionFileId, List<Long> fileLabelIds);

    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void deleteAnnotationFileByVersionIdAndLabelIds(Long versionFileId, List<Long> fileLabelIds);
}
