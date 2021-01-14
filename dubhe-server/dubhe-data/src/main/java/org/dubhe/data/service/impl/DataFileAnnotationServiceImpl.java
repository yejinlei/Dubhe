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

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.data.dao.DataFileAnnotationMapper;
import org.dubhe.data.domain.entity.DataFileAnnotation;
import org.dubhe.data.service.DataFileAnnotationService;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.utils.DateUtil;
import org.dubhe.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 数据文件标注服务实现类
 * @date 2021-01-06
 */
@Service
public class DataFileAnnotationServiceImpl extends ServiceImpl<DataFileAnnotationMapper, DataFileAnnotation> implements DataFileAnnotationService {

    @Autowired
    private DataFileAnnotationMapper dataFileAnnotationMapper;

    /**
     * 批量保存数据文件标注数据
     *
     * @param dataFileAnnotations   数据文件标注集合
     */
    @Override
    public void insertDataFileBatch(List<DataFileAnnotation> dataFileAnnotations) {
        if(!CollectionUtil.isEmpty(dataFileAnnotations)){
            dataFileAnnotations.forEach(info->{
                dataFileAnnotationMapper.insert(info);
            });

        }
    }


    /**
     * 根据版本文件ID批量删除标注数据
     *
     * @param versionIds    版本文件IDS
     */
    @Override
    public void deleteBatch(List<Long> versionIds) {
        dataFileAnnotationMapper.deleteBatch(versionIds);
    }


    /**
     * 删除已标注的文本标签
     *
     * @param wrapper 数据文件注释标签删除条件
     */
    @Override
    public void deleteFileAnnotationLabel(LambdaUpdateWrapper<DataFileAnnotation> wrapper) {
        dataFileAnnotationMapper.delete(wrapper);
    }

    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签ID列表
     */
    @Override
    public List<Long> findInfoByVersionId(Long versionFileId) {
        return dataFileAnnotationMapper.findInfoByVersionId(versionFileId);
    }

    /**
     * 批量新增标注文件数据
     *
     * @param datasetId     数据集ID
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    @Override
    public void insertAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds) {

        UserDTO currentUserDto = JwtUtils.getCurrentUserDto();
        List<DataFileAnnotation> dataFileAnnotations = fileLabelIds.stream().map(labelId -> {
            DataFileAnnotation dataFileAnnotation = new DataFileAnnotation();
            dataFileAnnotation.setDatasetId(datasetId);
            dataFileAnnotation.setVersionFileId(versionFileId);
            dataFileAnnotation.setLabelId(labelId);
            dataFileAnnotation.setPrediction(1D);
            dataFileAnnotation.setCreateUserId(currentUserDto.getId());
            dataFileAnnotation.setUpdateUserId(currentUserDto.getId());
            dataFileAnnotation.setCreateTime(DateUtil.getCurrentTimestamp());
            dataFileAnnotation.setUpdateTime(DateUtil.getCurrentTimestamp());
            dataFileAnnotation.setDeleted(false);
            return dataFileAnnotation;
        }).collect(Collectors.toList());
        dataFileAnnotationMapper.insertBatch(dataFileAnnotations);
    }

    /**
     * 批量修改标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    @Override
    public void updateAnnotationFileByVersionIdAndLabelIds(Long versionFileId, List<Long> fileLabelIds) {
        dataFileAnnotationMapper.updateAnnotationFileByVersionIdAndLabelIds(versionFileId,fileLabelIds,1D);
    }

    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    @Override
    public void deleteAnnotationFileByVersionIdAndLabelIds(Long versionFileId, List<Long> fileLabelIds) {
        dataFileAnnotationMapper.deleteAnnotationFileByVersionIdAndLabelIds(versionFileId,fileLabelIds);
    }
}
