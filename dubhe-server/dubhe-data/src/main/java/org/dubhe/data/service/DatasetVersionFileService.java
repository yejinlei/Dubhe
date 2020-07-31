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

package org.dubhe.data.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.data.constant.FileStatusEnum;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;

import java.util.Collection;
import java.util.List;
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
     * 删除数据集下所有版本文件关系
     *
     * @param datasetId 数据集id
     */
    void datasetDelete(Long datasetId);

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
     * 获取数据集指定状态下的的文件列表
     *
     * @param datasetId   数据集id
     * @param notInStatus 非指定状态
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFile> getFilesByDatasetIdAndStatus(Long datasetId, Collection<Integer> notInStatus);

    /**
     * 数据集标注状态
     *
     * @param datasetId   数据集id
     * @param status      状态
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    List<DatasetVersionFile> getListByDatasetIdAndAnnotationStatus(Long datasetId, String versionName, Collection<Integer> status);

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
     * @param dataset 数据集
     * @return boolean 数据集是否回退
     */
    boolean rollbackDataset(Dataset dataset);

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
     * 根据当前数据集版本获取图片的数量
     *
     * @param datasetId
     * @param versionName
     * @return: java.lang.Integer
     */
    Integer getImageCountsByDatasetIdAndVersionName(Long datasetId, String versionName);

    /**
     * 根据条件查询数据集版本文件
     *
     * @param fileQueryWrapper 查询条件
     * @return
     */
    List<DatasetVersionFile> queryList(QueryWrapper<DatasetVersionFile> fileQueryWrapper);

    /**
     * 根据条件更新数据集版本文件数据
     *
     * @param datasetVersionFile 数据集文件关系对象
     * @param updateWrapper      更新条件
     * @return
     */
    boolean updateEntity(DatasetVersionFile datasetVersionFile, Wrapper<DatasetVersionFile> updateWrapper);

    /**
     * 清除标注信息后数据集状态变为初始状态
     *
     * @param dataset 数据集id
     * @param init    初始状态
     */
    void updateStatus(Dataset dataset, FileStatusEnum init);


    /**
     * 获取文件对应增强文件列表
     *
     * @param process 命令执行过程
     * @return boolean 返回结果是否为空
     */
    boolean getCopyResult(Process process);

    /**
     * 获取当前数据集版本的原始文件
     *
     * @param dataset  当前数据集
     * @return: Integer 原始文件数量
     */
    Integer getSourceFileCount(Dataset dataset);
}
