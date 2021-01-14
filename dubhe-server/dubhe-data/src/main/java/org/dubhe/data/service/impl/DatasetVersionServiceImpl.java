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
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.NumberConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetVersionMapper;
import org.dubhe.data.domain.dto.ConversionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionDeleteDTO;
import org.dubhe.data.domain.dto.DatasetVersionQueryCriteriaDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersion;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.domain.vo.DatasetVersionCriteriaVO;
import org.dubhe.data.domain.vo.DatasetVersionVO;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.identify.service.StateIdentify;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.DatasetVersionService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.TaskService;
import org.dubhe.data.service.http.DatasetVersionHttpService;
import org.dubhe.data.util.ConversionUtil;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.domain.dto.UserSmallDTO;
import org.dubhe.enums.DatasetTypeEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.OperationTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.UserService;
import org.dubhe.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     * 数据集版本文件服务实现类
     */
    @Resource
    private DatasetVersionFileServiceImpl datasetVersionFileServiceImpl;

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
     * bucketName
     */
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 获取数据集实时状态工具类
     */
    @Resource
    private StateIdentify stateIdentify;

    /**
     * 调用算法判断当前数据集是否可以被删除(训练算法)
     */
    @Autowired
    private DatasetVersionHttpService datasetVersionHttpService;

    /**
     * 文件服务
     */
    @Resource
    @Lazy
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

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final ConcurrentHashMap<Long, Boolean> copyFlag = new ConcurrentHashMap<>();

    @Autowired
    private TaskService taskService;

    private static final String ANNOTATION = "annotation";

    private static final String VERSION_FILE = "versionFile";

    /**
     * 数据集版本发布
     *
     * @param datasetVersionCreateDTO 数据集版本条件
     * @return String 版本名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publish(DatasetVersionCreateDTO datasetVersionCreateDTO) {
        datasetVersionCreateDTO.setVersionName(getNextVersionName(datasetVersionCreateDTO.getDatasetId()));
        Dataset dataset = datasetService.getById(datasetVersionCreateDTO.getDatasetId());
        // 1.判断数据集是否存在
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        //判断数据集是否在发布中
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        // 数据集标注完成才能发布
        DataStateEnum currentDatasetStatus = stateIdentify.getStatus(dataset.getId(), dataset.getCurrentVersionName(), false);
        dataset.setStatus(currentDatasetStatus.getCode());
        if (!dataset.getStatus().equals(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE) && !dataset.getStatus().equals(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE)
                && !dataset.getStatus().equals(DataStateCodeConstant.TARGET_COMPLETE_STATE)) {
            throw new BusinessException(ErrorEnum.DATASET_ANNOTATION_NOT_FINISH, "id:" + datasetVersionCreateDTO.getDatasetId(), null);
        }
        // 2.判断用户输入的版本是否已经存在
        List<DatasetVersion> datasetVersionList = datasetVersionMapper.
                findDatasetVersion(datasetVersionCreateDTO.getDatasetId(), datasetVersionCreateDTO.getVersionName());
        if (CollectionUtil.isNotEmpty(datasetVersionList)) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_EXIST, null, null);
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
        datasetVersion.setOriginUserId(dataset.getCreateUserId());
        datasetVersion.setDataConversion(ConversionStatusEnum.PUBLISHING.getValue());
        //新增数据集版本信息
        datasetVersionMapper.insert(datasetVersion);
        // 更新数据集当前版本
        datasetService.updateVersionName(dataset.getId(), datasetVersionCreateDTO.getVersionName());
    }

    /**
     * 发布时文件复制
     *
     * @param dataset        数据集
     * @param datasetVersion 数据集版本
     */
    public void publishCopyFile(Dataset dataset, DatasetVersion datasetVersion) {
        //标记开始复制操作
        copyFlag.put(datasetVersion.getId(), false);
        // targetDir = dataset/25/versionFile/V0001（未复制版本版本号（新版本））/origin
        String targetDir = dataset.getUri() + File.separator + VERSION_FILE + File.separator
                + datasetVersion.getVersionName() + File.separator + "origin";
        //获取新发布版本的数据集版本中间表的数据
        List<DatasetVersionFile> datasetVersionFiles =
                datasetVersionFileService.findByDatasetIdAndVersionName(dataset.getId(), datasetVersion.getVersionName());
        //当前发布版本所有图片的文件名
        List<String> picNames = new ArrayList<>();
        //获取当前版本（新版本）的文件URL
        List<String> picUrls = fileService.selectUrls(dataset.getId(), datasetVersion.getVersionName());
        picUrls.forEach(picUrl -> picNames.add(StringUtils.substringAfter(picUrl, "/")));
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
            if (dataset.getAnnotateType().equals(MagicNumConstant.TWO)) {
                Task task = Task.builder().total(datasetVersionFiles.size())
                        .datasetId(dataset.getId())
                        .type(DataTaskTypeEnum.OFRECORD.getValue())
                        .labels("")
                        .datasetVersionId(datasetVersion.getId()).build();
                taskService.createTask(task);
            }
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
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
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
     * @param version     数据集版本
     */
    public void saveDatasetVersionFiles(DatasetVersion version) {
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.
                findByDatasetIdAndVersionName(version.getDatasetId(), version.getVersionSource());
        if (datasetVersionFiles != null && datasetVersionFiles.size() > MagicNumConstant.ZERO) {
            datasetVersionFiles.stream().forEach(datasetVersionFile -> {
                datasetVersionFile.setVersionName(version.getVersionName());
                datasetVersionFile.setBackupStatus(datasetVersionFile.getAnnotationStatus());
            });
            datasetVersionFileService.insertList(datasetVersionFiles);
        }
    }

    /**
     * 获取用户信息
     *
     * @param userDtoMap     用户信息
     * @param datasetVersion 数据集版本
     * @return UserSmallDTO  用户信息
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
     * @return Map<String, Object>        版本列表
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> getList(DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria) {
        //校验入参
        if (datasetVersionQueryCriteria.getCurrent()==null||datasetVersionQueryCriteria.getSize()==null){
                throw new BusinessException(ErrorEnum.PARAM_ERROR);
        }
        //校验数据集是否合法
        Dataset dataset = datasetService.getById(datasetVersionQueryCriteria.getDatasetId());
        if (dataset==null){
            throw new BusinessException(ErrorEnum.PARAM_ERROR);
        }
        //查询数据集版本历史列表
        QueryWrapper<DatasetVersion> wrapper = WrapperHelp.getWrapper(datasetVersionQueryCriteria);
        Page<DatasetVersionVO> pages = new Page<DatasetVersionVO>() {{
            setCurrent(datasetVersionQueryCriteria.getCurrent());
            setSize(datasetVersionQueryCriteria.getSize());
            setTotal(datasetVersionMapper.selectCount(wrapper));
            List<DatasetVersionVO> collect = datasetVersionMapper.selectList(
                    wrapper.last(" limit " + (datasetVersionQueryCriteria.getCurrent() - NumberConstant.NUMBER_1)*datasetVersionQueryCriteria.getSize() + ", " + datasetVersionQueryCriteria.getSize())
            ).stream().map(val -> {
                        return DatasetVersionVO.from(val,
                                dataset,
                                datasetService.progress(new ArrayList<Long>() {{
                                    add(dataset.getId());
                                }}).get(dataset.getId()),
                                datasetVersionFileService.selectDatasetVersionFileCount(val),
                                getUserSmallDTO(new HashMap<>(MagicNumConstant.SIXTEEN),val),
                                getUserSmallDTO(new HashMap<>(MagicNumConstant.SIXTEEN),val)
                        );
                    }
            ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)){
                setRecords(collect);
            }
        }};
        return PageUtil.toPage(pages);

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
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        if (versionName.equals(dataset.getCurrentVersionName())) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_DELETE_CURRENT_ERROR);
        }
        UpdateWrapper<DatasetVersion> datasetVersionUpdateWrapper = new UpdateWrapper<>();
        datasetVersionUpdateWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDeleted(true);
        baseMapper.update(datasetVersion, datasetVersionUpdateWrapper);

        UpdateWrapper<DatasetVersionFile> datasetVersionFileUpdateWrapper = new UpdateWrapper<>();
        datasetVersionFileUpdateWrapper.eq("dataset_id", datasetId)
                .eq("version_name", versionName);
        DatasetVersionFile datasetVersionFile = new DatasetVersionFile();
        datasetVersionFile.setStatus(MagicNumConstant.ONE);
        datasetVersionFileServiceImpl.getBaseMapper().update(datasetVersionFile, datasetVersionFileUpdateWrapper);
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
    @DataPermissionMethod
    public void versionDelete(DatasetVersionDeleteDTO datasetVersionDeleteDTO) {
        datasetService.checkPublic(datasetVersionDeleteDTO.getDatasetId(), OperationTypeEnum.UPDATE);
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
    @DataPermissionMethod
    public void versionSwitch(Long datasetId, String versionName) {
        datasetService.checkPublic(datasetId, OperationTypeEnum.UPDATE);
        Dataset dataset = datasetService.getById(datasetId);
        // 业务判断
        // 1.判断数据集是否存在
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        //判断数据集是否在发布中
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        // 自动标注中不允许版本切换
        if (dataset.getStatus().equals(DataStateCodeConstant.AUTOMATIC_LABELING_STATE)
                || dataset.getStatus().equals(DataStateCodeConstant.TARGET_FOLLOW_STATE)
                || dataset.getStatus().equals(DataStateCodeConstant.TARGET_FAILURE_STATE)
        ) {
            throw new BusinessException(ErrorEnum.DATASET_VERSION_STATUS_NO_SWITCH, "id:" + datasetId, null);
        }
        //版本回退
        datasetVersionFileService.rollbackDataset(dataset);
        // 2.版本切换
        datasetService.updateVersionName(datasetId, versionName);
        //更新当前版本数据集的状态
        DataStateEnum status = stateIdentify.getStatus(dataset.getId(), versionName, true);
        datasetService.updateStatus(dataset.getId(), status);
    }

    /**
     * 获取下一个可用版本号
     *
     * @param datasetId 数据集id
     * @return String 下一个可用版本名称
     */
    @Override
    @DataPermissionMethod
    public String getNextVersionName(Long datasetId) {
        Dataset dataset = datasetService.getById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT, "id:" + datasetId, null);
        }
        String maxVersionName = datasetVersionMapper.getMaxVersionName(datasetId);
        if (StringUtils.isEmpty(maxVersionName)) {
            return Constant.DEFAULT_VERSION;
        } else {
            Integer versionName = Integer.parseInt(maxVersionName.substring(1)) + MagicNumConstant.ONE;
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
        //查处所有转换状态是未复制的版本
        List<DatasetVersion> versions = list(WrapperHelp.getWrapper(needFileCopy));
        if (CollectionUtil.isEmpty(versions)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "No version data to copy");
            return;
        }
        versions.forEach(version -> {
            copyFlag.putIfAbsent(version.getId(), true);
            //如果当前版本状态为未复制
            if (copyFlag.get(version.getId())) {
                try {
                    Dataset dataset = datasetService.getBaseMapper().selectById(version.getDatasetId());
                    //未复制状态的版本
                    pool.getExecutor().submit(() -> publishCopyFile(dataset, version));
                } catch (Exception e) {
                    LogUtil.error(LogEnum.BIZ_DATASET, "copy task is refused", e);
                }
            }
        });
    }

    /**
     * 标注文件复制
     */
    @Override
    public void annotationFileCopy() {
        DatasetVersionCriteriaVO needFileCopy = DatasetVersionCriteriaVO.builder()
                .deleted(NOT_DELETED).dataConversion(ConversionStatusEnum.PUBLISHING.getValue()).build();
        List<DatasetVersion> versions = list(WrapperHelp.getWrapper(needFileCopy));
        versions.forEach(version -> {
            Dataset dataset = datasetService.getBaseMapper().selectById(version.getDatasetId());
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try{
                        // 写入版本文件关系数据(新版本) - 正常情况
                        saveDatasetVersionFiles(version);
                        // 更改新增关系版本信息
                        datasetVersionFileService.newShipVersionNameChange(dataset.getId(),
                                version.getVersionSource(), version.getVersionName());
                    }catch (Exception e){
                        LogUtil.error(LogEnum.BIZ_DATASET, "update version information error:{}", e);
                        transactionStatus.setRollbackOnly();
                        return;
                    }
                }
            });
            String prefixPath = dataset.getUri() + "/";
            String annVersionTargetDir = prefixPath + VERSION_FILE + "/"
                    + version.getVersionName() + "/" + ANNOTATION;
            if (version.getVersionSource() == null) {
                String annotationSourceDir = prefixPath + ANNOTATION;
                List<String> annotationNames = new ArrayList<>();
                List<String> picUrls = fileService.selectUrls(dataset.getId(), version.getVersionName());
                picUrls.forEach(picUrl -> {
                    String fileName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(picUrl, "/"), ".");
                    String annotationUrl = annotationSourceDir + "/" + fileName;
                    annotationNames.add(annotationUrl);
                });
                minioUtil.copyObject(bucketName, annotationNames, annVersionTargetDir);
            } else {
                List<String> unChangedNames = fileService.selectNames(dataset.getId(), MagicNumConstant.ZERO, version.getVersionName());
                String unChangedAnnotationSourceDir = prefixPath + VERSION_FILE;
                List<String> unChangedAnnotationUrls = new ArrayList<>();
                unChangedNames.forEach(unChangedName -> {
                    String annotationUrl = unChangedAnnotationSourceDir + "/" + version.getVersionSource()
                            + "/" + ANNOTATION + "/" + unChangedName;
                    unChangedAnnotationUrls.add(annotationUrl);
                });
                minioUtil.copyObject(bucketName, unChangedAnnotationUrls, annVersionTargetDir);
                List<String> changedNames = fileService.selectNames(dataset.getId(), MagicNumConstant.ONE, version.getVersionName());
                String changedAnnotationSourceDir = prefixPath + ANNOTATION;
                List<String> changedAnnotationUrls = new ArrayList<>();
                changedNames.forEach(changedName -> {
                    String annotationUrl = changedAnnotationSourceDir + "/" + changedName;
                    changedAnnotationUrls.add(annotationUrl);
                });
                minioUtil.copyObject(bucketName, changedAnnotationUrls, annVersionTargetDir);
            }
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try{
                        version.setDataConversion(ConversionStatusEnum.NOT_COPY.getValue());
                        getBaseMapper().updateById(version);
                        //版本回退
                        dataset.setCurrentVersionName(version.getVersionSource());
                        datasetVersionFileService.rollbackDataset(dataset);
                    } catch (Exception e){
                        LogUtil.error(LogEnum.BIZ_DATASET, "update version conversion error:{}", e);
                        transactionStatus.setRollbackOnly();
                    }
                }
            });
        });
    }

    /**
     * 查询当前数据集版本的原始文件数量
     *
     * @param datasetId 数据集id
     * @return Integer  原始文件数量
     */
    @Override
    @DataPermissionMethod
    public Integer getSourceFileCount(Long datasetId) {
        Dataset dataset = datasetService.getBaseMapper().selectById(datasetId);
        return datasetVersionFileService.getSourceFileCount(dataset);
    }

    /**
     * 获取数据集版本详情
     *
     * @param datasetVersionId 数据集版本ID
     * @return 数据集版本信息
     */
    @Override
    public DatasetVersion detail(Long datasetVersionId) {
        return baseMapper.selectById(datasetVersionId);
    }

    /**
     * 数据集版本数据更新
     *
     * @param id            数据集版本ID
     * @param sourceStatus  原状态
     * @param targetStatus  目的状态
     */
    @Override
    public void update(Long id, Integer sourceStatus, Integer targetStatus) {
        UpdateWrapper<DatasetVersion> datasetVersionUpdateWrapper = new UpdateWrapper<>();
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDataConversion(targetStatus);
        datasetVersionUpdateWrapper.eq("id", id);
        datasetVersionUpdateWrapper.eq("data_conversion", sourceStatus);
        baseMapper.update(datasetVersion, datasetVersionUpdateWrapper);
    }

    @Override
    public DatasetVersion getDatasetVersionSourceVersion(Dataset dataset) {
        return baseMapper.selectOne(new LambdaQueryWrapper<DatasetVersion>() {{
            eq(DatasetVersion::getDatasetId, dataset.getId());
            eq(DatasetVersion::getVersionName, dataset.getCurrentVersionName());
        }});
    }

    /**
     * 获取数据集版本
     *
     * @param datasetId 数据集ID
     * @param versionName  版本名
     * @return DatasetVersion 数据集版本
     */
    @Override
    public DatasetVersion getVersionByDatasetIdAndVersionName(Long datasetId, String versionName) {
        QueryWrapper<DatasetVersion> datasetVersionQueryWrapper = new QueryWrapper<>();
        datasetVersionQueryWrapper.lambda().eq(DatasetVersion::getDatasetId,datasetId)
                .eq(DatasetVersion::getVersionName,versionName);
        return getBaseMapper().selectOne(datasetVersionQueryWrapper);
    }
}
