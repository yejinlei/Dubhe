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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.data.constant.DatasetStatusEnum;
import org.dubhe.data.domain.dto.BatchFileCreateDTO;
import org.dubhe.data.domain.dto.DatasetCreateDTO;
import org.dubhe.data.domain.dto.DatasetCustomCreateDTO;
import org.dubhe.data.domain.dto.DatasetDeleteDTO;
import org.dubhe.data.domain.dto.DatasetEnhanceFinishDTO;
import org.dubhe.data.domain.dto.DatasetEnhanceRequestDTO;
import org.dubhe.data.domain.dto.DatasetIsVersionDTO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.dto.FileDeleteDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.domain.vo.DatasetCountVO;
import org.dubhe.data.domain.vo.DatasetQueryDTO;
import org.dubhe.data.domain.vo.DatasetVO;
import org.springframework.transaction.annotation.Transactional;

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
     * @return: Map<String, Object> 查询数据集(有版本)列表
     */
    Map<String, Object> dataVersionlistVO(Page<Dataset> page, DatasetIsVersionDTO datasetIsVersionDTO);

    /**
     * 数据扩容
     *
     * @param datasetEnhanceRequestDTO 数据扩容参数
     */
    void enhance(DatasetEnhanceRequestDTO datasetEnhanceRequestDTO);

    /**
     * 数据增强完成
     *
     * @param datasetEnhanceFinishDTO 数据增强参数
     * @return boolean  数据增强完成是否成功
     */
    boolean enhanceFinish(DatasetEnhanceFinishDTO datasetEnhanceFinishDTO);

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
     * @return Long 保存标签数量
     */
    Long saveLabel(Label label, Long datasetId);

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
     * @param id 数据集id
     */
    void checkPublic(Long id);

    /**
     * 检测是否为公共数据集
     *
     * @param dataset 数据集
     */
    void checkPublic(Dataset dataset);

    /**
     * 自动标注检查
     *
     * @param file 文件
     */
    void autoAnnotatingCheck(File file);

    /**
     * 更新时间
     *
     * @param fileMap 文件map
     * @return boolean 更新结果
     */
    boolean updataTimeByIdSet(Map<Long, List<DatasetVersionFile>> fileMap);

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
    boolean updateStatus(Dataset dataset, DatasetStatusEnum pre);

    /**
     * 更新状态
     *
     * @param id 数据集id
     * @param to 转变后的状态
     * @return boolean 更新状态是否成功
     */
    boolean updateStatus(Long id, DatasetStatusEnum to);

    /**
     * 数据集更新
     *
     * @param dataset 数据集对象
     * @param updateWrapper 更新操作类
     * @return boolean 更新是否成功
     */
    boolean updateEntity(Dataset dataset, Wrapper<Dataset> updateWrapper);

    /**
     * 更改数据集状态
     *
     * @param dataset 数据集
     * @param to      转变后的状态
     * @return boolean 更改数据集状态是否成功
     */
    boolean transferStatus(Dataset dataset, DatasetStatusEnum to);

    /**
     * 导入用户自定义数据集
     *
     * @param datasetCustomCreateDTO 用户导入自定义数据集请求实体
     * @return Long 数据集ID
     */
    Long importDataset(DatasetCustomCreateDTO datasetCustomCreateDTO);

    /**
     * 初始化版本数据
     *
     * @param dataset 数据集
     */
    @Transactional(rollbackFor = Exception.class)
    void initVersion(Dataset dataset);

}
