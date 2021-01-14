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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.NumberConstant;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DataStatusEnum;
import org.dubhe.data.constant.DatatypeEnum;
import org.dubhe.data.constant.FileTypeEnum;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.dto.DatasetVersionFileDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersion;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.constant.FileStateMachineConstant;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.service.DataFileAnnotationService;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.FileService;
import org.dubhe.dto.StateChangeDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.GeneratorKeyUtil;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 数据集版本文件关系 服务实现类
 * @date 2020-05-14
 */
@Service
public class DatasetVersionFileServiceImpl extends ServiceImpl<DatasetVersionFileMapper, DatasetVersionFile>
        implements DatasetVersionFileService, IService<DatasetVersionFile> {

    @Resource
    private DatasetVersionFileMapper datasetVersionFileMapper;

    @Resource
    @Lazy
    private DatasetService datasetService;

    @Resource
    @Lazy
    private FileService fileService;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    @Autowired
    private DataFileAnnotationService dataFileAnnotationService;


    /**
     * 查询数据集指定版本文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    @Override
    public List<DatasetVersionFile> findByDatasetIdAndVersionName(Long datasetId, String versionName) {
        return datasetVersionFileMapper.findByDatasetIdAndVersionName(datasetId, versionName);
    }

    /**
     * 批量写入数据集版本文件
     *
     * @param data 版本文件列表
     */
    @Override
    public void insertList(List<DatasetVersionFile> data) {
        data.stream().forEach(datasetVersionFile -> {
            if (null == datasetVersionFile.getAnnotationStatus()) {
                datasetVersionFile.setAnnotationStatus(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);
            }
        });
        Queue<Long> dataFileIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_VERSION_FILE, data.size());
        for (DatasetVersionFile datasetVersionFile : data) {
            datasetVersionFile.setId(dataFileIds.poll());
        }
        datasetVersionFileMapper.saveList(data);
    }

    /**
     * 新增关系版本变更
     *
     * @param datasetId     数据集id
     * @param versionSource 源版本
     * @param versionTarget 目标版本
     */
    @Override
    public void newShipVersionNameChange(Long datasetId, String versionSource, String versionTarget) {
        datasetVersionFileMapper.newShipVersionNameChange(datasetId, versionSource, versionTarget);
    }

    /**
     * 版本文件删除标记
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param fileIds     文件id列表
     */
    @Override
    public void deleteShip(Long datasetId, String versionName, List<Long> fileIds) {
        datasetVersionFileMapper.deleteShip(datasetId, versionName, fileIds);
        //删除数据集标注接口
        List<DatasetVersionFile> files = datasetVersionFileMapper.selectByDatasetIdAndVersionNameAndFileIds(datasetId, versionName, fileIds);
        if(!CollectionUtils.isEmpty(files) && Objects.isNull(versionName)){
            List<Long> ids = files.stream().map(a -> a.getId()).collect(Collectors.toList());
            dataFileAnnotationService.deleteBatch(ids);
        }
    }


    /**
     * 数据集版本文件标注状态修改
     *
     * @param datasetId    数据集id
     * @param versionName  版本名称
     * @param fileId       文件id
     * @param sourceStatus 源状态
     * @param targetStatus 目标状态
     * @return int         标注修改的数量
     */
    @Override
    public int updateAnnotationStatus(Long datasetId, String versionName, Set<Long> fileId, Integer sourceStatus, Integer targetStatus) {
        //获取当前版本数据集下第一张图片
        DatasetVersionFile versionFile = StringUtils.isBlank(versionName) ? null : getFirstByDatasetIdAndVersionNum(datasetId, versionName, null);

        UpdateWrapper<DatasetVersionFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("dataset_id", datasetId);
        updateWrapper.in("file_id", fileId);
        DatasetVersionFile datasetVersionFile = new DatasetVersionFile() {{
            setAnnotationStatus(targetStatus);
        }};
        if (StringUtils.isNotEmpty(versionName)) {
            updateWrapper.eq("version_name", versionName);
        }
        if (sourceStatus != null) {
            updateWrapper.eq("annotation_status", sourceStatus);
        }
        if (versionFile != null) {
            datasetVersionFile.setChanged(Constant.CHANGED);
        }
        //=======================嵌入状态机===================================//
        datasetVersionFile = baseMapper.selectOne(updateWrapper);
        if (versionFile != null) {
            datasetVersionFile.setChanged(Constant.CHANGED);
        }
        //创建入参请求体
        StateChangeDTO stateChangeDTO = new StateChangeDTO();
        //创建需要执行事件的方法的传入参数
        Object[] objects = new Object[1];
        objects[0] = datasetVersionFile;
        stateChangeDTO.setObjectParam(objects);
        stateChangeDTO.setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        //执行自动标注算法事件
        if (targetStatus.equals(FileStateCodeConstant.MANUAL_ANNOTATION_FILE_STATE)) {
            stateChangeDTO.setEventMethodName(FileStateMachineConstant.FILE_MANUAL_ANNOTATION_SAVE_EVENT);
        }
        if (targetStatus.equals(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE)) {
            stateChangeDTO.setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
        }
        //=======================嵌入状态机===================================//
        StateMachineUtil.stateChange(stateChangeDTO);
        return 0;
    }

    /**
     * 获取数据集指定版本下文件列表(有效文件)
     *
     * @param datasetId                 数据id
     * @param versionName               版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    @Override
    public List<DatasetVersionFile> getFilesByDatasetIdAndVersionName(Long datasetId, String versionName) {
        QueryWrapper<DatasetVersionFile> queryWrapper = new QueryWrapper();
        queryWrapper.eq("dataset_id", datasetId);
        if (StringUtils.isNotEmpty(versionName)) {
            queryWrapper.eq("version_name", versionName);
        }
        queryWrapper.notIn("status", MagicNumConstant.ONE);
        return baseMapper.selectList(queryWrapper);
    }


    /**
     * 数据集标注状态
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param status      状态
     * @param offset      偏移量
     * @param limit       页容量
     * @param order       排序方式
     * @return 数据集版本文件列表
     */
    @Override
    public LinkedList<DatasetVersionFileDTO> getListByDatasetIdAndAnnotationStatus(Long datasetId, String versionName, Integer status, Long offset, Integer limit, String orderByName, String order, Long labelId) {
        order =  Objects.isNull(order)? "asc": order;
        LinkedList<Integer> versionFileIdList = datasetVersionFileMapper.getIdByDatasetIdAndAnnotationStatus(
                datasetId, versionName, FileTypeEnum.getStatus(status == null
                ? NumberConstant.NUMBER_0 : status), orderByName, offset, limit,order,labelId);
        LinkedList<DatasetVersionFileDTO> listByDatasetIdAndAnnotationStatus = null;
        if(!CollectionUtils.isEmpty(versionFileIdList)){
            listByDatasetIdAndAnnotationStatus = datasetVersionFileMapper.getListByDatasetIdAndAnnotationStatus(
                    datasetId, orderByName, versionFileIdList, order);
        }
        return listByDatasetIdAndAnnotationStatus;
    }

    /**
     * 获取数据集指定版本第一张图片
     *
     * @param datasetId           数据集id
     * @param versionName         版本名称
     * @param status              状态
     * @return DatasetVersionFile 版本首张图片
     */
    @Override
    public DatasetVersionFile getFirstByDatasetIdAndVersionNum(Long datasetId, String versionName, Collection<Integer> status) {
        QueryWrapper<DatasetVersionFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id", datasetId);
        if (!CollectionUtils.isEmpty(status)) {
            queryWrapper.in("annotation_status", status);
        }
        if (StringUtils.isNotEmpty(versionName)) {
            queryWrapper.eq("version_name", versionName);
        }
        queryWrapper.orderByAsc("id");
        queryWrapper.last(" limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据版本列表中的文件id获取文件列表
     *
     * @param datasetVersionFiles 版本文件中间表列表
     * @return List<File> 文件列表
     */
    @Override
    public List<File> getFileListByVersionFileList(List<DatasetVersionFile> datasetVersionFiles) {
        QueryWrapper<org.dubhe.data.domain.entity.File> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(org.dubhe.data.domain.entity.File::getDatasetId, datasetVersionFiles.get(0).getDatasetId())
                .in(org.dubhe.data.domain.entity.File::getId, datasetVersionFiles.stream().map(DatasetVersionFile::getFileId).collect(Collectors.toList()))
                .eq(org.dubhe.data.domain.entity.File::getFileType, DatatypeEnum.IMAGE)
                .orderByAsc(org.dubhe.data.domain.entity.File::getId);
        return fileService.listFile(wrapper);
    }

    /**
     * 通过数据集和版本获取文件状态
     *
     * @param datasetId   数据集id
     * @param versionName 版本版本名称
     * @return List<File> 文件状态列表
     */
    @Override
    public List<Integer> getFileStatusListByDatasetAndVersion(Long datasetId, String versionName) {
        if (datasetId == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "datasetId isEmpty");
            return null;
        }
        return datasetVersionFileMapper.findFileStatusListByDatasetAndVersion(datasetId, versionName);
    }

    /**
     * 版本回退
     *
     * @param dataset 数据集对象
     */
    @Override
    public void rollbackDataset(Dataset dataset) {
        if (StringUtils.isNoneBlank(dataset.getCurrentVersionName()) && isNeedToRollback(dataset)) {
            doRollback(dataset);
        }
    }

    /**
     * 判断当前数据集是否需要回滚
     *
     * @param dataset        需要回滚的数据集
     * @return boolean 数据集是否需要回退
     */
    @Override
    public boolean isNeedToRollback(Dataset dataset) {
        LambdaQueryWrapper<DatasetVersionFile> isChanged = new LambdaQueryWrapper<DatasetVersionFile>()
                .eq(DatasetVersionFile::getDatasetId, dataset.getId())
                .eq(DatasetVersionFile::getVersionName, dataset.getCurrentVersionName())
                .eq(DatasetVersionFile::getChanged, Constant.CHANGED);
        return baseMapper.selectCount(isChanged) > 0;
    }


    /**
     * 根据当前数据集和当前版本号修改数据集是否改变
     *
     * @param id           数据集id
     * @param versionName  版本号
     */
    @Override
    public void updateChanged(Long id, String versionName) {
        baseMapper.updateChanged(id,versionName);
    }

    /**
     * 回滚数据集
     *
     * @param dataset 需要回滚的数据集
     * @return boolean 数据集回退是否成功
     */
    public void doRollback(Dataset dataset) {
        //文件状态为删除新增的和标记为改变的
        datasetVersionFileMapper.rollbackFileAndAnnotationStatus(dataset.getId(), dataset.getCurrentVersionName(), Constant.CHANGED);
    }

    /**
     * 获取当前数据集版本的原始文件数量
     *
     * @param dataset    当前数据集
     * @return 原始文件数量
     */
    @Override
    public Integer getSourceFileCount(Dataset dataset) {
        return datasetVersionFileMapper.getSourceFileCount(dataset);
    }

    /**
     * 获取可以增强的文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 增强文件列表
     */
    @Override
    public List<DatasetVersionFile> getNeedEnhanceFilesByDatasetIdAndVersionName(Long datasetId, String versionName) {
        return baseMapper.getNeedEnhanceFilesByDatasetIdAndVersionName(datasetId, versionName);
    }

    /**
     * 获取文件对应增强文件列表
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param fileId      文件id
     * @return List<File> 增强文件列表
     */
    @Override
    public List<File> getEnhanceFileList(Long datasetId, String versionName, Long fileId) {
        return baseMapper.getEnhanceFileList(datasetId, versionName, fileId);
    }

    /**
     * 获取增强文件数量
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return Integer    当前版本增强文件数量
     */
    @Override
    public Integer getEnhanceFileCount(Long datasetId, String versionName) {
        return baseMapper.getEnhanceFileCount(datasetId, versionName);
    }

    /**
     * 根据当前数据集版本获取图片的数量
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return 当前数据集版本获取图片的数量
     */
    @Override
    public Integer getImageCountsByDatasetIdAndVersionName(Long datasetId, String versionName) {
        QueryWrapper<DatasetVersionFile> datasetVersionFileQueryWrapper = new QueryWrapper<>();
        datasetVersionFileQueryWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        return baseMapper.selectCount(datasetVersionFileQueryWrapper);
    }



    /**
     * 分页获取数据集版本文件数据
     *
     * @param offset       偏移量
     * @param pageSize     页容量
     * @param datasetId    数据集ID
     * @param versionName  数据集版本名称
     * @return 数据集版本文件列表
     */
    @Override
    public List<DatasetVersionFile> getPages(int offset, int pageSize, Long datasetId, String versionName) {
        QueryWrapper<DatasetVersionFile> datasetVersionFileQueryWrapper = new QueryWrapper<>();
        datasetVersionFileQueryWrapper.eq("dataset_id", datasetId);
        if (StringUtils.isNotEmpty(versionName)) {
            datasetVersionFileQueryWrapper.eq("version_name", versionName);
        }
        datasetVersionFileQueryWrapper.last("limit " + offset + "," + pageSize);
        return baseMapper.selectList(datasetVersionFileQueryWrapper);
    }

    /**
     * 查询当前版本的数据集信息
     *
     * @param datasetIds
     * @return 数据集当前版本文件列表
     */
    @Override
    public List<DatasetVersionFile> listDatasetVersionFileByDatasetIds(List<Long> datasetIds) {
        return baseMapper.listDatasetVersionFileByDatasetIds(datasetIds);
    }

    /**
     * 获取数据集当前版本文件数量
     *
     * @param queryWrapper 查询条件
     * @return Integer 数据集当前版本文件数量
     */
    @Override
    public Integer getFileCountByDatasetIdAndVersion(LambdaQueryWrapper<DatasetVersionFile> queryWrapper) {
        return baseMapper.selectCount(queryWrapper);
    }

    /**
     * 获取数据集文件状态统计数据
     *
     * @param datasetId   数据集ID
     * @param versionName 数据集版本名称
     * @return Map 数据集当前版本文件数量
     */
    @Override
    public Map<Integer, Integer> getDatasetVersionFileCount(Long datasetId, String versionName) {
        return baseMapper.getDatasetVersionFileCount(datasetId, versionName);
    }

    /**
     * 获取数据集文件数量统计数据
     *
     * @param datasetVersion 数据集版本
     * @return 数据集文件统计
     */
    @Override
    public Integer selectDatasetVersionFileCount(DatasetVersion datasetVersion){
        return baseMapper.selectCount(new LambdaQueryWrapper<DatasetVersionFile>(){{
            eq(DatasetVersionFile::getDatasetId,datasetVersion.getDatasetId());
            eq(DatasetVersionFile::getVersionName,datasetVersion.getVersionName());
            ne(DatasetVersionFile::getStatus,NumberConstant.NUMBER_0);
        }});
    }

    /**
     * 获取offset
     *
     * @param datasetId 数据集id
     * @param fileId    文件id
     * @param type      数据集类型
     * @return Integer 获取到offset
     */
    @Override
    public Integer getOffset(Long fileId, Long datasetId, Integer type) {
        Dataset dataset = datasetService.getOneById(datasetId);
        DatasetVersionFile datasetVersionFile = baseMapper.selectOne(new LambdaQueryWrapper<DatasetVersionFile>() {{
            eq(DatasetVersionFile::getDatasetId, dataset.getId());
            if (StringUtils.isBlank(dataset.getCurrentVersionName())) {
                isNull(DatasetVersionFile::getVersionName);
            } else {
                eq(DatasetVersionFile::getVersionName, datasetService.getOneById(datasetId).getCurrentVersionName());
            }
            eq(DatasetVersionFile::getFileId, fileId);
        }});
        return baseMapper.selectCount(
                new LambdaQueryWrapper<DatasetVersionFile>() {{
                    eq(DatasetVersionFile::getDatasetId, datasetId);
                    in(DatasetVersionFile::getStatus, DataStatusEnum.ADD.getValue(), DataStatusEnum.NORMAL.getValue());
                    if (type != null) {
                        in(DatasetVersionFile::getAnnotationStatus, FileTypeEnum.getStatus(type));
                    }
                    if (StringUtils.isBlank(dataset.getCurrentVersionName())) {
                        isNull(DatasetVersionFile::getVersionName);
                    } else {
                        eq(DatasetVersionFile::getVersionName, datasetService.getOneById(datasetId).getCurrentVersionName());
                    }
                    le(DatasetVersionFile::getId, datasetVersionFile.getId());
                }}
        );
    }


    /**
     * 条件查询数据集文件表的数据量
     *
     * @param eq 条件
     * @return long 版本文件数量
     */
    @Override
    public long selectCount(LambdaQueryWrapper<DatasetVersionFile> eq) {
        return baseMapper.selectCount(eq);
    }

    /**
     * 根据数据集id,版本查询状态为删除的数据版本文件中间表
     *
     * @param id                 数据集Id
     * @param currentVersionName 数据集版本
     * @return DatasetVersionFile Dataset版本文件关系表
     */
    @Override
    public List<DatasetVersionFile> findStatusByDatasetIdAndVersionName(Long id, String currentVersionName) {
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileMapper.findStatusByDatasetIdAndVersionName(id, currentVersionName);
        return datasetVersionFiles;
    }

    /**
     * 获取单个的文件版本信息
     *
     * @param datasetId    数据集ID
     * @param versionName  版本名称
     * @param fileId       文件ID
     * @return 文件版本信息
     */
    @Override
    public DatasetVersionFile getDatasetVersionFile(Long datasetId,String versionName,Long fileId){
        return baseMapper.selectOne(new LambdaQueryWrapper<DatasetVersionFile>(){{
            eq(DatasetVersionFile::getDatasetId,datasetId);
            if (StringUtils.isBlank(versionName)){
                isNull(DatasetVersionFile::getVersionName);
            }else {
                eq(DatasetVersionFile::getVersionName,versionName);
            }
            eq(DatasetVersionFile::getFileId,fileId);
        }});
    }

    /**
     * 重新标注操作更新文件状态
     *
     * @param datasetId  数据集ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAnnotating(Long datasetId) {
        Dataset dataset = datasetService.getOneById(datasetId);
        datasetVersionFileMapper.update(
                new DatasetVersionFile() {{
                    setAnnotationStatus(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE);
                    setChanged(Constant.CHANGED);
                }},
                new UpdateWrapper<DatasetVersionFile>()
                        .lambda()
                        .eq(DatasetVersionFile::getDatasetId, dataset.getId())
                        .eq(dataset.getCurrentVersionName() != null, DatasetVersionFile::getVersionName, dataset.getCurrentVersionName())
        );
    }


    /**
     * 获取数据集版本文件ID
     *
     * @param id                    数据集ID
     * @param currentVersionName    版本名称
     * @param fileIds               文件id
     * @return 获取数据集版本文件
     */
    @Override
    public List<DatasetVersionFile> getVersionFileByDatasetAndFile(Long id, String currentVersionName, Set<Long> fileIds) {
        return datasetVersionFileMapper.selectList(new LambdaQueryWrapper<DatasetVersionFile>() {{
                                                       eq(DatasetVersionFile::getDatasetId, id);
                                                       if (StringUtils.isBlank(currentVersionName)) {
                                                           isNull(DatasetVersionFile::getVersionName);
                                                       } else {
                                                           eq(DatasetVersionFile::getVersionName, currentVersionName);
                                                       }
                                                       in(DatasetVersionFile::getFileId, fileIds);
                                                   }}
        );
    }

    /**
     * 修改文件状态
     *
     * @param datasetVersionFile 数据集版本文件实体
     */
    @Override
    public void updateStatusById(DatasetVersionFile datasetVersionFile) {
        datasetVersionFileMapper.updateAnnotationStatusById(datasetVersionFile.getAnnotationStatus(),datasetVersionFile.getDatasetId(),datasetVersionFile.getId());
    }

}