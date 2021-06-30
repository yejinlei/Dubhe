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

package org.dubhe.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.data.dao.DatasetLabelMapper;
import org.dubhe.data.dao.LabelMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetLabel;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.DatasetLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
     * @param datasetId             数据集id
     * @return List<DatasetLabel>   标签列表
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
     * @param rels          数据集标签
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
     * @param rel       数据集标签
     * @return boolean  是否存在标签
     */
    public boolean exist(DatasetLabel rel) {
        if (rel == null || rel.getDatasetId() == null || rel.getLabelId() == null) {
            return false;
        }
        QueryWrapper<DatasetLabel> datasetLabelQueryWrapper = new QueryWrapper<>();
        datasetLabelQueryWrapper
                .lambda()
                .eq(DatasetLabel::getDatasetId, rel.getDatasetId())
                .eq(DatasetLabel::getLabelId, rel.getLabelId());
        return getBaseMapper().selectCount(datasetLabelQueryWrapper) > MagicNumConstant.ZERO;
    }

    /**
     * 根据数据集ID删除数据集标签关联数据
     *
     * @param datasetId 数据集id
     * @return int      执行次数
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
     * @param datasetId     数据集ID
     * @return List<label>  数据集下所有标签列表
     */
    @Override
    public List<Label> listLabelByDatasetId(Long datasetId) {
        return labelMapper.listLabelByDatasetId(datasetId);
    }

    /**
     * 批量保存标签
     *
     * @param datasetLabels 数据集标签信息
     */
    @Override
    public void saveList(List<DatasetLabel> datasetLabels) {
        saveBatch(datasetLabels);
    }

    /**
     * 查询标签是否正在使用
     *
     * @param labels    需要查询的标签
     * @return Boolean  标签是否使用
     */
    @Override
    public Boolean isLabelGroupInUse(List<Label> labels) {
        List<Long> labelIds = new ArrayList<>();
        labels.forEach(label -> labelIds.add(label.getId()));
        QueryWrapper<DatasetLabel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(DatasetLabel::getLabelId, labelIds);
        return getBaseMapper().selectCount(queryWrapper) > MagicNumConstant.ZERO;
    }


    /**
     * 新增数据集标签数据
     *
     * @param datasetLabel 数据标签实体
     */
    @Override
    public void insert(DatasetLabel datasetLabel) {
        super.save(datasetLabel);
    }

    /**
     * 删除数据集标签
     *
     * @param datasetId         数据集id
     * @param deleteFlag        删除标识
     */
    @Override
    public void updateStatusByDatasetId(Long datasetId, Boolean deleteFlag) {
        baseMapper.updateStatusByDatasetId(datasetId, deleteFlag);
    }


    /**
     * 备份数据集标签关系数据
     * @param originDatasetId   原数据集ID
     * @param targetDateset     目标数据集实体
     */
    @Override
    public void backupDatasetLabelDataByDatasetId(Long originDatasetId, Dataset targetDateset) {
        List<DatasetLabel> datasetLabels = baseMapper.selectList(new LambdaQueryWrapper<DatasetLabel>().eq(DatasetLabel::getDatasetId, originDatasetId));
        if (!CollectionUtils.isEmpty(datasetLabels)) {
            datasetLabels.forEach(a -> {
                a.setDatasetId(targetDateset.getId());
                a.setCreateUserId(targetDateset.getCreateUserId());
                a.setUpdateUserId(a.getCreateUserId());
            });
            baseMapper.insertBatch(datasetLabels);
        }
    }
}
