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
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.DateUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.dao.DataFileAnnotationMapper;
import org.dubhe.data.domain.entity.DataFileAnnotation;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.service.DataFileAnnotationService;
import org.dubhe.data.util.FileUtil;
import org.dubhe.data.util.GeneratorKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @description 数据文件标注服务实现类
 * @date 2021-01-06
 */
@Service
public class DataFileAnnotationServiceImpl extends ServiceImpl<DataFileAnnotationMapper, DataFileAnnotation> implements DataFileAnnotationService {

    @Autowired
    private DataFileAnnotationMapper dataFileAnnotationMapper;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private DatasetVersionFileServiceImpl datasetVersionFileServiceImpl;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    /**
     * 批量保存数据文件标注数据
     *
     * @param dataFileAnnotations   数据文件标注集合
     */
    @Override
    public void insertDataFileBatch(List<DataFileAnnotation> dataFileAnnotations) {
        if(!CollectionUtils.isEmpty(dataFileAnnotations)){
            dataFileAnnotationMapper.insertBatch(dataFileAnnotations);
        }
    }


    /**
     * 根据版本文件ID批量删除标注数据
     *
     * @param versionIds    版本文件IDS
     */
    @Override
    public void deleteBatch(Long datasetId,List<Long> versionIds) {
        dataFileAnnotationMapper.deleteBatch(datasetId,versionIds);
    }


    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签ID列表
     */
    @Override
    public List<Long> findInfoByVersionId(Long datasetId,Long versionFileId) {
        return dataFileAnnotationMapper.findInfoByVersionId(datasetId,versionFileId);
    }

    /**
     * 批量新增标注文件数据
     * @param datasetId     数据集ID
     * @param versionFileId  版本文件ID
     * @param fileLabelIds   标注标签Ids
     * @param fileName       文件名称
     */
    @Override
    public void insertAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds, String fileName) {

        List<DataFileAnnotation> dataFileAnnotations = fileLabelIds.stream().map(labelId -> {
            DataFileAnnotation dataFileAnnotation = new DataFileAnnotation();
            dataFileAnnotation.setDatasetId(datasetId);
            dataFileAnnotation.setVersionFileId(versionFileId);
            dataFileAnnotation.setLabelId(labelId);
            dataFileAnnotation.setPrediction(1D);
            dataFileAnnotation.setCreateUserId(JwtUtils.getCurUserId());
            dataFileAnnotation.setUpdateUserId(JwtUtils.getCurUserId());
            dataFileAnnotation.setCreateTime(DateUtil.getCurrentTimestamp());
            dataFileAnnotation.setUpdateTime(DateUtil.getCurrentTimestamp());
            dataFileAnnotation.setStatus(MagicNumConstant.ZERO);
            dataFileAnnotation.setInvariable(MagicNumConstant.ZERO);
            dataFileAnnotation.setFileName(fileName);
            dataFileAnnotation.setDeleted(false);
            return dataFileAnnotation;
        }).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(dataFileAnnotations)){
            Queue<Long> dataFileAnnotionIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_FILE_ANNOTATION, dataFileAnnotations.size());
            for (DataFileAnnotation dataFileAnnotation : dataFileAnnotations) {
                dataFileAnnotation.setId(dataFileAnnotionIds.poll());
            }
            dataFileAnnotationMapper.insertBatch(dataFileAnnotations);
        }
    }



    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    @Override
    public void deleteAnnotationFileByVersionIdAndLabelIds(Long datasetId, Long versionFileId, List<Long> fileLabelIds) {
        dataFileAnnotationMapper.deleteAnnotationFileByVersionIdAndLabelIds(datasetId,versionFileId,fileLabelIds);
    }


    /**
     * 根据版本文件 id 修改状态
     *
     * @param ids        版本文件ID
     * @param deleteFlag 删除标识
     */
    @Override
    public void updateStatusByVersionIds(Long datasetId,List<Long> ids, boolean deleteFlag) {
        dataFileAnnotationMapper.updateStatusByVersionIds(datasetId,ids,deleteFlag);
    }


    /**
     * 文本数据集需备份文本标注数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionFiles  版本文件
     */
    @Override
    public void backupDataFileAnnotationDataByDatasetId(Dataset originDataset, Dataset targetDataset, List<DatasetVersionFile> versionFiles) {
        LogUtil.info(LogEnum.BIZ_DATASET, "文本数据集需备份文本标注数据1");
        Long curUserId = userContextService.getCurUserId();
        List<DatasetVersionFile> filesByDatasetIdAndVersionName = datasetVersionFileServiceImpl.getFilesByDatasetIdAndVersionName(originDataset.getId(), originDataset.getCurrentVersionName());
        List<Long> objects = new ArrayList<>();
        for (DatasetVersionFile versionFile : filesByDatasetIdAndVersionName){
            objects.add(versionFile.getId());
        }
        Map<String, Long> nameMap = versionFiles.stream().collect(Collectors.toMap(DatasetVersionFile::getFileName, DatasetVersionFile::getId));
        List<DataFileAnnotation> dataFileAnnotations = baseMapper.selectList(new LambdaUpdateWrapper<DataFileAnnotation>().eq(DataFileAnnotation::getDatasetId, originDataset.getId())
                                                                                    .in(DataFileAnnotation::getVersionFileId,objects));
        LogUtil.info(LogEnum.BIZ_DATASET, "文本数据集需备份文本标注数据2");
        if(CollectionUtil.isNotEmpty(dataFileAnnotations)){
            LogUtil.info(LogEnum.BIZ_DATASET, "文本数据集需备份文本标注数据3");
            List<DataFileAnnotation> fileAnnotations = dataFileAnnotations.stream().map(a -> {
                DataFileAnnotation annotation = DataFileAnnotation.builder()
                        .datasetId(targetDataset.getId())
                        .fileName(a.getFileName())
                        .labelId(a.getLabelId())
                        .prediction(a.getPrediction())
                        .versionFileId(nameMap.get(a.getFileName()))
                        .build();
                annotation.setDeleted(false);
                annotation.setCreateUserId(curUserId);
                annotation.setUpdateUserId(annotation.getCreateUserId());
                return annotation;
            }).collect(Collectors.toList());
            LogUtil.info(LogEnum.BIZ_DATASET, "文本数据集需备份文本标注数据4");
            if(!CollectionUtils.isEmpty(fileAnnotations)){
                Queue<Long> dataFileAnnotionIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_FILE_ANNOTATION, fileAnnotations.size());
                for (DataFileAnnotation dataFileAnnotation : fileAnnotations) {
                    dataFileAnnotation.setId(dataFileAnnotionIds.poll());
                    dataFileAnnotation.setStatus(MagicNumConstant.ZERO);
                    dataFileAnnotation.setInvariable(MagicNumConstant.ZERO);
                }
                LogUtil.info(LogEnum.BIZ_DATASET, "文本数据集需备份文本标注数据5");
            }
            List<List<DataFileAnnotation>> splitAnnotations = CollectionUtil.split(fileAnnotations, MagicNumConstant.FOUR_THOUSAND);
            splitAnnotations.forEach(splitAnnotation->baseMapper.insertBatch(splitAnnotation));
        }
    }

    /**
     * 根据标签Id,数据集Id,数据集文件版本id查询标签标注信息
     *
     * @param labelId           标签id
     * @param datasetId         数据集Id
     * @return DataFileAnnotation
     */
    @Override
    public List<DataFileAnnotation> getLabelIdByDatasetIdAndVersionId(Long[] labelId, Long datasetId, Long offset, Integer limit,String versionName) {
        return baseMapper.getLabelIdByDatasetIdAndVersionId(labelId,datasetId,offset,limit, versionName);
    }

    /**
     * 获取发布时版本标注信息
     *
     * @param datasetId         数据集ID
     * @param versionName       版本名称
     * @param status            版本文件状态
     * @return List<DataFileAnnotation>   标注信息
     */
    @Override
    public List<DataFileAnnotation> getAnnotationByVersion(Long datasetId, String versionName, Integer status) {
        return baseMapper.getAnnotationByVersion(datasetId, versionName, status);
    }

    /**
     *  更新标注信息
     *
     * @param dataFileAnnotations 标注信息列表
     */
    @Override
    public void updateDataFileAnnotations(List<DataFileAnnotation> dataFileAnnotations) {
        dataFileAnnotations.forEach(dataFileAnnotation -> baseMapper.updateDataFileAnnotations(dataFileAnnotation.getId(),
                dataFileAnnotation.getStatus(),dataFileAnnotation.getInvariable(),dataFileAnnotation.getDatasetId(),
                dataFileAnnotation.getVersionFileId()));
    }

    /**
     * 回退标注信息
     *
     * @param datasetId             数据集ID
     * @param versionSource         源版本
     * @param status                标注状态
     * @param invariable            是否为版本标注
     */
    @Override
    public void rollbackAnnotation(Long datasetId, String versionSource, Integer status, Integer invariable){
        baseMapper.rollbackAnnotation(datasetId, versionSource, status, invariable);
    }

    /**
     * 筛选查询数量
     *
     * @param datasetId         数据集id
     * @param versionName       版本名称
     * @param labelId           标签id
     */
    @Override
    public Long selectDetectionCount(Long datasetId, String versionName, Long[] labelId) {
        return baseMapper.selectDetectionCount(datasetId, versionName, labelId);
    }
}
