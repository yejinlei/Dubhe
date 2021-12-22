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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.biz.file.dto.FilePageDTO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.DatasetCsvImportDTO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.biz.file.dto.FileDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.domain.vo.FileQueryCriteriaVO;
import org.dubhe.data.domain.vo.FileVO;
import org.dubhe.biz.base.vo.ProgressVO;
import org.dubhe.data.machine.enums.FileStateEnum;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 文件信息服务
 * @date 2020-04-10
 */
public interface FileService {

    /**
     * 文件详情
     *
     * @param fileId 文件id
     * @param datasetId 数据集id
     * @return FileVO 文件详情
     */
    FileVO get(Long fileId,Long datasetId);

    /**
     * 文件查询
     *
     * @param datasetId         数据集id
     * @param page              分页条件
     * @param fileQueryCriteria 查询文件参数
     * @return Map<String, Object> 文件查询列表
     */
    Map<String, Object> listPage(Long datasetId, Page page, FileQueryCriteriaVO fileQueryCriteria);

    /**
     * 文件查询，物体检测标注页面使用
     *
     * @param datasetId 数据集id
     * @param offset    offset
     * @param limit     页容量
     * @param page      分页条件
     * @param type      文件类型
     * @return Page<File> 文件查询列表
     */
    Page<File> listByLimit(Long datasetId, Long offset, Integer limit, Integer page, Integer[] type, Long[] labelId);

    /**
     * 获取offset
     *
     * @param datasetId 数据集id
     * @param fileId    文件id
     * @param type      文件类型
     * @return Integer 获取到的offset
     */
    Integer getOffset(Long fileId, Long datasetId, Integer[] type, Long[] labelIds);

    /**
     * 获取首个文件
     *
     * @param datasetId 数据集id
     * @param type      文件类型
     * @return Long 获取首个文件
     */
    Long getFirst(Long datasetId, Integer type);

    /**
     * 对minio 的账户密码进行加密操作
     *
     * @return Map<String, String> minio账户密码加密map
     */
    Map<String, String> getMinIOInfo();

    /**
     * 获取文件对应所有增强文件
     *
     * @param fileId 文件id
     * @param datasetId 数据集id
     * @return List<File> 获取文件对应所有增强文件列表
     */
    List<File> getEnhanceFileList(Long fileId,Long datasetId);

    /**
     * 视频采样任务
     */
    void videoSample();

    /**
     * 更新文件状态
     *
     * @param files          文件集合
     * @param fileStatusEnum 文件状态
     * @return int 更新结果是否成功
     */
    int update(Collection<File> files, FileStateEnum fileStatusEnum);

    /**
     * 根据文件ID获取文件内容
     *
     * @param fileId 文件ID
     * @return 文件实体
     */
    File selectById(Long fileId, Long datasetId);

    /**
     * 根据查询条件获取第一个文件
     *
     * @param queryWrapper  查询条件
     * @return              文件详情
     */
    File selectOne(QueryWrapper<File> queryWrapper);


    /**
     * 如果ids为空，则返回空
     *
     * @param fileIds       文件id集合
     * @param datasetId     数据集ID
     * @return Set<File>    获取到的文件
     */
    Set<File> get(List<Long> fileIds, Long datasetId);

    /**
     * 保存文件
     *
     * @param fileId        文件id
     * @param files         file文件
     * @return List<Long>   保存文件数量
     */
    List<File> saveFiles(Long fileId, List<FileCreateDTO> files);

    /**
     * 数据集标注进度
     *
     * @param datasets 数据集
     * @return Map<Long, ProgressVO> 数据集标注进度
     */
    Map<Long, ProgressVO> listStatistics( List<Dataset> datasets);

    /**
     * 删除文件
     *
     * @param datasetId 数据集id
     */
    void delete(Long datasetId);

    /**
     * 判断视频数据集是否已存在视频
     *
     * @param datasetId 数据集id
     */
    void isExistVideo(Long datasetId);

    /**
     * 保存视频文件
     *
     * @param fileId fileId
     * @param files  file文件
     * @param type   文件类型
     * @param pid    文件父id
     * @param userId 用户id
     *
     * @return  List<File> 文件列表
     */
    List<File> saveVideoFiles(Long fileId, List<FileCreateDTO> files, int type, Long pid, Long userId);


    /**
     * 将整体任务分割
     *
     * @param files file文件
     * @param task  任务
     * @return List<TaskSplitBO> 分割后的任务
     */
    List<TaskSplitBO> split(Collection<File> files, Task task);

    /**
     * 根据条件获取文件列表
     *
     * @param wrapper 查询条件
     * @return
     */
    List<File> listFile(QueryWrapper<File> wrapper);

    /**
     * 批量获取数据集文件
     *
     * @param datasetId  数据集ID
     * @param offset     偏移量
     * @param batchSize  批大小
     * @return 文件列表
     */
    List<File> listBatchFile(Long datasetId, int offset, int batchSize);

    /**
     * 采样任务过期
     */
    void expireSampleTask();

    /**
     * 根据版本和数据集ID获取文件url
     *
     * @param datasetId     数据集ID
     * @param versionName   版本名
     * @return List<String> url列表
     */
    List<String> selectUrls(Long datasetId,String versionName);

    /**
     * 根据version.changed获取文件name列表
     *
     * @param datasetId     数据集ID
     * @param changed       版本文件是否改动
     * @param versionName   版本名称
     * @return List<name>   名称列表
     */
    List<String> selectNames(Long datasetId, Integer changed,String versionName);

    /**
     * 音频数据集文件查询
     *
     * @param datasetId         数据集id
     * @param page              分页条件
     * @param fileQueryCriteria 查询文件参数
     * @return Map<String, Object> 文件查询列表
     */
    Map<String, Object> audioFilesByPage(Long datasetId, Page page, FileQueryCriteriaVO fileQueryCriteria);

    /**
     * 文本数据集文件查询
     *
     * @param datasetId         数据集id
     * @param page              分页条件
     * @param fileQueryCriteria 查询文件参数
     * @return Map<String, Object> 文件查询列表
     */
    Map<String, Object> txtContentByPage(Long datasetId, Page page, FileQueryCriteriaVO fileQueryCriteria);

    /**
     * 文本状态数量统计
     *
     * @param datasetId               数据集ID
     * @param fileQueryCriteria       文件查询条件
     * @return 文本状态数量统计
     */
    ProgressVO getFileCountByStatus(Long datasetId, FileQueryCriteriaVO fileQueryCriteria);

    /**
     * 获取数据集文件数量
     *
     * @param datasetId 数据集ID
     * @return 数据集文件数量
     */
    int getFileCountByDatasetId(Long datasetId);

    /**
     * 获取数据集原图文件数量
     *
     * @param datasetId 数据集ID
     * @param versionName 版本名称
     * @return 数据集原图文件数量
     */
    int getOriginalFileCountOfDataset(Long datasetId, String versionName);


    /**
     * 备份数据集文件数据
     * @param originDataset    原数据集实体
     * @param targetDataset    目标数据集实体
     * @return 新生成文件数据
     */
    List<File> backupFileDataByDatasetId(Dataset originDataset, Dataset targetDataset);

    /**
     * 文本数据集csv导入
     *
     * @param datasetCsvImportDTO 导入信息
     */
    void tableImport(DatasetCsvImportDTO datasetCsvImportDTO);


    /**
     * 将文本数据同步至ES
     *
     * @param dataset 数据集
     * @param fileIdsNotToEs  需要同步的文件ID
     */
    void transportTextToEs(Dataset dataset,List<Long> fileIdsNotToEs,Boolean ifImport);

    /**
     * 还原es_transport状态
     *
     * @param datasetId 数据集ID
     * @param fileId 文件ID
     */
    void recoverEsStatus(Long datasetId, Long fileId);

    /**
     * 删除es中数据
     *
     * @param fileIds 文件ID数组
     */
    void deleteEsData(Long[] fileIds);

    /**
     * 获取文件列表
     *
     * @param datasetId  数据集ID
     * @param prefix     匹配前缀
     * @param recursive  是否递归
     * @param versionName 版本
     * @return List<FileListDTO> 文件列表
     */
    List<FileDTO> fileList(Long datasetId, String prefix, boolean recursive, String versionName, boolean isVersionFile);

    /**
     * 分页获取文件列表
     *
     * @param filePageDTO 文件查询和响应实体
     * @param datasetId 数据集ID
     */
    void filePage(FilePageDTO filePageDTO, Long datasetId);

}
