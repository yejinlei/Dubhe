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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;

import java.util.List;

/**
 * @description 数据集版本文件 Mapper 接口
 * @date: 2020-05-14
 */
public interface DatasetVersionFileMapper extends BaseMapper<DatasetVersionFile> {

    /**
     * 根据数据集ID和版本名称获取正常状态下对应文件列表
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return List<DatasetVersionFile> 数据集ID和版本名称获取正常状态下对应文件列表
     */
    @Select("select * from data_dataset_version_file where dataset_id = #{datasetId} " +
            "and version_name = #{versionName} and status = 2 ")
    List<DatasetVersionFile> findByDatasetIdAndVersionName(Long datasetId, String versionName);

    /**
     * 新增关系版本变更
     *
     * @param datasetId     数据集ID
     * @param versionSource 原版本号
     * @param versionTarget 目的版本号
     */
    @UpdateProvider(type = org.dubhe.data.dao.provider.DatasetVersionFileProvider.class, method = "newShipVersionNameChange")
    void newShipVersionNameChange(@Param("datasetId") Long datasetId, @Param("versionSource") String versionSource,
                                  @Param("versionTarget") String versionTarget);

    /**
     * 删除数据集版本文件关系
     *
     * @param datasetId   数据集ID
     * @param versionName 版本名称
     * @param fileIds     文件ID
     */
    @UpdateProvider(type = org.dubhe.data.dao.provider.DatasetVersionFileProvider.class, method = "deleteShip")
    void deleteShip(@Param("datasetId") Long datasetId, @Param("versionName") String versionName,
                    @Param("fileIds") List<Long> fileIds);

    /**
     * 删除数据集下所有版本文件关系
     *
     * @param datasetId 数据集ID
     */
    @Delete("delete from data_dataset_version_file where dataset_id = #{datasetId}")
    void datasetDelete(@Param("datasetId") Long datasetId);

    /**
     * 按数据集和版本查找文件状态列表
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return List<Integer> 数据集和版本列表
     */
    @SelectProvider(type = org.dubhe.data.dao.provider.DatasetVersionFileProvider.class, method = "findFileStatusListByDatasetAndVersion")
    List<Integer> findFileStatusListByDatasetAndVersion(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 回退文件以及文件标注状态等
     *
     * @param datasetId   数据集id
     * @param versionName 数据集名称
     * @param changed     是否改变
     */
    @UpdateProvider(type = org.dubhe.data.dao.provider.DatasetVersionFileProvider.class, method = "rollbackFileAndAnnotationStatus")
    void rollbackFileAndAnnotationStatus(@Param("datasetId") Long datasetId, @Param("versionName") String versionName, @Param("changed") int changed);

    /**
     * 批量保存
     *
     * @param datasetVersionFiles 数据集版本文件列表
     */
    void saveList(@Param("datasetVersionFiles") List<DatasetVersionFile> datasetVersionFiles);

    /**
     * 获取数据集增强文件
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return List<DatasetVersionFile> 数据集增强文件
     */
    List<DatasetVersionFile> getNeedEnhanceFilesByDatasetIdAndVersionName(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 获取文件对应增强文件列表
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @param fileId      文件ID
     * @return List<File> 文件对应增强文件列表
     */
    List<File> getEnhanceFileList(@Param("datasetId") Long datasetId, @Param("versionName") String versionName, @Param("fileId") Long fileId);

    /**
     * 查询当前数据集版本的原始文件数量
     *
     * @param dataset   当前数据集
     * @return: Integer 原始文件数量
     */
    Integer getSourceFileCount(@Param("dataset") Dataset dataset);
}
