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
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.data.domain.entity.DatasetVersion;

import java.util.List;

/**
 * @description 数据集
 * @date 2020-05-14
 */
@DataPermission(ignoresMethod = {"insert", "getCountByDatasetVersionId", "findDatasetVersion"})
public interface DatasetVersionMapper extends BaseMapper<DatasetVersion> {

    /**
     * 查询某个数据集的某个版本是否存在
     *
     * @param datasetId             数据集ID
     * @param versionName           数据集版本
     * @return List<DatasetVersion> 数据集的版本信息
     */
    @Select("select * from data_dataset_version where dataset_id = #{datasetId} and version_name = #{versionName}")
    List<DatasetVersion> findDatasetVersion(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);


    /**
     * 获取指定数据集当前使用最大版本号
     *
     * @param datasetId     数据集ID
     * @return String       指定数据集当前使用最大版本号
     */
    @Select("select max(version_name) from data_dataset_version where dataset_id = #{datasetId} and version_name like 'V%'")
    String getMaxVersionName(@Param("datasetId") Long datasetId);

    /**
     * 获取当前数据集版本的url
     *
     * @param datasetId         数据集ID
     * @param versionName       数据集版本
     * @return List<String>    数据集版本的url
     */
    @Select("SELECT version_url FROM data_dataset_version  WHERE dataset_id = #{datasetId}  and version_name = #{versionName}")
    List<String> selectVersionUrl(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 根据数据集ID删除数据信息
     *
     * @param datasetId 数据集ID
     */
    @Delete("delete from data_dataset_version where dataset_id = #{datasetId}")
    void deleteByDatasetId(@Param("datasetId")  Long datasetId);


    /**
     * 根据数据集ID查询版本名称列表
     * @param datasetId 数据集ID
     * @return  版本名称列表
     */
    @Select("SELECT version_name FROM data_dataset_version  WHERE dataset_id = #{datasetId}")
    List<String> getDatasetVersionNameListByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * 查询当前版本文件数量
     *
     * @param datasetId         数据集ID
     * @param versionName       版本名称
     * @return Long             版本文件数量
     */
    @Select("select count(1) from data_dataset_version_file where dataset_id = #{datasetId} and version_name = #{versionName}")
    Integer getCountByDatasetVersionId(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);
}
