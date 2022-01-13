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
import org.apache.ibatis.annotations.*;
import org.dubhe.data.domain.bo.FileUploadBO;
import org.dubhe.data.domain.dto.DatasetVersionFileDTO;
import org.dubhe.data.domain.entity.DataFileAnnotation;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 数据集版本文件 Mapper 接口
 * @date 2020-05-14
 */
public interface DatasetVersionFileMapper extends BaseMapper<DatasetVersionFile> {

    /**
     * 根据数据集ID和版本名称获取正常状态下对应文件列表
     *
     * @param datasetId                 数据集ID
     * @param versionName               数据集版本名称
     * @return List<DatasetVersionFile> 数据集ID和版本名称获取正常状态下对应文件列表
     */
    List<DatasetVersionFile> findByDatasetIdAndVersionName(@Param("datasetId") Long datasetId,
                                                           @Param("versionName") String versionName);

    /**
     * 新增关系版本变更
     *
     * @param datasetId     数据集ID
     * @param versionSource 原版本号
     * @param versionTarget 目的版本号
     */
    void newShipVersionNameChange(@Param("datasetId") Long datasetId, @Param("versionSource") String versionSource,
                                  @Param("versionTarget") String versionTarget);

    /**
     * 删除数据集版本文件关系
     *
     * @param datasetId   数据集ID
     * @param versionName 版本名称
     * @param fileIds     文件ID
     */
    void updateStatusByFileIdAndDatasetId(@Param("datasetId") Long datasetId, @Param("versionName") String versionName,
                                          @Param("fileIds") List<Long> fileIds);



    /**
     * 按数据集和版本查找文件状态列表
     *
     * @param datasetId         数据集id
     * @param versionName       数据集版本名称
     * @return List<Integer>    数据集和版本列表
     */
    List<Integer> findFileStatusListByDatasetAndVersion(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 回退文件以及文件标注状态等
     *
     * @param datasetId   数据集id
     * @param versionName 数据集名称
     * @param changed     是否改变
     */
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
     * @param datasetId                 数据集ID
     * @param versionName               数据集版本名称
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
     * 获取当前版本对应增强文件数量
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return Integer    当前版本对应增强文件数量
     */
    Integer getEnhanceFileCount(@Param("datasetId")Long datasetId,@Param("versionName") String versionName);

    /**
     * 查询当前数据集版本的原始文件数量
     *
     * @param dataset   当前数据集
     * @return Integer 原始文件数量
     */
    Integer getSourceFileCount(@Param("dataset") Dataset dataset);

    /**
     * 更新数据集版本文件状态
     *
     * @param datasetVersionFile 数据集版本文件实体
     * @param status             数据集文件状态
     */
    @Update("update data_dataset_version_file set annotation_status = #{status} where dataset_id = #{datasetVersionFile.datasetId} " +
            " and (version_name = #{datasetVersionFile.versionName} or version_name is NULL) and file_id = #{datasetVersionFile.fileId} and annotation_status = #{datasetVersionFile.status}")
    void updateStatus(@Param("datasetVersionFile") DatasetVersionFile datasetVersionFile, @Param("status") Integer status);

    /**
     * 获取数据集的版本文件数据
     *
     * @param datasetIds 数据集IDS
     * @return  List<DatasetVersionFile>   数据集的版本文件数据
     */
    List<DatasetVersionFile> listDatasetVersionFileByDatasetIds(List<Long> datasetIds);

    /**
     * 获取数据集文件状态统计数据
     *
     * @param datasetId    数据集ID
     * @param versionName  数据集版本名称
     * @return Map<Integer, Integer>   数据集文件状态统计数据
     */
    @MapKey("status")
    Map<Integer, Integer> getDatasetVersionFileCount(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 根据数据集ID删除数据版本文件数据
     *
     * @param datasetId     数据集ID
     * @param limitNumber   删除数量
     * @return int 成功删除条数
     */
    @Delete("delete from data_dataset_version_file where dataset_id = #{datasetId} limit #{limitNumber} ")
    int deleteBydatasetId(@Param("datasetId") Long datasetId, @Param("limitNumber") int limitNumber);




    /**
     * 通过数据集ID和状态获取ID
     *
     * @param datasetId     数据集ID
     * @param versionName   版本名称
     * @param status        文件状态
     * @param orderByName   排序字段
     * @param offset        偏移量
     * @param limit         页容量
     * @param order         排序方式
     * @param dataFileAnnotations       标签ID
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFileDTO> getIdByDatasetIdAndAnnotationStatus(@Param("datasetId") Long datasetId,
                                                                          @Param("versionName") String versionName,
                                                                          @Param("status") Set<Integer> status,
                                                                          @Param("orderByName")String orderByName,
                                                                          @Param("offset") Long offset,
                                                                          @Param("limit") Integer limit,
                                                                          @Param("order") String order,
                                                                          @Param("dataFileAnnotations")List<DataFileAnnotation> dataFileAnnotations,
                                                                          @Param("annotationType")Integer annotationType);





    /**
     * 根据当前数据集和当前版本号修改数据集是否改变
     *
     * @param id           数据集id
     * @param versionName  版本号
     */
    @Update("update data_dataset_version_file set changed = 1 where dataset_id = #{datasetId} and version_name = #{currentVersionName}")
    void updateChanged(@Param("datasetId") Long id,
                       @Param("currentVersionName") String versionName);

    /**
     * 根据文件id 和 当前版本修改数据
     *
     * @param prediction         预测值
     * @param fileId             文件ID
     * @param currentVersionName 当前版本
     * @param datasetId          数据集ID
     */
    void updateByFileIdAndCurrentVersionName(@Param("prediction")int prediction, @Param("fileId")Long fileId,
                                             @Param("currentVersionName")String currentVersionName, @Param("datasetId")Long datasetId);

    /**
     * 根据数据集ID/版本名称/文件ID 查询数据集版本文件数据
     *
     * @param datasetId     数据集ID
     * @param versionName   版本名称
     * @param fileIds       文件IDS
     * @return
     */
    List<DatasetVersionFile> selectByDatasetIdAndVersionNameAndFileIds(
            @Param("datasetId")Long datasetId, @Param("versionName")String versionName, @Param("fileIds")List<Long> fileIds);

    /**
     *  修改标注状态接口
     *
     * @param annotationStatus 标注状态
     * @param datasetId        数据集id
     * @param id               Id
     */
    @Update("update data_dataset_version_file set annotation_status = #{annotationStatus} where dataset_id = #{datasetId} and id = #{id}")
    void updateAnnotationStatusById(@Param("annotationStatus")Integer annotationStatus, @Param("datasetId") Long datasetId,  @Param("id")Long id);


    /**
     * 根据数据集分页条件查询文件总数量
     *
     * @param datasetId             数据集ID
     * @param versionName    当前版本号码
     * @param status                状态
     * @param versionId             标签ID
     * @return 文件数量
     */
    int selectFileListTotalCount(@Param("datasetId") Long datasetId,
                                  @Param("versionName") String versionName,
                                  @Param("status") Set<Integer> status,
                                  @Param("versionId") List<Long> versionId);

    /**
     * 批量新增版本文件数据
     *
     * @param versionFiles 版本文件数据
     */
    void insertBatch(@Param("list") List<DatasetVersionFile> versionFiles);

    /**
     * 根据数据集Id和版本号查询数据集版本列表
     *
     * @param datasetId
     * @param currentVersionName
     * @return List<DatasetVersionFile>
     */
    List<Long> findByDatasetIdAndVersionNameAndStatus(@Param("datasetId") Long datasetId, @Param("versionName") String currentVersionName,
                                                      @Param("labelIds") Long[] labelIds);

    /**
     * 搜索时获取文件offset
     *
     * @param datasetId        数据集ID
     * @param annotationStatus 文件标注状态
     * @param versionName      版本名称
     * @param versionFileId    版本文件ID
     * @param labelIds         标签ID集合
     * @return Integer 文件偏移量
     */
    Integer getOffset(@Param("datasetId") Long datasetId, @Param("annotationStatus")Set<Integer> annotationStatus,
                      @Param("versionName") String versionName, @Param("versionFileId") Long versionFileId,
                      @Param("labelIds") Long[] labelIds);

    /**
     * 文件id获取版本文件id
     *
     * @param datasetId         数据集id
     * @param fileIds           文件id
     * @return List<Long>       版本文件id
     */
    List<Long> getVersionFileIdsByFileIds(@Param("datasetId") Long datasetId,@Param("fileIds")List<Long> fileIds);

    /**
     * 获取版本文件id
     *
     * @param datasetId         数据集id
     * @param fileName          文件名称
     * @param versionName       版本名称
     * @return Long             版本文件id
     */
    Long getVersionFileIdByFileName(@Param("datasetId")Long datasetId, @Param("fileName")String fileName, @Param("versionName")String versionName);

    /**
     * 获取导入文件所需信息
     * @param datasetId         数据集id
     * @return List<FileUploadBO>
     */
    List<FileUploadBO> getFileUploadContent(@Param("datasetId")Long datasetId, @Param("fileIds")List<Long> fileIds);
}