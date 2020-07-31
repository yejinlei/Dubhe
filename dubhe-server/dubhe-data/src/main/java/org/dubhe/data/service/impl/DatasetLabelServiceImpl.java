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

package org.dubhe.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.dao.DatasetLabelMapper;
import org.dubhe.data.dao.LabelMapper;
import org.dubhe.data.domain.entity.DatasetLabel;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.DatasetLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 数据集标签关联关系 服务实现类
 * @date 2020-04-01
 */
@Service
public class DatasetLabelServiceImpl extends ServiceImpl<DatasetLabelMapper, DatasetLabel> implements DatasetLabelService {

    @Autowired
    private LabelMapper labelMapper;

    /**
     * 标签列表
     *
     * @param datasetId 数据集id
     * @return List<DatasetLabel> 标签列表
     */
    @Override
    public List<DatasetLabel> list(Long datasetId) {
        QueryWrapper<DatasetLabel> q = new QueryWrapper<>();
        q.lambda().eq(DatasetLabel::getDatasetId, datasetId);
        return getBaseMapper().selectList(q);
    }

    /**
     * 过滤存在的标签
     *
     * @param rels 数据集标签
     * @return DatasetLabel 过滤后的标签
     */
    @Override
    public List<DatasetLabel> filterExist(List<DatasetLabel> rels) {
        if (CollectionUtils.isEmpty(rels)) {
            return new LinkedList<>();
        }
        return rels.stream().filter(i -> !exist(i)).collect(Collectors.toList());
    }

    /**
     * rel为空时return false,请调用方自行斟酌过滤
     *
     * @param rel 数据集标签
     * @return boolean 是否存在标签
     */
    public boolean exist(DatasetLabel rel) {
        if (rel == null || rel.getDatasetId() == null || rel.getLabelId() == null) {
            return false;
        }
        QueryWrapper<DatasetLabel> datasetLabelQueryWrapper = new QueryWrapper<>();
        datasetLabelQueryWrapper.lambda().eq(DatasetLabel::getDatasetId, rel.getDatasetId()).eq(DatasetLabel::getLabelId, rel.getLabelId());
        return getBaseMapper().selectCount(datasetLabelQueryWrapper) > MagicNumConstant.ZERO;
    }

    /**
     * 删除标签
     *
     * @param datasetId 数据集id
     * @return int 执行次数
     */
    @Override
    public int del(Long datasetId) {
        QueryWrapper<DatasetLabel> datasetLabelQueryWrapper = new QueryWrapper<>();
        datasetLabelQueryWrapper.lambda().eq(DatasetLabel::getDatasetId, datasetId);
        return getBaseMapper().delete(datasetLabelQueryWrapper);
    }

    /**
     * 获取数据集下所有标签
     *
     * @param datasetId 数据集ID
     * @return List<label> 数据集下所有标签列表
     */
    @Override
    public List<Label> listLabelByDatasetId(Long datasetId) {
        return labelMapper.listLabelByDatasetId(datasetId);
    }

    @Override
    public void saveList(List<DatasetLabel> datasetLabels) {
        saveBatch(datasetLabels);
    }

}
