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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dubhe.data.domain.bo.FileUploadBO;
import org.dubhe.data.domain.dto.DatasetVersionFileDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersion;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 数据集版本文件 服务类
 * @date 2020-05-14
 */
public interface DatasetVersionFileService {

    /**
     * 查询数据集指定版本文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFile> findByDatasetIdAndVersionName(Long datasetId, String versionName);

    /**
     * 批量写入数据集版本文件
     *
     * @param data 版本文件列表
     */
    void insertList(List<DatasetVersionFile> data);

    /**
     * 新增关系版本变更
     *
     * @param datasetId     数据集id
     * @param versionSource 源版本
     * @param versionTarget 目标版本
     */
    void newShipVersionNameChange(Long datasetId, String versionSource, String versionTarget);

    /**
     * 版本文件删除标记
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param fileIds     文件id列表
     */
    void deleteShip(Long datasetId, String versionName, List<Long> fileIds);



    /**
     * 数据集版本文件标注状态修改
     *
     * @param datasetId    数据集id
     * @param versionName  版本名称
     * @param fileId       文件id
     * @param sourceStatus 源状态
     * @param targetStatus 目标状态
     * @return int 标注修改的数量
     */
    int updateAnnotationStatus(Long datasetId, String versionName, Set<Long> fileId, Integer sourceStatus, Integer targetStatus);

    /**
     * 获取数据集指定版本下文件列表(有效文件)
     *
     * @param datasetId   数据id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFile> getFilesByDatasetIdAndVersionName(Long datasetId, String versionName);


    /**
     * 数据集标注状态
     *
     * @param datasetId   数据集id
     * @param status      状态
     * @param versionName 版本名称
     * @param offset      偏移量
     * @param limit       页容量
     * @param order       排序方式
     * @param labelId     标签ID
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFileDTO> getListByDatasetIdAndAnnotationStatus(Long datasetId, String versionName, Integer[] status, Long offset,
                                                                     Integer limit, String orderByName, String order, Long[] labelId);

    /**
     * 获取数据集指定版本第一张图片
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param status      状态
     * @return DatasetVersionFile 版本首张图片
     */
    DatasetVersionFile getFirstByDatasetIdAndVersionNum(Long datasetId, String versionName, Collection<Integer> status);

    /**
     * 根据版本列表中的文件id获取文件列表
     *
     * @param datasetVersionFiles 版本文件中间表列表
     * @return List<File> 文件列表
     */
    List<File> getFileListByVersionFileList(List<DatasetVersionFile> datasetVersionFiles);

    /**
     * 通过数据集和版本获取文件状态
     *
     * @param datasetId   数据集id
     * @param versionName 版本版本名称
     * @return List<File> 文件状态列表
     */
    List<Integer> getFileStatusListByDatasetAndVersion(Long datasetId, String versionName);

    /**
     * 版本回退
     *
     * @param dataset 数据集对象
     */
    void rollbackDataset(Dataset dataset);

    /**
     * 获取可以增强的文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 增强版本文件列表
     */
    List<DatasetVersionFile> getNeedEnhanceFilesByDatasetIdAndVersionName(Long datasetId, String versionName);

    /**
     * 获取文件对应增强文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param fileId      文件id
     * @return List<File> 增强文件列表
     */
    List<File> getEnhanceFileList(Long datasetId, String versionName, Long fileId);

    /**
     * 获取增强文件数量
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return Integer    当前版本增强文件数量
     */
    Integer getEnhanceFileCount(Long datasetId, String versionName);

    /**
     * 根据当前数据集版本获取图片的数量
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return 当前数据集版本获取图片的数量
     */
    Integer getImageCountsByDatasetIdAndVersionName(Long datasetId, String versionName);


    /**
     * 获取当前数据集版本的原始文件
     *
     * @param dataset  当前数据集
     * @return Integer 原始文件数量
     */
    Integer getSourceFileCount(Dataset dataset);

    /**
     * 分页获取数据集版本文件数据
     *
     * @param offset      偏移量
     * @param pageSize    页容量
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return
     */
    List<DatasetVersionFile> getPages(int offset, int pageSize, Long datasetId, String versionName);



    /**
     * 查询当前数据集下所有的文件数量
     * @param queryWrapper 查询条件
     * @return 数量
     */
    Integer getFileCountByDatasetIdAndVersion(LambdaQueryWrapper<DatasetVersionFile> queryWrapper);

    /**
     * 获取数据集文件状态统计数据
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return 数据集状态统计
     */
    Map<Integer, Integer> getDatasetVersionFileCount(Long datasetId, String versionName);

    /**
     * 获取数据集文件数量统计数据
     *
     * @param datasetVersion 数据集版本
     * @return 数据集文件统计
     */
    Integer selectDatasetVersionFileCount(DatasetVersion datasetVersion);

    /**
     * 获取offset
     *
     * @param datasetId 数据集id
     * @param fileId    文件idR
     * @param type      数据集类型
     * @return Integer 获取到offset
     */
    Integer getOffset(Long fileId, Long datasetId, Integer[] type, Long[] labelIds);


    /**
     * 条件查询数据集文件表的数据量
     *
     * @param eq 条件
     * @return 大小
     */
    long selectCount(LambdaQueryWrapper<DatasetVersionFile> eq);

    /**
     * 判断当前数据集是否需要回滚
     *
     * @param dataset 需要回滚的数据集
     * @return boolean 数据集是否需要回退
     */
    boolean isNeedToRollback(Dataset dataset);

    /**
     * 获取数据版本文件信息
     *
     * @param datasetId    数据集ID
     * @param versionName  当前版本
     * @param fileId       文件Id
     * @return  获取数据版本文件实体
     */
    DatasetVersionFile getDatasetVersionFile(Long datasetId,String versionName,Long fileId);

    /**
     * 根据当前数据集和当前版本号修改数据集是否改变
     *
     * @param id           数据集id
     * @param versionName  版本号
     */
    void updateChanged(Long id, String versionName);

    /**
     * 清除标注操作
     *
     * @param datasetId  数据集ID
     */
    void deleteAnnotating(Long datasetId);

    /**
     * 获取数据集版本文件ID
     *
     * @param id                    数据集ID
     * @param currentVersionName    版本名称
     * @param fileIds               文件id
     * @return 获取数据集版本文件
     */
    List<DatasetVersionFile> getVersionFileByDatasetAndFile(Long id, String currentVersionName, Set<Long> fileIds);

    /**
     * 修改文件状态
     *
     * @param datasetVersionFile 数据集版本文件实体
     */
    void updateStatusById(DatasetVersionFile datasetVersionFile);

    /**
     * 根据数据集分页条件查询文件总数量
     *
     * @param datasetId             数据集ID
     * @param currentVersionName    当前版本号码
     * @param status                状态
     * @param labelId               标签ID
     * @return 文件数量
     */
    int selectFileListTotalCount(Long datasetId, String currentVersionName, Integer[] status, Long[] labelId);


    /**
     * 根据数据集ID和版本号查询版本文件信息
     *
     * @param datasetId     数据集ID
     * @param versionName   版本名称
     * @return  数据集版本文件列表
     */
    List<DatasetVersionFile> getDatasetVersionFileByDatasetIdAndVersion(Long datasetId, String versionName);


    /**
     * 备份版本文件数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionFiles  原版本文件列表
     * @param files         已转换文件列表
     */
    void backupDatasetVersionFileDataByDatasetId(Dataset originDataset, Dataset targetDataset, List<DatasetVersionFile> versionFiles, List<File> files);

    /**
     * 文件id获取版本文件id
     *
     * @param datasetId         数据集id
     * @param fileIds           文件id
     * @return List<Long>       版本文件id
     */
    List<Long> getVersionFileIdsByFileIds(Long datasetId, List<Long> fileIds);

    /**
     * 获取版本文件id
     *
     * @param datasetId         数据集id
     * @param fileName            文件id
     * @param versionName       版本名称
     */
    Long getVersionFileIdByFileName(Long datasetId, String fileName, String versionName);

    /**
     * 获取导入文件所需信息
     * @param datasetId         数据集id
     * @return List<FileUploadBO>
     */
    List<FileUploadBO> getFileUploadContent(Long datasetId, List<Long> fileIds);
}
