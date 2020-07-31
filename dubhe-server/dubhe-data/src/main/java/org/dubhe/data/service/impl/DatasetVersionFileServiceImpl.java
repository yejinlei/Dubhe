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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DataStatusEnum;
import org.dubhe.data.constant.DatatypeEnum;
import org.dubhe.data.constant.FileStatusEnum;
import org.dubhe.data.dao.DatasetVersionFileMapper;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.FileService;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private FileService fileService;

    @Value("${minio.bucketName}")
    private String bucket;

    @Value("${k8s.nfs-root-path}")
    private String nfsRootPath;

    @Value("${k8s.nfs}")
    private String nfsIp;

    @Value("${data.server.userName}")
    private String userName;

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
                datasetVersionFile.setAnnotationStatus(FileStatusEnum.INIT.getValue());
            }
        });
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
    }

    /**
     * 删除数据集下所有版本文件关系
     *
     * @param datasetId 数据集id
     */
    @Override
    public void datasetDelete(Long datasetId) {
        datasetVersionFileMapper.datasetDelete(datasetId);
    }

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
    @Override
    public int updateAnnotationStatus(Long datasetId, String versionName, Set<Long> fileId, Integer sourceStatus, Integer targetStatus) {
        DatasetVersionFile versionFile = StringUtils.isBlank(versionName) ?
                null : getFirstByDatasetIdAndVersionNum(datasetId, versionName, null);
        UpdateWrapper<DatasetVersionFile> updateWrapper = new UpdateWrapper<>();
        DatasetVersionFile datasetVersionFile = new DatasetVersionFile();
        datasetVersionFile.setAnnotationStatus(targetStatus);

        updateWrapper.eq("dataset_id", datasetId);
        updateWrapper.in("file_id", fileId);
        if (StringUtils.isNotEmpty(versionName)) {
            updateWrapper.eq("version_name", versionName);
        }
        if (sourceStatus != null) {
            updateWrapper.eq("annotation_status", sourceStatus);
        }
        if (versionFile != null) {
            datasetVersionFile.setChanged(Constant.CHANGED);
        }
        return baseMapper.update(datasetVersionFile, updateWrapper);
    }

    /**
     * 获取数据集指定版本下文件列表(有效文件)
     *
     * @param datasetId   数据id
     * @param versionName 版本名称
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
     * 获取数据集指定状态下的的文件列表
     *
     * @param datasetId   数据集id
     * @param notInStatus 非指定状态
     * @return List<DatasetVersionFile> 版本文件列表
     */
    @Override
    public List<DatasetVersionFile> getFilesByDatasetIdAndStatus(Long datasetId, Collection<Integer> notInStatus) {
        QueryWrapper<DatasetVersionFile> queryWrapper = new QueryWrapper<>();
        Dataset dataset = datasetService.getOneById(datasetId);
        queryWrapper.eq("dataset_id", datasetId);
        if (dataset != null && StringUtils.isNotEmpty(dataset.getCurrentVersionName())) {
            queryWrapper.eq("version_name", dataset.getCurrentVersionName());
        }
        if (!CollectionUtils.isEmpty(notInStatus)) {
            queryWrapper.notIn("annotation_status", notInStatus);
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 数据集标注状态
     *
     * @param datasetId   数据集id
     * @param status      状态
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    @Override
    public List<DatasetVersionFile> getListByDatasetIdAndAnnotationStatus(Long datasetId, String versionName, Collection<Integer> status) {
        QueryWrapper<DatasetVersionFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id", datasetId);
        queryWrapper.ne("status", DataStatusEnum.DELETE.getValue());
        if (!CollectionUtils.isEmpty(status)) {
            queryWrapper.in("annotation_status", status);
        }
        if (StringUtils.isNotEmpty(versionName)) {
            queryWrapper.eq("version_name", versionName);
        }
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取数据集指定版本第一张图片
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @param status      状态
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
        wrapper.lambda()
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
     * 清除标注信息后数据集状态变为初始状态
     *
     * @param dataset 数据集id
     * @param init    初始状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStatus(Dataset dataset, FileStatusEnum init) {
        UpdateWrapper<DatasetVersionFile> datasetVersionFileUpdateWrapper = new UpdateWrapper();
        datasetVersionFileUpdateWrapper.eq("dataset_id", dataset.getId())
                .eq(dataset.getCurrentVersionName() != null, "version_name", dataset.getCurrentVersionName());
        DatasetVersionFile datasetVersionFile = new DatasetVersionFile();
        datasetVersionFile.setAnnotationStatus(init.getValue());
        datasetVersionFile.setChanged(Constant.CHANGED);
        baseMapper.update(datasetVersionFile, datasetVersionFileUpdateWrapper);
    }

    /**
     * 版本回退
     *
     * @param dataset 数据集
     * @return boolean 数据集是否回退
     */
    @Override
    public boolean rollbackDataset(Dataset dataset) {
        if (isNeedToRollback(dataset)) {
            return doRollback(dataset);
        }
        return true;
    }

    /**
     * 判断当前数据集是否需要回滚
     *
     * @param dataset 需要回滚的数据集
     * @return boolean 数据集是否需要回退
     */
    public boolean isNeedToRollback(Dataset dataset) {
        LambdaQueryWrapper<DatasetVersionFile> isChanged = new LambdaQueryWrapper<DatasetVersionFile>()
                .eq(DatasetVersionFile::getDatasetId, dataset.getId())
                .eq(DatasetVersionFile::getVersionName, dataset.getCurrentVersionName())
                .eq(DatasetVersionFile::getChanged, Constant.CHANGED);
        return baseMapper.selectCount(isChanged) > 0;
    }

    /**
     * 回滚数据集
     *
     * @param dataset 需要回滚的数据集
     * @return boolean 数据集回退是否成功
     */
    public boolean doRollback(Dataset dataset) {
        //回退文件以及文件状态
        datasetVersionFileMapper.rollbackFileAndAnnotationStatus(dataset.getId(), dataset.getCurrentVersionName(), Constant.CHANGED);
        //回退标注文件
        String datasetPath = nfsRootPath + bucket + Constant.DATASET_PATH_NAME + dataset.getId();
        String sourcePath = datasetPath + Constant.VERSION_PATH_NAME + dataset.getCurrentVersionName() + Constant.ANNOTATION_PATH_NAME + Constant.ALL_IN_THE_CURRENT_DIRECTORY;
        String targetPath = datasetPath + Constant.ANNOTATION_PATH_NAME + java.io.File.separator + dataset.getCurrentVersionName() + java.io.File.separator;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.format(Constant.COPY_COMMAND, userName, nfsIp, sourcePath, targetPath)});
            if (!getCopyResult(process)) {
                return false;
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_DATASET, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获取文件对应增强文件列表
     *
     * @param process 命令执行过程
     * @return boolean 返回结果是否为空
     */
    @Override
    public boolean getCopyResult(Process process) {
        InputStreamReader stream = new InputStreamReader(process.getErrorStream());
        BufferedReader reader = new BufferedReader(stream);
        String result = null;
        try {
            while (reader.read() != MagicNumConstant.NEGATIVE_ONE) {
                result = reader.readLine();
                if (result.contains("omitting directory")) {
                    result = null;
                } else {
                    LogUtil.info(LogEnum.BIZ_DATASET, "result is:  /   " + result);
                }
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "getCopyResult is failure:  " + e.getMessage());
        } finally {
            try {
                stream.close();
                reader.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "getCopyResult is failure:  " + e.getMessage());
            }
        }
        return result == null;
    }

    /**
     * 获取当前数据集版本的原始文件数量
     *
     * @param dataset 当前数据集
     * @return: java.lang.Integer 原始文件数量
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
     * 根据当前数据集版本获取图片的数量
     *
     * @param datasetId   数据集id
     * @param versionName 数据集版本名称
     * @return: java.lang.Integer 当前数据集版本获取图片的数量
     */
    @Override
    public Integer getImageCountsByDatasetIdAndVersionName(Long datasetId, String versionName) {
        QueryWrapper<DatasetVersionFile> datasetVersionFileQueryWrapper = new QueryWrapper<>();
        datasetVersionFileQueryWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        return baseMapper.selectCount(datasetVersionFileQueryWrapper);
    }

    @Override
    public List<DatasetVersionFile> queryList(QueryWrapper<DatasetVersionFile> fileQueryWrapper) {
        return list(fileQueryWrapper);
    }

    @Override
    public boolean updateEntity(DatasetVersionFile datasetVersionFile, Wrapper<DatasetVersionFile> updateWrapper) {
        return update(datasetVersionFile, updateWrapper);
    }

}
