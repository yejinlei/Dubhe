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
package org.dubhe.datasetutil.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.datasetutil.common.constant.DataStateCodeConstant;
import org.dubhe.datasetutil.dao.DatasetMapper;
import org.dubhe.datasetutil.domain.entity.Dataset;
import org.dubhe.datasetutil.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @description 数据集 服务实现类
 * @date 2020-09-17
 */
@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * 根据数据集Id查询创建人Id
     *
     * @param datasetId 数据集Id
     * @return dataset 数据集合
     */
    @Override
    public Dataset findCreateUserIdById(Long datasetId) {
        return baseMapper.selectById(datasetId);
    }

    /**
     * 根据ID 查询数据集
     *
     * @param datasetId 数据集ID
     * @return Dataset 数据集
     */
    @Override
    public Dataset findDatasetById(Long datasetId) {
        return datasetMapper.findDatasetById(datasetId);
    }

    @Override
    public Dataset queryDatasetById(Long datasetId) {
        return baseMapper.selectById(datasetId);
    }

    /**
     * 更新数据集状态
     *
     * @param dataset 数据集
     * @return int 数量
     */
    @Override
    public int updateDatasetStatus(Dataset dataset) {
        dataset.setStatus(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
        return datasetMapper.updateById(dataset);
    }

    /**
     * 查询数据集数据标签数量
     *
     * @param datasetId 数据集ID
     * @return int 数量
     */
    @Override
    public int findDataLabelById(Long datasetId) {
        return datasetMapper.findDataLabelById(datasetId);
    }

    /**
     * 查询数据集文件数量
     *
     * @param datasetId 数据集ID
     * @return int 数量
     */
    @Override
    public int findDataFileById(Long datasetId) {
        return datasetMapper.findDataFileById(datasetId);
    }

    /**
     * 根据ID 查询数据集
     *
     * @param datasetId 数据集ID
     * @return Dataset 数据集
     */
    @Override
    public Dataset findDatasetByIdNormal(Long datasetId) {
        return datasetMapper.findDatasetByIdNormal(datasetId);
    }

    /**
     * 新增数据集
     *
     * @param insertSql sql语句
     */
    @Override
    public void saveBatch(List<String> insertSql) {
        if(!CollectionUtils.isEmpty(insertSql)){
            insertSql.forEach(sql->{
                if(!Objects.isNull(sql)){
                    baseMapper.saveBatch(sql);
                }
            });
        }

    }

    /**
     * 删除数据集通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    @Override
    public void deleteDatasetById(long datasetId) {
        baseMapper.deleteDatasetById(datasetId);
    }

    /**
     * 更新数据集状态
     *
     * @param dataset 数据集
     */
    @Override
    public void updateDatasetStatusIsImport(Dataset dataset) {
        dataset.setStatus(DataStateCodeConstant.IN_THE_IMPORT_STATE);
        datasetMapper.updateById(dataset);
    }

    /**
     * 更新数据集
     *
     * @param dataset 数据集信息
     * @return int 数量
     */
    @Override
    public int updateDataset(Dataset dataset) {
        return datasetMapper.updateById(dataset);
    }

}
