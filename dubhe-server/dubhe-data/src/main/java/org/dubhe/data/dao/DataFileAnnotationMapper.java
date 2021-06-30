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
package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.models.auth.In;
import org.dubhe.data.domain.dto.DatasetVersionFileDTO;
import org.dubhe.data.domain.entity.DataFileAnnotation;
import org.springframework.security.core.parameters.P;

import java.util.List;

/**
 * @description 数据文件标注服务Mapper
 * @date 2021-01-06
 */
public interface DataFileAnnotationMapper  extends BaseMapper<DataFileAnnotation> {

    /**
     * 批量删除数据标注信息
     *
     * @param ids 数据集文件标注ID列表
     */
    void deleteBatch(@Param("datasetId") Long datasetId,@Param("ids") List<Long> ids);


    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签ID列表
     */
    List<Long> findInfoByVersionId(@Param("datasetId") Long datasetId,@Param("versionFileId") Long versionFileId);

    /**
     * 批量保存数据文件标注信息
     *
     * @param dataFileAnnotations   数据文件标注实体
     */
    void insertBatch(List<DataFileAnnotation> dataFileAnnotations);


    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void deleteAnnotationFileByVersionIdAndLabelIds(@Param("datasetId") Long datasetId,@Param("versionFileId")Long versionFileId, @Param("fileLabelIds") List<Long> fileLabelIds);


    /**
     * 根据版本文件 id 修改状态
     *
     * @param ids        版本文件ID
     * @param deleteFlag 删除标识
     */
    void updateStatusByVersionIds(@Param("datasetId") Long datasetId,@Param("ids")  List<Long> ids, @Param("deleteFlag")  boolean deleteFlag);

    /**
     * 根据标签Id,数据集Id,数据集文件版本id查询标签标注信息
     *
     * @param labelId           标签id
     * @param datasetId         数据集Id
     * @return List<DatasetVersionFileDTO>
     */
    List<DataFileAnnotation> getLabelIdByDatasetIdAndVersionId(@Param("labelId") Long[] labelId, @Param("datasetId")Long datasetId, @Param("offset") Long offset,
                                                               @Param("limit") Integer limit, @Param("versionName") String versionName);

    /**
     * 根据数据集id和标签id获取数据集版本文件id
     *
     * @param labelId   标签id
     * @param datasetId 数据集id
     * @param ids      数据集版本文件id
     * @return List<Long> 数据集版本文件id
     */
    List<Long> getVersionFileIdByLabelId(@Param("labelId") Long[] labelId,@Param("datasetId") Long datasetId,@Param("ids") List<Long> ids);

    /**
     * 获取发布时版本标注信息
     *
     * @param datasetId         数据集ID
     * @param versionName       版本名称
     * @param status            版本文件状态
     * @return List<DataFileAnnotation>   标注信息
     */
    List<DataFileAnnotation> getAnnotationByVersion(@Param("datasetId") Long datasetId, @Param("versionName") String versionName,
                                                    @Param("status")Integer status);

    /**
     * 更新标注信息
     *
     * @param id                标注ID
     * @param status            标注状态
     * @param invariable        是否为版本信息
     * @param datasetId         数据集ID
     * @param versionFileId     版本文件ID
     */
    void updateDataFileAnnotations(@Param("id")Long id,@Param("status")Integer status, @Param("invariable")Integer invariable,
                                   @Param("datasetId")Long datasetId, @Param("versionFileId")Long versionFileId);

    /**
     * 回退标注信息
     *
     * @param datasetId             数据集ID
     * @param versionSource         源版本
     * @param status                标注状态
     * @param invariable            是否为版本标注
     */
    void rollbackAnnotation(@Param("datasetId") Long datasetId,@Param("versionSource") String versionSource,
                            @Param("status") Integer status,@Param("invariable") Integer invariable);

    /**
     * 筛选查询数量
     *
     * @param datasetId         数据集id
     * @param versionName       版本名称
     * @param labelIds           标签id
     */
    Long selectDetectionCount(@Param("datasetId")Long datasetId, @Param("versionName")String versionName, @Param("labelIds")Long[] labelIds);
}
