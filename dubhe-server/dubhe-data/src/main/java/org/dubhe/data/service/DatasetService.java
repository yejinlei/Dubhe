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
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.biz.base.vo.ProgressVO;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.domain.vo.*;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @description 数据集信息服务
 * @date 2020-04-10
 */
public interface DatasetService {

    /**
     * 文件提交
     *
     * @param datasetId          数据集id
     * @param batchFileCreateDTO 保存的文件
     */
    void uploadFiles(Long datasetId, BatchFileCreateDTO batchFileCreateDTO);

    /**
     * 上传视频
     *
     * @param datasetId     数据集id
     * @param fileCreateDTO 上传视频参数
     */
    void uploadVideo(Long datasetId, FileCreateDTO fileCreateDTO);

    /**
     * 数据集正在自动标注中的文件不允许删除,否则会导致进行中的任务完成的文件数达不到总文件数，无法完成
     *
     * @param fileDeleteDTO 删除文件参数
     */
    void delete(FileDeleteDTO fileDeleteDTO);

    /**
     * 创建数据集
     *
     * @param datasetCreateDTO 数据集信息
     * @return Long 数据集id
     */
    Long create(DatasetCreateDTO datasetCreateDTO);

    /**
     * 数据集查询
     *
     * @param page            分页信息
     * @param datasetQueryDTO 查询条件
     * @return MapMap<String, Object> 查询出对应的数据集
     */
    Map<String, Object> listVO(Page<Dataset> page, DatasetQueryDTO datasetQueryDTO);

    /**
     * 获取数据集详情
     *
     * @param datasetId 数据集id
     * @return DatasetVO 根据Id查询出对应的数据集
     */
    DatasetVO get(Long datasetId);

    /**
     * 数据集修改
     *
     * @param datasetCreateDTO 数据集修改条件
     * @param datasetId        数据集id
     * @return boolean         修改结果是否成功
     */
    boolean update(DatasetCreateDTO datasetCreateDTO, Long datasetId);

    /**
     * 删除数据集
     *
     * @param datasetDeleteDTO 删除数据集条件
     */
    void delete(DatasetDeleteDTO datasetDeleteDTO);

    /**
     * 数据集下载
     *
     * @param datasetId           数据集id
     * @param httpServletResponse httpServletResponse
     */
    void download(Long datasetId, HttpServletResponse httpServletResponse);

    /**
     * 查询有版本的数据集
     *
     * @param page                分页条件
     * @param datasetIsVersionDTO 查询数据集(有版本)条件
     * @return Map<String, Object> 查询数据集(有版本)列表
     */
    Map<String, Object> dataVersionListVO(Page<Dataset> page, DatasetIsVersionDTO datasetIsVersionDTO);

    /**
     * 数据扩容
     *
     * @param datasetEnhanceRequestDTO 数据扩容参数
     */
    void enhance(DatasetEnhanceRequestDTO datasetEnhanceRequestDTO);

    /**
     * 查询公共和个人数据集的数量
     *
     * @return DatasetCountVO 查询公共和个人数据集的数量
     */
    DatasetCountVO queryDatasetsCount();

    /**
     * 保存标签
     *
     * @param label     标签
     * @param datasetId 数据集id
     */
    void saveLabel(Label label, Long datasetId);

    /**
     * 根据ID获取数据集详情
     *
     * @param datasetId 数据集ID
     * @return dataset 数据集
     */
    Dataset getOneById(Long datasetId);

    /**
     * 检测是否为公共数据集
     *
     * @param id 数据集ID
     * @param type 校验类型
     * @return Boolean 更新结果
     */
    Boolean checkPublic(Long id, OperationTypeEnum type);

    /**
     * 检测是否为公共数据集
     *
     * @param dataset 数据集
     * @param type 操作类型枚举
     * @return Boolean 更新结果
     */
    Boolean checkPublic(Dataset dataset, OperationTypeEnum type);

    /**
     * 自动标注检查
     *
     * @param file 文件
     */
    void autoAnnotatingCheck(File file);


    /**
     * 条件查询数据集
     *
     * @param datasetQueryWrapper
     * @return List 数据集列表
     */
    List<Dataset> queryList(QueryWrapper<Dataset> datasetQueryWrapper);

    /**
     * 更新数据集状态
     *
     * @param dataset 数据集
     * @param pre     转变前的状态
     * @return boolean 更新数据集状态是否成功
     */
    boolean updateStatus(Dataset dataset, DataStateEnum pre);

    /**
     * 更新状态
     *
     * @param id 数据集id
     * @param to 转变后的状态
     * @return boolean 更新状态是否成功
     */
    boolean updateStatus(Long id, DataStateEnum to);


    /**
     * 更改数据集状态
     *
     * @param dataset 数据集
     * @param to      转变后的状态
     * @return boolean 更改数据集状态是否成功
     */
    boolean transferStatus(Dataset dataset, DataStateEnum to);

    /**
     * 导入用户自定义数据集
     *
     * @param datasetCustomCreateDTO 用户导入自定义数据集请求实体
     * @return Long 数据集ID
     */
    Long importDataset(DatasetCustomCreateDTO datasetCustomCreateDTO);

    /**
     * 数据集置顶
     *
     * @param datasetId 数据集
     */
    void topDataset(Long datasetId);

    /**
     * 预置标签处理
     *
     * @param presetLabelType 预置标签类型
     * @param datasetId       数据集id
     */
    void presetLabel(Integer presetLabelType, Long datasetId);

    /**
     * 获取数据集标签类型
     *
     * @param datasetId 数据集ID
     * @return DatasetLabelEnum 数据集标签类型
     */
    DatasetLabelEnum getDatasetLabelType(Long datasetId);


    /**
     * 获取数据集标注进度接口
     *
     * @param datasetIds 数据集id列表
     * @return Map<Long, ProgressVO>
     */
    Map<Long, ProgressVO> progress(List<Long> datasetIds);

    /**
     * 查询数据集状态
     *
     * @param datasetIds 数据集Id
     * @return Map<Long, IsImportVO> 返回数据集状态
     */
    Map<Long, IsImportVO> determineIfTheDatasetIsAnImport(List<Long> datasetIds);

    /**
     * 整体删除数据还原
     *
     * @param dto 还原实体
     */
    void allRollback(RecycleCreateDTO dto);

    /**
     * 普通数据集转预置
     *
     * @param datasetConvertPresetDTO 普通数据集转预置请求实体
     */
    void convertPreset(DatasetConvertPresetDTO datasetConvertPresetDTO);

    /**
     * 根据数据集ID查询数据集是否转换信息
     *
     * @param datasetId 数据集ID
     * @return  true: 允许 false: 不允许
     */
    Boolean getConvertInfoByDatasetId(Long datasetId);

    /**
     * 根据数据集ID删除数据信息
     *
     * @param datasetId 数据集ID
     */
    void deleteInfoById(Long datasetId);

    /**
     * 备份数据集DB和MINIO数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionFiles  原版本列表
     */
    void backupDatasetDBAndMinioData(Dataset originDataset, Dataset targetDataset, List<DatasetVersionFile> versionFiles);

    /**
     * 获取预置数据集列表
     *
     * @return Map<String, Object> 数据集详情
     */
    List<Dataset> getPresetDataset();

}
