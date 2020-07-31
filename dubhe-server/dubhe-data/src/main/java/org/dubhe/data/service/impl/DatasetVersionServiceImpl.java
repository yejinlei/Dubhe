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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetVersionMapper;
import org.dubhe.data.domain.dto.ConversionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionDeleteDTO;
import org.dubhe.data.domain.dto.DatasetVersionQueryCriteriaDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersion;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.vo.DatasetVersionCriteriaVO;
import org.dubhe.data.domain.vo.DatasetVersionVO;
import org.dubhe.data.domain.vo.ProgressVO;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.DatasetVersionService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.http.ConversionHttpService;
import org.dubhe.data.service.http.DatasetVersionHttpService;
import org.dubhe.data.util.ConversionUtil;
import org.dubhe.data.util.StatusIdentifyUtil;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.dto.UserSmallDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.UserService;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.dubhe.constant.PermissionConstant.ADMIN_USER_ID;
import static org.dubhe.data.constant.Constant.*;

/**
 * @description 数据集版本功能 服务实现类
 * @date 2020-05-14
 */
@Service
public class DatasetVersionServiceImpl extends ServiceImpl<DatasetVersionMapper, DatasetVersion>
        implements DatasetVersionService {

    /**
     * nfs服务端 共享目录
     */
    @Value("${k8s.nfs-root-path}")
    private String nfsPath;

    /**
     * nfs服务暴露的IP地址
     */
    @Value("${k8s.nfs}")
    private String nfsIp;

    /**
     * 文件存储服务器用户名
     */
    @Value("${data.server.userName}")
    private String userName;

    /**
     * 数据集服务
     */
    @Resource
    private DatasetServiceImpl datasetService;

    /**
     * 数据集版本mapper
     */
    @Resource
    private DatasetVersionMapper datasetVersionMapper;

    /**
     * 数据集版本文件服务
     */
    @Resource
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * minIo客户端工具
     */
    @Resource
    private MinioUtil minioUtil;

    /**
     * 线程池
     */
    @Autowired
    private BasePool pool;

    /**
     * 二进制转换http服务
     */
    @Autowired
    private ConversionHttpService conversionHttpService;

    /**
     * bucketName
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 获取数据集实时状态工具类
     */
    @Resource
    private StatusIdentifyUtil statusIdentifyUtil;

    /**
     * 调用算法判断当前数据集是否可以被删除(训练算法)
     */
    @Autowired
    private DatasetVersionHttpService datasetVersionHttpService;

    /**
     * 文件服务
     */
    @Autowired
    public FileService fileService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 文本格式转换(json to txt)
     */
    @Autowired
    private ConversionUtil conversionUtil;

    private ConcurrentHashMap<Long, Boolean> copyFlag = new ConcurrentHashMap<>();

    /**
     * 数据集版本发布
     *
     * @param datasetVersionCreateDTO 数据集版本条件
     * @return String 版本名
     */
    @Override
    public String publish(DatasetVersionCreateDTO datasetVersionCreateDTO) {
        datasetVersionCreateDTO.setVersionName(getNextVersionName(datasetVersionCreateDTO.getDatasetId()));
        Dataset dataset = datasetService.getById(datasetVersionCreateDTO.getDatasetId());
        // 1.判断数据集是否存在
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        datasetService.checkPublic(dataset);
        // 数据集标注完成才能发布
        DatasetStatusEnum currentDatasetStatus = statusIdentifyUtil.getStatus(dataset.getId(), dataset.getCurrentVersionName());
        dataset.setStatus(currentDatasetStatus.getValue());
        if (dataset.getStatus() != DatasetStatusEnum.FINISHED.getValue() && dataset.getStatus() != DatasetStatusEnum.AUTO_FINISHED.getValue()
                && dataset.getStatus() != DatasetStatusEnum.FINISHED_TRACK.getValue()) {
            throw new BusinessException(ErrorEnum.DATASET_ANNOTATION_NOT_FINISH, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        // 2.判断用户输入的版本是否已经存在
        List<DatasetVersion> datasetVersionList = datasetVersionMapper.
                findDatasetVersion(datasetVersionCreateDTO.getDatasetId(), datasetVersionCreateDTO.getVersionName());
        if (CollectionUtil.isNotEmpty(datasetVersionList)) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_EXIST, null, null);
        }
        String prefixPath = nfsPath + bucketName + File.separator + dataset.getUri() + File.separator;
        String annotationSourceDir = prefixPath + "annotation" +
                (dataset.getCurrentVersionName() == null ? "" : (File.separator + dataset.getCurrentVersionName()));
        String annotationTargetDir = prefixPath + "annotation" + File.separator + datasetVersionCreateDTO.getVersionName();
        String annVersionTargetDir = prefixPath + "versionFile" + File.separator
                + datasetVersionCreateDTO.getVersionName() + File.separator + "annotation";
        String command = String.format(COMMAND, userName, nfsIp, annotationTargetDir, annotationSourceDir
                , annotationTargetDir, annVersionTargetDir, annotationSourceDir, annVersionTargetDir);
        String[] cmd = {"/bin/sh", "-c", command};
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (!datasetVersionFileService.getCopyResult(process)) {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_ANNOTATION_COPY_EXCEPTION);
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_DATASET, ErrorEnum.DATASET_VERSION_ANNOTATION_COPY_EXCEPTION + e.getMessage());
            throw new BusinessException(ErrorEnum.DATASET_VERSION_ANNOTATION_COPY_EXCEPTION);
        }
        publishDo(dataset, datasetVersionCreateDTO);
        return datasetVersionCreateDTO.getVersionName();
    }

    /**
     * 发布部分数据库操作
     *
     * @param dataset                 数据集
     * @param datasetVersionCreateDTO 数据集版本参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishDo(Dataset dataset, DatasetVersionCreateDTO datasetVersionCreateDTO) {
        String versionUrl = dataset.getUri() + File.separator
                + "versionFile" + File.separator + datasetVersionCreateDTO.getVersionName();
        DatasetVersion datasetVersion = new DatasetVersion(dataset.getCurrentVersionName(), versionUrl, datasetVersionCreateDTO);
        datasetVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        //新增数据集版本信息
        datasetVersionMapper.insert(datasetVersion);
        // 4.写入版本文件关系数据(新版本) - 正常情况
        saveDatasetVersionFiles(dataset, datasetVersionCreateDTO.getVersionName());
        // 5.更改新增关系版本信息
        datasetVersionFileService.newShipVersionNameChange(dataset.getId(),
                dataset.getCurrentVersionName(), datasetVersionCreateDTO.getVersionName());
        //6.版本回退
        if (!datasetVersionFileService.rollbackDataset(dataset)) {
            throw new BusinessException("rollback failure");
        }
        // 7.更新数据集当前版本
        datasetService.updateVersionName(dataset.getId(), datasetVersionCreateDTO.getVersionName());
    }

    /**
     * 发布时文件复制
     *
     * @param dataset        数据集
     * @param datasetVersion 数据集版本
     */
    public void publishCopyFile(Dataset dataset, DatasetVersion datasetVersion) {
        copyFlag.put(datasetVersion.getId(), false);
        String targetDir = dataset.getUri() + File.separator + "versionFile" + File.separator
                + datasetVersion.getVersionName() + File.separator + "origin";
        List<DatasetVersionFile> datasetVersionFiles =
                datasetVersionFileService.findByDatasetIdAndVersionName(dataset.getId(), datasetVersion.getVersionName());
        //当前发布版本所有图片的文件名
        List<String> picNames = new ArrayList<>();
        datasetVersionFiles.forEach(f -> {
            String picUrl = fileService.selectById(f.getFileId()).getUrl();
            picNames.add(StringUtils.substringAfter(picUrl, "/"));
        });
        try {
            minioUtil.copyDir(bucketName, picNames, targetDir);
            datasetVersion.setDataConversion(ConversionStatusEnum.NOT_CONVERSION.getValue());
            getBaseMapper().updateById(datasetVersion);
            if (AnnotateTypeEnum.OBJECT_DETECTION.getValue().equals(dataset.getAnnotateType())) {
                LogUtil.info(LogEnum.BIZ_DATASET, "yolo conversion start");
                conversionUtil.txtConversion(targetDir, dataset.getId());
                LogUtil.info(LogEnum.BIZ_DATASET, "yolo conversion end");
            }
            copyFlag.remove(datasetVersion.getId());
        } catch (Exception e) {
            copyFlag.put(datasetVersion.getId(), true);
            LogUtil.error(LogEnum.BIZ_DATASET, "fail to copy or conversion:{}", e);
            throw new BusinessException(ErrorEnum.DATASET_VERSION_ANNOTATION_COPY_EXCEPTION);
        }
    }

    /**
     * 训练任务所需版本
     *
     * @param id 数据集id
     * @return List<DatasetVersionVO> 版本列表
     */
    @Override
    public List<DatasetVersionVO> versionList(Long id) {
        List<DatasetVersionVO> list = new ArrayList<>();
        DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria = new DatasetVersionQueryCriteriaDTO();
        datasetVersionQueryCriteria.setDatasetId(id);
        datasetVersionQueryCriteria.setDeleted(NOT_DELETED);
        List<DatasetVersion> datasetVersions = datasetVersionMapper.selectList(WrapperHelp.getWrapper(datasetVersionQueryCriteria));
        datasetVersions.forEach(datasetVersion -> {
            Integer imageCounts = datasetVersionFileService.getImageCountsByDatasetIdAndVersionName(
                    datasetVersion.getDatasetId(),
                    datasetVersion.getVersionName()
            );
            DatasetVersionVO datasetVersionVO = new DatasetVersionVO();
            datasetVersionVO.setVersionName(datasetVersion.getVersionName());
            datasetVersionVO.setVersionNote(datasetVersion.getVersionNote());
            datasetVersionVO.setImageCounts(imageCounts);
            if (!ConversionStatusEnum.NOT_COPY.getValue().equals(datasetVersion.getDataConversion())) {
                datasetVersionVO.setVersionUrl(datasetVersion.getVersionUrl());
            }
            if (ConversionStatusEnum.IS_CONVERSION.getValue().equals(datasetVersion.getDataConversion())) {
                String binaryUrl = datasetVersion.getVersionUrl() + File.separator + OFRECORD + File.separator + TRAIN;
                datasetVersionVO.setVersionOfRecordUrl(binaryUrl);
            }
            list.add(datasetVersionVO);
        });
        return list;
    }

    /**
     * 保存版本文件
     *
     * @param dataset     数据集id
     * @param versionName 版本名
     */
    public void saveDatasetVersionFiles(Dataset dataset, String versionName) {
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.
                findByDatasetIdAndVersionName(dataset.getId(), dataset.getCurrentVersionName());
        if (datasetVersionFiles != null && datasetVersionFiles.size() > MagicNumConstant.ZERO) {
            datasetVersionFiles.stream().forEach(datasetVersionFile -> {
                datasetVersionFile.setVersionName(versionName);
                datasetVersionFile.setBackupStatus(datasetVersionFile.getAnnotationStatus());
            });
            datasetVersionFileService.insertList(datasetVersionFiles);
        }
    }

    /**
     * 获取用户信息
     *
     * @param userDtoMap
     * @param datasetVersion 数据集版本
     * @return UserSmallDTO 用户信息
     */
    public UserSmallDTO getUserSmallDTO(Map<Long, UserSmallDTO> userDtoMap, DatasetVersion datasetVersion) {
        UserSmallDTO userSmallDTO = null;
        if (!userDtoMap.containsKey(datasetVersion.getCreateUserId())) {
            UserDTO userDTO = userService.findById(datasetVersion.getCreateUserId());
            if (ObjectUtil.isNotNull(userDTO)) {
                userSmallDTO = new UserSmallDTO(userDTO);
                userDtoMap.put(datasetVersion.getCreateUserId(), userSmallDTO);
            }
        } else {
            userSmallDTO = userDtoMap.get(datasetVersion.getCreateUserId());
        }
        return userSmallDTO;
    }

    /**
     * 数据集版本列表
     *
     * @param datasetVersionQueryCriteria 查询条件
     * @param page                        分页查询
     * @return Map<String, Object> 版本列表
     */
    @Override
    public Map<String, Object> getList(DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria, Page<DatasetVersion> page) {
        Dataset dataset = datasetService.getById(datasetVersionQueryCriteria.getDatasetId());
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetVersionQueryCriteria.getDatasetId(), null);
        }
        //获取到管理员ID和用户ID
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        datasetVersionQueryCriteria.setCreateUserId(createUserIds);
        List<DatasetVersionVO> result = new ArrayList<>();
        IPage<DatasetVersion> datasetVersionPage = datasetVersionMapper.selectPage(page, WrapperHelp.getWrapper(datasetVersionQueryCriteria));
        Map<Long, UserSmallDTO> userDtoMap = new HashMap<>(MagicNumConstant.SIXTEEN);
        datasetVersionPage.getRecords().stream().forEach(datasetVersion -> {
            DatasetVersionVO datasetVersionVO = new DatasetVersionVO(datasetVersion, dataset);
            if (datasetVersion.getCreateUserId() != null) {
                datasetVersionVO.setCreateUser(getUserSmallDTO(userDtoMap, datasetVersion));
            }
            if (datasetVersion.getUpdateUserId() != null) {
                datasetVersionVO.setUpdateUser(getUserSmallDTO(userDtoMap, datasetVersion));
            }
            List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService
                    .getFilesByDatasetIdAndVersionName(dataset.getId(), datasetVersion.getVersionName());
            List<org.dubhe.data.domain.entity.File> files = new ArrayList<>();
            if (!CollectionUtils.isEmpty(datasetVersionFiles)) {
                datasetVersionVO.setFileCount(datasetVersionFiles.size());
                ProgressVO progressVO = new ProgressVO();
                datasetVersionFiles.stream().forEach(file -> {
                    org.dubhe.data.domain.entity.File datasetFile = new org.dubhe.data.domain.entity.File();
                    datasetFile.setStatus(file.getAnnotationStatus());
                    datasetFile.setId(file.getFileId());
                    files.add(datasetFile);
                    switch (file.getAnnotationStatus()) {
                        case MagicNumConstant.ZERO:
                        case MagicNumConstant.ONE:
                            progressVO.setUnfinished(progressVO.getUnfinished() + MagicNumConstant.ONE);
                            break;
                        case MagicNumConstant.TWO:
                            progressVO.setAutoFinished(progressVO.getAutoFinished() + MagicNumConstant.ONE);
                            break;
                        case MagicNumConstant.THREE:
                            progressVO.setFinished(progressVO.getFinished() + MagicNumConstant.ONE);
                            break;
                        case MagicNumConstant.FOUR:
                            progressVO.setFinishAutoTrack(progressVO.getFinishAutoTrack() + MagicNumConstant.ONE);
                            break;
                        default:
                    }
                });
                datasetVersionVO.setProgressVO(progressVO);
            }
            DatasetStatusEnum datasetStatusEnum = statusIdentifyUtil.getStatus(datasetVersionVO.getDatasetId(), datasetVersionVO.getVersionName());
            datasetVersionVO.setStatus(datasetStatusEnum == null ? DatasetStatusEnum.INIT.getValue() : datasetStatusEnum.getValue());
            datasetVersionVO.setDataConversion(datasetVersion.getDataConversion());
            result.add(datasetVersionVO);
        });
        return PageUtil.toPage(datasetVersionPage, result);
    }

    /**
     * 删除版本
     *
     * @param datasetId          数据集id
     * @param versionName        版本名
     * @param datasetVersionUrls 数据集版本url
     */
    public void delVersion(Long datasetId, String versionName, List<String> datasetVersionUrls) {
        Dataset dataset = datasetService.getById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH, "id:" + datasetId, null);
        }
        datasetService.checkPublic(dataset);
        if (versionName.equals(dataset.getCurrentVersionName())) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_DELETE_CURRENT_ERROR);
        }
        Set<Long> createUserIds = new HashSet<>();
        createUserIds.add(dataset.getCreateUserId());
        createUserIds.add(ADMIN_USER_ID);
        UpdateWrapper<DatasetVersion> datasetVersionUpdateWrapper = new UpdateWrapper<>();
        datasetVersionUpdateWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName)
                .in("create_user_id", createUserIds);
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDeleted(true);
        baseMapper.update(datasetVersion, datasetVersionUpdateWrapper);
        //删除版本对应的minio文件
        datasetVersionUrls.forEach(dataseturl -> {
            try {
                minioUtil.del(bucketName, dataseturl);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset version file error", e);
            }
        });
    }

    /**
     * 数据集版本删除
     *
     * @param datasetVersionDeleteDTO 数据集版本删除条件
     */
    @Override
    public void versionDelete(DatasetVersionDeleteDTO datasetVersionDeleteDTO) {
        //取出当前数据集版本的url
        List<String> thisUrls = datasetVersionMapper.selectVersionUrl(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName());
        List<String> datasetVersionUrls = new ArrayList<>();
        thisUrls.forEach(url -> {
            datasetVersionUrls.add(url);
            datasetVersionUrls.add(url + StrUtil.SLASH + "ofrecord" + StrUtil.SLASH + "train");
        });
        if (!CollectionUtils.isEmpty(datasetVersionUrls)) {
            //训练中的url进行比较
            boolean status = datasetVersionHttpService.urlStatus(datasetVersionUrls);
            if (status) {
                this.delVersion(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName(), datasetVersionUrls);
            } else {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_PTJOB_STATUS);
            }
        } else {
            this.delVersion(datasetVersionDeleteDTO.getDatasetId(), datasetVersionDeleteDTO.getVersionName(), datasetVersionUrls);
        }
    }

    /**
     * 数据集版本切换
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void versionSwitch(Long datasetId, String versionName) {
        Dataset dataset = datasetService.getById(datasetId);
        // 业务判断
        // 1.判断数据集是否存在
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        // (1) 数据集标注中不允许删除
        if (dataset.getStatus() == DatasetStatusEnum.AUTO_ANNOTATING.getValue() ||
                dataset.getStatus() == DatasetStatusEnum.AUTO_ANNOTATING.getValue()) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_STATUS_NO_SWITCH, "id:" + datasetId, null);
        }
        // 1.数据集版本回退
        if (!datasetVersionFileService.rollbackDataset(dataset)) {
            throw new BusinessException("rollback failure");
        }
        // 2.版本切换
        datasetService.updateVersionName(datasetId, versionName);
    }

    /**
     * 获取下一个可用版本号
     *
     * @param datasetId 数据集id
     * @return String 下一个可用版本名称
     */
    @Override
    public String getNextVersionName(Long datasetId) {
        Dataset dataset = datasetService.getById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        String maxVersionName = datasetVersionMapper.getMaxVersionName(datasetId);
        if (StringUtils.isEmpty(maxVersionName)) {
            return Constant.DEFAULT_VERSION;
        } else {
            Integer versionName = Integer.parseInt(maxVersionName.substring(1, maxVersionName.length())) + MagicNumConstant.ONE;
            return Constant.DATASET_VERSION_PREFIX + StringUtils.stringFillIn(versionName.toString(), MagicNumConstant.FOUR, MagicNumConstant.ZERO);
        }
    }

    /**
     * 数据集版本数据删除
     *
     * @param datasetId 数据集id
     */
    @Override
    public void datasetVersionDelete(Long datasetId) {
        baseMapper.datasetVersionDelete(datasetId);
    }

    /**
     * 数据转换
     */
    @Override
    public void datasetConvert() {
        DatasetVersionCriteriaVO needConversion = DatasetVersionCriteriaVO.builder()
                .deleted(NOT_DELETED).dataConversion(ConversionStatusEnum.NOT_CONVERSION.getValue()).build();
        QueryWrapper<DatasetVersion> queryWrapper = WrapperHelp.getWrapper(needConversion);
        String limit = "limit " + MagicNumConstant.TEN;
        queryWrapper.last(limit);
        List<DatasetVersion> versions = list(queryWrapper);
        if (CollectionUtil.isEmpty(versions)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "no version data to convert");
            return;
        }
        versions.forEach(f -> {
            Dataset dataset = datasetService.getBaseMapper().selectById(f.getDatasetId());
            if (AnnotateTypeEnum.CLASSIFICATION.getValue().equals(dataset.getAnnotateType())) {
                conversionHttpService.convert(f);
                f.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            } else {
                f.setDataConversion(ConversionStatusEnum.UNABLE_CONVERSION.getValue());
            }
            getBaseMapper().updateById(f);
        });
    }

    /**
     * 数据转换回调接口
     *
     * @param datasetVersionId 版本id
     * @return int 影响版本数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishConvert(Long datasetVersionId, ConversionCreateDTO conversionCreateDTO) {
        LogUtil.info(LogEnum.BIZ_DATASET, "conversion call-back id:{},msg:{}", datasetVersionId, conversionCreateDTO.getMsg());
        DatasetVersion datasetVersion = getBaseMapper().selectById(datasetVersionId);
        if (CONVERSION_SUCCESS.equals(conversionCreateDTO.getMsg())) {
            datasetVersion.setDataConversion(ConversionStatusEnum.IS_CONVERSION.getValue());
        } else {
            datasetVersion.setDataConversion(ConversionStatusEnum.UNABLE_CONVERSION.getValue());
        }
        datasetVersion.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return getBaseMapper().updateById(datasetVersion);
    }

    /**
     * 文件复制
     */
    @Override
    public void fileCopy() {
        DatasetVersionCriteriaVO needFileCopy = DatasetVersionCriteriaVO.builder()
                .deleted(NOT_DELETED).dataConversion(ConversionStatusEnum.NOT_COPY.getValue()).build();
        List<DatasetVersion> versions = list(WrapperHelp.getWrapper(needFileCopy));
        if (CollectionUtil.isEmpty(versions)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "No version data to copy");
            return;
        }
        versions.forEach(version -> {
            copyFlag.putIfAbsent(version.getId(), true);
            if (copyFlag.get(version.getId())) {
                try {
                    Dataset dataset = datasetService.getBaseMapper().selectById(version.getDatasetId());
                    pool.getExecutor().submit(() -> publishCopyFile(dataset, version));
                } catch (Exception e) {
                    LogUtil.error(LogEnum.BIZ_DATASET, "copy task is refused", e);
                }
            }
        });
    }

    /**
     * 查询当前数据集版本的原始文件数量
     *
     * @param datasetId 数据集id
     * @return Integer 原始文件数量
     */
    @Override
    public Integer getSourceFileCount(Long datasetId) {
        Dataset dataset = datasetService.getBaseMapper().selectById(datasetId);
        return datasetVersionFileService.getSourceFileCount(dataset);
    }

}
