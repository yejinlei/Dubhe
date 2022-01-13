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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.biz.base.constant.*;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.biz.file.utils.MinioUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.api.impl.ShellFileStoreApiImpl;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.dto.PtTrainDataSourceStatusQueryDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.permission.base.BaseService;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.permission.annotation.RolePermission;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.data.client.TrainServerClient;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.biz.base.vo.ProgressVO;
import org.dubhe.data.domain.bo.FileUploadBO;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.domain.vo.*;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.StateIdentifyUtil;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.*;
import org.dubhe.data.service.task.DatasetRecycleFile;
import org.dubhe.data.util.GeneratorKeyUtil;
import org.dubhe.data.util.ZipUtil;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.domain.dto.RecycleDetailCreateDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.dubhe.recycle.enums.RecycleResourceEnum;
import org.dubhe.recycle.enums.RecycleTypeEnum;
import org.dubhe.recycle.service.RecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.Constant.*;
import static org.dubhe.data.constant.ErrorEnum.DATASET_PUBLIC_LIMIT_ERROR;

/**
 * @description 数据集服务实现类
 * @date 2020-04-10
 */
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    @Autowired
    @Lazy
    private TaskService taskService;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    /**
     * 需要同步的状态
     */
    private static final Set<Integer> NEED_SYNC_STATUS = new HashSet<Integer>() {{
        add(DataStateCodeConstant.NOT_ANNOTATION_STATE);
        add(DataStateCodeConstant.MANUAL_ANNOTATION_STATE);
        add(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE);
        add(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
        add(DataStateCodeConstant.NOT_SAMPLED_STATE);
        add(DataStateCodeConstant.TARGET_COMPLETE_STATE);
    }};

    /**
     * 执行数据扩容数据集应该具备的状态
     */
    private static final Set<Integer> COMPLETE_STATUS = new HashSet<Integer>() {{
        add(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE);
        add(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
        add(DataStateCodeConstant.TARGET_COMPLETE_STATE);
    }};


    /**
     * bucket
     */
    @Value("${minio.bucketName}")
    private String bucket;

    /**
     * 路径名前缀
     */
    @Value("${storage.file-store-root-path:/nfs/}")
    private String prefixPath;

    /**
     * esSearch索引
     */
    @Value("${es.index}")
    private String esIndex;

    /**
     * 文件信息服务
     */
    @Autowired
    public FileService fileService;


    /**
     * 数据集标签服务类
     */
    @Resource
    @Lazy
    private LabelService labelService;

    /**
     * 文件服务类
     */
    @Autowired
    private org.dubhe.data.util.FileUtil fileUtil;

    /**
     * 数据集版本文件关系服务实现类
     */
    @Autowired
    @Lazy
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * 数据集版本服务实现类
     */
    @Autowired
    @Lazy
    private DatasetVersionService datasetVersionService;

    /**
     * 数据集实时状态获取工具
     */
    @Autowired
    private StateIdentifyUtil stateIdentify;

    /**
     * 任务mapper
     */
    @Autowired
    private TaskMapper taskMapper;

    @Resource
    private TrainServerClient trainServiceClient;

    /**
     * 数据集标签服务
     */
    @Autowired
    private DatasetLabelService datasetLabelService;


    @Autowired
    private DatasetGroupLabelService datasetGroupLabelService;

    /**
     * 数据回收服务
     */
    @Autowired
    private RecycleService recycleService;

    /**
     * 标签组服务
     */
    @Autowired
    private LabelGroupServiceImpl labelGroupService;

    /**
     * 用户内容服务
     */
    @Autowired
    private UserContextService userContextService;

    /**
     * 数据回收服务类
     */
    @Autowired
    private DatasetRecycleFile datasetRecycleFile;

    /**
     * 文件回收工具
     */
    @Autowired
    private RecycleTool recycleTool;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 文件标注服务
     */
    @Resource
    private DataFileAnnotationService dataFileAnnotationService;

    /**
     * minIo客户端工具
     */
    @Resource
    private MinioUtil minioUtil;

    @Autowired
    private GeneratorKeyUtil generatorKeyUtil;

    /**
     * 线程池
     */
    @Autowired
    private BasePool pool;

    @Value("${storage.file-store}")
    private String nfsIp;

    @Value("${data.server.userName}")
    private String userName;


    /**
     * 检测是否为公共数据集
     *
     * @param id 数据集id
     * @return Boolean 是否为公共数据集
     */
    @Override
    public Boolean checkPublic(Long id, OperationTypeEnum type) {
        Dataset dataset = baseMapper.selectById(id);
        return checkPublic(dataset, type);
    }

    /**
     * 检测是否为公共数据集
     *
     * @param dataset 数据集
     */
    @Override
    public Boolean checkPublic(Dataset dataset, OperationTypeEnum type) {
        if (Objects.isNull(dataset)) {
            return false;
        }
        if (DatasetTypeEnum.PUBLIC.getValue().equals(dataset.getType())) {
            //操作类型校验公共数据集
            if (OperationTypeEnum.UPDATE.equals(type)) {
                BaseService.checkAdminPermission();
                //操作类型校验公共数据集
            } else if (OperationTypeEnum.LIMIT.equals(type)) {
                throw new BusinessException(DATASET_PUBLIC_LIMIT_ERROR);
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * 自动标注检查
     *
     * @param file 文件
     */
    @Override
    public void autoAnnotatingCheck(File file) {
        autoAnnotatingCheck(file.getDatasetId());
    }

    /**
     * 自动标注检查
     *
     * @param datasetId 数据集id
     */
    public void autoAnnotatingCheck(Long datasetId) {
        LambdaQueryWrapper<Dataset> datasetQueryWrapper = new LambdaQueryWrapper<>();
        datasetQueryWrapper
                .eq(Dataset::getId, datasetId)
                .eq(Dataset::getStatus, DataStateCodeConstant.AUTOMATIC_LABELING_STATE);
        if (getBaseMapper().selectCount(datasetQueryWrapper) > MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.AUTO_ERROR);
        }
    }

    /**
     * 数据集修改
     *
     * @param datasetCreateDTO 更新的数据集详情
     * @param datasetId        数据集id
     * @return boolean 更新是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(DatasetCreateDTO datasetCreateDTO, Long datasetId) {
        if (!exist(datasetId)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        checkPublic(datasetId, OperationTypeEnum.UPDATE);
        Dataset dataset = getBaseMapper().selectById(datasetId);
        int fileCount = fileService.getFileCountByDatasetId(datasetId);
        if (!dataset.getDataType().equals(datasetCreateDTO.getDataType())
                && fileCount > MagicNumConstant.ZERO && datasetCreateDTO.getDataType() != null) {
            throw new BusinessException(ErrorEnum.DATASET_TYPE_MODIFY_ERROR);
        }
        if (!dataset.getAnnotateType().equals(datasetCreateDTO.getAnnotateType())
                && dataset.getStatus() != MagicNumConstant.ZERO && datasetCreateDTO.getAnnotateType() != null) {
            throw new BusinessException(ErrorEnum.DATASET_ANNOTATION_MODIFY_ERROR);
        }
        Dataset newDataset = DatasetCreateDTO.update(datasetCreateDTO);
        newDataset.setId(datasetId);
        newDataset.setTop(dataset.isTop());
        newDataset.setImport(dataset.isImport());
        int count;
        try {
            count = getBaseMapper().updateById(newDataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR, null, e);
        }
        if (count == MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        //修改数据集和标签的关系
        doDatasetLabelByUpdate(dataset, datasetCreateDTO, datasetId);
        return true;
    }


    /**
     * 更新数据集状态
     *
     * @param dataset 数据集
     * @param pre     转变前的状态
     * @return boolean 更新结果
     */
    @Override
    public boolean updateStatus(Dataset dataset, DataStateEnum pre) {
        QueryWrapper<Dataset> datasetQueryWrapper = new QueryWrapper<>();
        datasetQueryWrapper.lambda().eq(Dataset::getId, dataset.getId());
        if (pre != null) {
            datasetQueryWrapper.lambda().eq(Dataset::getStatus, pre);
        }
        getBaseMapper().update(dataset, datasetQueryWrapper);
        return true;
    }

    /**
     * 更新状态
     *
     * @param id 数据集id
     * @param to 转变后的状态
     * @return boolean 更新结果
     */
    @Override
    public boolean updateStatus(Long id, DataStateEnum to) {
        return updateStatus(id, null, to);
    }

    /**
     * 更新状态实现
     *
     * @param id  数据集id
     * @param pre 转变前的状态
     * @param to  转变后的状态
     * @return boolean 更新结果
     */
    public boolean updateStatus(Long id, DataStateEnum pre, DataStateEnum to) {
        Dataset dataset = new Dataset();
        dataset.setStatus(to.getCode());
        QueryWrapper<Dataset> datasetQueryWrapper = new QueryWrapper<>();
        datasetQueryWrapper.lambda().eq(Dataset::getId, id);
        getBaseMapper().update(dataset, datasetQueryWrapper);
        return true;
    }

    /**
     * 更改数据集状态
     *
     * @param dataset 数据集
     * @param to      转变后的状态
     * @return boolean 更新结果
     */
    @Override
    public boolean transferStatus(Dataset dataset, DataStateEnum to) {
        return transferStatus(dataset, null, to);
    }

    /**
     * 更新数据集实现
     *
     * @param dataset 数据集
     * @param pre     转变前的状态
     * @param to      转变后的状态
     * @return boolean 更新结果
     */
    public boolean transferStatus(Dataset dataset, DataStateEnum pre, DataStateEnum to) {
        if (dataset == null || to == null) {
            return false;
        }
        dataset.setStatus(to.getCode());
        return updateStatus(dataset, pre);
    }

    /**
     * 保存标签
     *
     * @param label     标签
     * @param datasetId 数据集id
     * @return Long     标签id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveLabel(Label label, Long datasetId) {
        if (label.getId() == null && StringUtils.isEmpty(label.getName())) {
            throw new BusinessException(ErrorEnum.LABEL_ERROR);
        }
        if (!exist(datasetId)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        Dataset dataset = baseMapper.selectById(datasetId);
        if (Objects.isNull(dataset)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        checkPublic(dataset, OperationTypeEnum.UPDATE);

        //校验是否是预置数据集
        DatatypeEnum enumValue = DatatypeEnum.getEnumValue(dataset.getDataType());
        List<Label> labelList = labelService.getPubLabels(enumValue.getValue());

        //名称重复性校验
        if (labelService.checkoutLabelIsRepeat(datasetId, label.getName())) {
            throw new BusinessException(ErrorEnum.LABEL_NAME_REPEAT);
        }
        if (!CollectionUtils.isEmpty(labelList)) {
            Map<String, Long> labelNameMap = labelList.stream().collect(Collectors.toMap(Label::getName, Label::getId));
            if (!Objects.isNull(labelNameMap.get(label.getName()))) {
                datasetLabelService.insert(DatasetLabel.builder().datasetId(datasetId).labelId(labelNameMap.get(label.getName())).build());
                //datasetGroupLabelService.insert(DatasetGroupLabel.builder().labelGroupId(dataset.getLabelGroupId()).labelId(labelNameMap.get(label.getName())).build());
            } else {
                insertLabelData(label, datasetId);
            }
        } else {
            insertLabelData(label, datasetId);
        }

    }


    /**
     * 获取数据集详情
     *
     * @param datasetId 数据集id
     * @return DatasetVO 数据集详情
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public DatasetVO get(Long datasetId) {
        Dataset ds = baseMapper.selectById(datasetId);
        if (ds == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        if (checkPublic(ds, OperationTypeEnum.SELECT)) {
            DataContext.set(CommonPermissionDataDTO.builder().id(datasetId).type(true).build());
        }
        Map<Long, ProgressVO> statistics = fileService.listStatistics(Arrays.asList(ds));

        if (ds.getLabelGroupId() != null) {
            LabelGroup labelGroup = labelGroupService.getBaseMapper().selectById(ds.getLabelGroupId());
            DatasetVO datasetVO = buildDatasetVO(ds, labelGroup.getName(), labelGroup.getType());
            datasetVO.setProgress(statistics.get(datasetVO.getId()));
            setDatasetVOFileCount(datasetVO);
            return datasetVO;
        }

        DatasetVO datasetVO = buildDatasetVO(ds, null, null);
        setDatasetVOFileCount(datasetVO);
        return datasetVO;
    }

    private DatasetVO buildDatasetVO(Dataset dataset, String labelGroupName, Integer labelGroupType) {
        DatasetVO datasetVO = new DatasetVO();
        if (dataset == null) {
            return null;
        }
        datasetVO.setId(dataset.getId());
        datasetVO.setName(dataset.getName());
        datasetVO.setRemark(dataset.getRemark());
        datasetVO.setCreateTime(dataset.getCreateTime());
        datasetVO.setUpdateTime(dataset.getUpdateTime());
        datasetVO.setType(dataset.getType());
        datasetVO.setDataType(dataset.getDataType());
        datasetVO.setAnnotateType(dataset.getAnnotateType());
        datasetVO.setStatus(dataset.getStatus());
        datasetVO.setDecompressState(dataset.getDecompressState());
        datasetVO.setImport(dataset.isImport());
        datasetVO.setTop(dataset.isTop());
        datasetVO.setLabelGroupId(dataset.getLabelGroupId());
        datasetVO.setLabelGroupName(labelGroupName);
        datasetVO.setLabelGroupType(labelGroupType);
        datasetVO.setSourceId(dataset.getSourceId());
        datasetVO.setCurrentVersionName(dataset.getCurrentVersionName());
        return datasetVO;
    }

    /**
     * 设置数据集FileCount信息
     *
     * @param datasetVO 数据集详情
     */
    public void setDatasetVOFileCount(DatasetVO datasetVO) {
        datasetVO.setFileCount(datasetVersionFileService.getFileCountByDatasetIdAndVersion(new LambdaQueryWrapper<DatasetVersionFile>() {{
            eq(DatasetVersionFile::getDatasetId, datasetVO.getId());
            if ((datasetVO.getCurrentVersionName() == null)) {
                isNull(DatasetVersionFile::getVersionName);
            } else {
                eq(DatasetVersionFile::getVersionName, datasetVO.getCurrentVersionName());
            }
            ne(DatasetVersionFile::getStatus, DataStatusEnum.DELETE.getValue());
        }}));
    }

    /**
     * 数据集下载
     *
     * @param datasetId           数据集id
     * @param httpServletResponse 响应对象
     */
    @Override
    public void download(Long datasetId, HttpServletResponse httpServletResponse) {
        Dataset ds = baseMapper.selectById(datasetId);
        if (ds == null) {
            return;
        }
        String zipFile = ZipUtil.zip(ds.getUri());
        fileStoreApi.download(zipFile, httpServletResponse);
    }

    /**
     * 创建数据集
     *
     * @param datasetCreateDTO 数据集信息
     * @return Long 数据集id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long create(DatasetCreateDTO datasetCreateDTO) {
        Dataset dataset = DatasetCreateDTO.from(datasetCreateDTO);
        dataset.setOriginUserId(userContextService.getCurUserId());
        try {
            save(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR);
        }
        //新增数据标签关系
        List<Label> labels = labelService.listByGroupId(datasetCreateDTO.getLabelGroupId());
        if (!CollectionUtils.isEmpty(labels)) {
            List<DatasetLabel> datasetLabels = labels.stream().map(a -> {
                DatasetLabel datasetLabel = new DatasetLabel();
                datasetLabel.setDatasetId(dataset.getId());
                datasetLabel.setLabelId(a.getId());
                return datasetLabel;
            }).collect(Collectors.toList());
            datasetLabelService.saveList(datasetLabels);
        }
        //预置标签处理
        if (datasetCreateDTO.getPresetLabelType() != null) {
            presetLabel(datasetCreateDTO.getPresetLabelType(), dataset.getId());
        }
        if (DatatypeEnum.VIDEO.getValue().equals(datasetCreateDTO.getDataType())) {
            dataset.setStatus(DataStateCodeConstant.NOT_SAMPLED_STATE);
        }
        dataset.setUri(fileUtil.getDatasetAbsPath(dataset.getId()));
        if (datasetCreateDTO.getDataType().equals(DatatypeEnum.AUTO_IMPORT.getValue())) {
            //自定义数据集处理 1.生成版本数据并设计数据集当前版本 2.数据集状态修改为标注完成
            datasetVersionService.insertOne(new DatasetVersion(dataset.getId(), DEFAULT_VERSION,
                    DatatypeEnum.getEnumValue(datasetCreateDTO.getDataType()).getMsg()));
            dataset.setStatus(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE);
            dataset.setCurrentVersionName(DEFAULT_VERSION);
        }
        if(datasetCreateDTO.isImport()){
            dataset.setStatus(DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode());
        }
        updateById(dataset);
        return dataset.getId();
    }

    /**
     * 预置标签处理
     *
     * @param presetLabelType 预置标签类型
     * @param datasetId       数据集id
     */
    @Override
    public void presetLabel(Integer presetLabelType, Long datasetId) {
        List<Label> labels = labelService.listByType(presetLabelType);
        if (CollectionUtil.isNotEmpty(labels)) {
            List<DatasetLabel> datasetLabels = new ArrayList<>();
            labels.stream().forEach(label -> {
                datasetLabels.add(
                        DatasetLabel.builder()
                                .datasetId(datasetId)
                                .labelId(label.getId())
                                .build());
            });
            if (CollectionUtil.isNotEmpty(datasetLabels)) {
                datasetLabelService.saveList(datasetLabels);
            }
        }
    }

    /**
     * 删除数据集
     *
     * @param datasetDeleteDTO 删除数据集条件
     */
    @Override
    public void delete(DatasetDeleteDTO datasetDeleteDTO) {
        if (datasetDeleteDTO.getIds() == null || datasetDeleteDTO.getIds().length == MagicNumConstant.ZERO) {
            return;
        }
        for (Long id : datasetDeleteDTO.getIds()) {
            delete(id);
        }
    }

    /**
     * 删除数据集相关信息
     *
     * @param id 数据集id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long id) {
        int count = baseMapper.updateStatusById(id, true);
        if (count <= MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        //根据数据集ID删除数据集标签关联数据
        labelService.updateStatusByDatasetId(id, true);

        //删除版本数据 标注数据
        datasetVersionService.updateStatusByDatasetId(id, true);


    }

    /**
     * 数据集正在自动标注中的文件不允许删除,否则会导致进行中的任务完成的文件数达不到总文件数，无法完成
     *
     * @param fileDeleteDTO 删除文件参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(FileDeleteDTO fileDeleteDTO) {

        for (Long datasetId : fileDeleteDTO.getDatasetIds()) {
            Dataset dataset = getById(datasetId);
            checkPublic(dataset, OperationTypeEnum.UPDATE);
            //删除文件时，需要对文件做标记
            datasetVersionFileService.deleteShip(
                    datasetId,
                    dataset.getCurrentVersionName(),
                    Arrays.asList(fileDeleteDTO.getFileIds())
            );

            if (dataset.getDataType().equals(DatatypeEnum.AUDIO.getValue())) {
                List<Long> versionFileIdsByFileIds = datasetVersionFileService
                        .getVersionFileIdsByFileIds(datasetId, Arrays.asList(fileDeleteDTO.getFileIds()));
                dataFileAnnotationService.deleteBatch(datasetId, versionFileIdsByFileIds);
            }

            //改变数据集的状态
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(DataStateMachineConstant.DATA_DELETE_FILES_EVENT);
                setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            }});
            if(dataset.getDataType().equals(MagicNumConstant.TWO) || dataset.getDataType().equals(MagicNumConstant.THREE)){
                fileService.deleteEsData(fileDeleteDTO.getFileIds());
            }
        }
    }


    /**
     * 删除数据集
     *
     * @param id 数据集id
     */
    public void delete(Long id) {
        checkPublic(id, OperationTypeEnum.UPDATE);
        //数据增强中不可以删除
        Dataset dataset = baseMapper.selectById(id);
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        if (dataset.getStatus().equals(DataStateCodeConstant.STRENGTHENING_STATE)) {
            throw new BusinessException(ErrorEnum.DATASET_ENHANCEMENT);
        }
        //内置预置数据集不许删除 如：COCO等
        if (DatasetTypeEnum.PUBLIC.getValue().compareTo(dataset.getType()) == 0 && Objects.isNull(dataset.getSourceId())) {
            throw new BusinessException(ErrorEnum.DATASET_NOT_OPERATIONS_BASE_DATASET);
        }

        //取出当前数据集信息
        List<DatasetVersionVO> datasetVersionVos = datasetVersionService.versionList(id);
        List<String> datasetVersionUrls = new ArrayList<>();
        datasetVersionVos.forEach(url -> {
            datasetVersionUrls.add(url.getVersionUrl());
            datasetVersionUrls.add(url.getVersionUrl() + StrUtil.SLASH + "ofrecord" + StrUtil.SLASH + "train");
        });
        if (CollectionUtil.isNotEmpty(datasetVersionUrls)) {
            //训练中的url进行比较
            PtTrainDataSourceStatusQueryDTO dto = new PtTrainDataSourceStatusQueryDTO();
            DataResponseBody<Map<String, Boolean>> trainDataSourceStatusData = trainServiceClient.getTrainDataSourceStatus(dto.setDataSourcePath(datasetVersionUrls));
            if (!trainDataSourceStatusData.succeed() || Objects.isNull(trainDataSourceStatusData.getData())) {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_PTJOB_STATUS);
            }
            if (!trainDataSourceStatusData.getData().values().contains(false)) {
                ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id);
            }
        } else {
            ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id);
        }
        //添加回收数据
        try {
            addRecycleDataByDeleteDataset(dataset);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset file error", e);
        }
        if (dataset.getDataType().equals(DatatypeEnum.TEXT.getValue()) || dataset.getDataType().equals(DatatypeEnum.TABLE.getValue())) {
            DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(esIndex);
            deleteRequest.setQuery(new TermQueryBuilder("datasetId", dataset.getId().toString()));
            try {
                restHighLevelClient.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "delete es data error:{}", e);
            }
        }
    }


    /**
     * 添加回收数据
     *
     * @param dataset 数据集实体
     */
    private void addRecycleDataByDeleteDataset(Dataset dataset) {

        //落地回收详情数据文件回收信息
        List<RecycleDetailCreateDTO> detailList = new ArrayList<>();
        detailList.add(RecycleDetailCreateDTO.builder()
                .recycleCondition(dataset.getId().toString())
                .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                .recycleNote(RecycleTool.generateRecycleNote("落地 数据集DB 数据文件回收", dataset.getId()))
                .build());
        //落地回收详情minio 数据文件回收信息
        if (!Objects.isNull(dataset.getUri())) {
            detailList.add(RecycleDetailCreateDTO.builder()
                    .recycleCondition(prefixPath + bucket + SymbolConstant.SLASH + dataset.getUri())
                    .recycleType(RecycleTypeEnum.FILE.getCode())
                    .recycleNote(RecycleTool.generateRecycleNote("落地 minio 数据文件回收", dataset.getId()))
                    .build());
        }
        //落地回收信息
        RecycleCreateDTO recycleCreateDTO = RecycleCreateDTO.builder()
                .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                .recycleCustom(RecycleResourceEnum.DATASET_RECYCLE_FILE.getClassName())
                .restoreCustom(RecycleResourceEnum.DATASET_RECYCLE_FILE.getClassName())
                .recycleDelayDate(NumberConstant.NUMBER_1)
                .recycleNote(RecycleTool.generateRecycleNote("删除数据集相关信息", dataset.getId()))
                .detailList(detailList)
                .build();
        recycleService.createRecycleTask(recycleCreateDTO);
    }


    /**
     * 数据集查询
     *
     * @param page            分页信息
     * @param datasetQueryDTO 查询条件
     * @return MapMap<String, Object> 查询出对应的数据集
     */
    @Override
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    public Map<String, Object> listVO(Page<Dataset> page, DatasetQueryDTO datasetQueryDTO) {
        String name = datasetQueryDTO.getName();
        if (StringUtils.isEmpty(name)) {
            return queryDatasets(page, datasetQueryDTO, null);
        }
        boolean nameFlag = PATTERN_NUM.matcher(name).matches();
        if (nameFlag) {
            DatasetQueryDTO queryCriteriaId = new DatasetQueryDTO();
            BeanUtils.copyProperties(datasetQueryDTO, queryCriteriaId);
            queryCriteriaId.setName(null);
            Set<Long> ids = new HashSet<>();
            ids.add(Long.parseLong(datasetQueryDTO.getName()));
            queryCriteriaId.setIds(ids);
            Map<String, Object> map = queryDatasets(page, queryCriteriaId, null);
            if (((List) map.get(RESULT)).size() > 0) {
                queryCriteriaId.setName(name);
                queryCriteriaId.setIds(null);
                return queryDatasets(page, queryCriteriaId, Long.parseLong(datasetQueryDTO.getName()));
            }
        }
        return queryDatasets(page, datasetQueryDTO, null);
    }

    /**
     * 查询数据集列表
     *
     * @param page          分页信息
     * @param queryCriteria 查询条件
     * @param datasetId     数据集id
     * @return java.util.Map<java.lang.String, java.lang.Object> 数据集列表
     */
    public Map<String, Object> queryDatasets(Page<Dataset> page, DatasetQueryDTO queryCriteria, Long datasetId) {
        queryCriteria.timeConvert();
        QueryWrapper<Dataset> datasetQueryWrapper = WrapperHelp.getWrapper(queryCriteria);
        datasetQueryWrapper.eq("deleted", MagicNumConstant.ZERO);
        if (datasetId != null) {
            datasetQueryWrapper.or().eq("id", datasetId);
        }
        if (StringUtils.isNotEmpty(queryCriteria.getSort()) && StringUtils.isNotEmpty(queryCriteria.getOrder())) {
            datasetQueryWrapper.orderByDesc("is_top").orderBy(
                    true,
                    SORT_ASC.equals(queryCriteria.getOrder().toLowerCase()),
                    StringUtils.humpToLine(queryCriteria.getSort())
            );
        } else {
            datasetQueryWrapper.orderByDesc("is_top", "update_time");
        }

        //预置数据集类型校验
        if (!Objects.isNull(queryCriteria.getType()) && queryCriteria.getType().compareTo(DatasetTypeEnum.PUBLIC.getValue()) == 0) {
            DataContext.set(CommonPermissionDataDTO.builder().id(datasetId).type(true).build());
        }
        page = getBaseMapper().listPage(page, datasetQueryWrapper);

        Map<Long, ProgressVO> statistics = newProgressVO(page.getRecords());

        List<DatasetVO> datasetVOS = new ArrayList<>();

        //构建数据集列表
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            List<Long> groupIds = page.getRecords().stream().map(a -> a.getLabelGroupId()).collect(Collectors.toList());
            Map<Long, List<LabelGroup>> groupListMap = new HashMap<>(groupIds.size());
            if (!CollectionUtils.isEmpty(groupIds)) {
                List<LabelGroup> labelGroups = labelGroupService.getBaseMapper().selectBatchIds(groupIds);
                if (!CollectionUtils.isEmpty(labelGroups)) {
                    groupListMap = labelGroups.stream().collect(Collectors.groupingBy(LabelGroup::getId));
                }
            }
            List<Dataset> records = page.getRecords();
            if (!CollectionUtils.isEmpty(records)) {
                for (Dataset dataset : records) {
                    DatasetVO datasetVO = buildDatasetVO(dataset, null, null);
                    if (dataset.getCurrentVersionName() != null) {
                        DatasetVersion datasetVersion = datasetVersionService
                                .getVersionByDatasetIdAndVersionName(dataset.getId(), dataset.getCurrentVersionName());
                        datasetVO.setDataConversion(datasetVersion.getDataConversion());
                    }
                    datasetVO.setProgress(statistics.get(datasetVO.getId()));
                    if (!Objects.isNull(groupListMap) && !Objects.isNull(dataset.getLabelGroupId()) &&
                            !Objects.isNull(groupListMap.get(dataset.getLabelGroupId()))) {
                        LabelGroup labelGroup = groupListMap.get(dataset.getLabelGroupId()).get(0);
                        datasetVO.setLabelGroupName(labelGroup.getName());
                        datasetVO.setLabelGroupType(labelGroup.getType());
                        datasetVO.setAutoAnnotation(labelGroup.getType() == MagicNumConstant.ONE);

                    }

                    datasetVOS.add(datasetVO);
                }
            }

        }

        //处理数据集文件数量
        if (CollectionUtil.isNotEmpty(datasetVOS)) {
            for (DatasetVO datasetVo : datasetVOS) {
                datasetVo.setFileCount(datasetVersionFileService.getFileCountByDatasetIdAndVersion(new LambdaQueryWrapper<DatasetVersionFile>() {{
                    eq(DatasetVersionFile::getDatasetId, datasetVo.getId());
                    if ((datasetVo.getCurrentVersionName() == null)) {
                        isNull(DatasetVersionFile::getVersionName);
                    } else {
                        eq(DatasetVersionFile::getVersionName, datasetVo.getCurrentVersionName());
                    }
                    ne(DatasetVersionFile::getStatus, DataStatusEnum.DELETE.getValue());
                }}));
            }
        }
        BaseService.removeContext();
        return PageUtil.toPage(page, datasetVOS);
    }

    /**
     * 获取数据集标注进度接口
     *
     * @param datasetIds 数据集id列表
     * @return Map<Long, ProgressVO> 数据集文件进度
     */
    @Override
    public Map<Long, ProgressVO> progress(List<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return Collections.emptyMap();
        }
        List<Dataset> datasets = new ArrayList<>();
        datasetIds.forEach(datasetId -> {
            Dataset dataset = getBaseMapper().selectById(datasetId);
            datasets.add(dataset);
        });
        return fileService.listStatistics(datasets);
    }

    /**
     * 取消页面列表数据集文件数量
     *
     * @param datasets 数据集列表
     * @return Map<Long, ProgressVO> 数据集文件进度
     */
    public Map<Long, ProgressVO> newProgressVO(List<Dataset> datasets) {
        Map<Long, ProgressVO> res = new HashMap<>(datasets.size());
        datasets.forEach(dataset -> {
            ProgressVO progressVO = null;
            res.put(dataset.getId(), progressVO);
        });
        return res;
    }

    /**
     * 文件提交
     *
     * @param datasetId          数据集id
     * @param batchFileCreateDTO 保存的文件
     */
    @Override
    public void uploadFiles(Long datasetId, BatchFileCreateDTO batchFileCreateDTO) {
        List<Long> fileIds = saveDbForUploadFiles(datasetId, batchFileCreateDTO, batchFileCreateDTO.getIfImport());
        if(batchFileCreateDTO.getIfImport()!=null && batchFileCreateDTO.getIfImport()){
            importFileAnnotation(datasetId, fileIds);
        }
        transportTextToEsForUploadFiles(datasetId, fileIds,batchFileCreateDTO.getIfImport());
    }

    void importFileAnnotation(Long datasetId, List<Long> fileIds) {
        List<Long> versionFileIds = datasetVersionFileService.getVersionFileIdsByFileIds(datasetId, fileIds);
        List<FileUploadBO> fileUploadContent = datasetVersionFileService.getFileUploadContent(datasetId,fileIds);
        List<DataFileAnnotation> dataFileAnnotations = new ArrayList<>();
        fileUploadContent.forEach(fileUploadBO -> {
            String annPath = StringUtils.substringBeforeLast(fileUploadBO.getFileUrl(), ".");
            annPath = annPath.replace("/origin/","/annotation/").replace(bucket+"/","");
            try {
                JSONArray annJsonArray = JSONObject.parseArray((minioUtil.readString(bucket, annPath)));
                for (Object object : annJsonArray) {
                    JSONObject jsonObject = (JSONObject) object;
                    Long categoryId = Long.parseLong(jsonObject.getString("category_id"));
                    Double score = jsonObject.getString("score")==null ? null : Double.parseDouble(jsonObject.getString("score"));
                    DataFileAnnotation dataFileAnnotation = DataFileAnnotation.builder().fileName(fileUploadBO.getFileName())
                            .versionFileId(fileUploadBO.getVersionFileId())
                            .datasetId(datasetId)
                            .labelId(categoryId)
                            .prediction(score).build();
                    dataFileAnnotations.add(dataFileAnnotation);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "导入数据集读取标注出错:{}",e);
            }
        });
        if(!CollectionUtils.isEmpty(dataFileAnnotations)){
            Queue<Long> dataFileAnnotionIds = generatorKeyUtil.getSequenceByBusinessCode(Constant.DATA_FILE_ANNOTATION, dataFileAnnotations.size());
            for (DataFileAnnotation dataFileAnnotation : dataFileAnnotations) {
                dataFileAnnotation.setId(dataFileAnnotionIds.poll());
                dataFileAnnotation.setStatus(MagicNumConstant.ZERO);
                dataFileAnnotation.setInvariable(MagicNumConstant.ZERO);
            }
            dataFileAnnotationService.insertDataFileBatch(dataFileAnnotations);
        }
    }

    /**
     * 上传文件数据之数据库保存
     *
     * @param datasetId
     * @param batchFileCreateDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveDbForUploadFiles(Long datasetId, BatchFileCreateDTO batchFileCreateDTO,Boolean ifImport) {
        Dataset dataset = getBaseMapper().selectById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH, "id:" + datasetId, null);
        }
        checkPublic(datasetId, OperationTypeEnum.UPDATE);
        autoAnnotatingCheck(datasetId);
        List<File> list = fileService.saveFiles(datasetId, batchFileCreateDTO.getFiles());
        List<Long> fileIds = new ArrayList<>();
        list.forEach(file -> fileIds.add(file.getId()));
        if (!CollectionUtils.isEmpty(list)) {
            List<DatasetVersionFile> datasetVersionFiles = new ArrayList<>();
            for (File file : list) {
                DatasetVersionFile datasetVersionFile = new DatasetVersionFile(datasetId, dataset.getCurrentVersionName(), file.getId(), file.getName());
                if(ifImport != null && ifImport){
                    datasetVersionFile.setAnnotationStatus(FileTypeEnum.FINISHED.getValue());
                }
                datasetVersionFiles.add(datasetVersionFile);
            }
            datasetVersionFileService.insertList(datasetVersionFiles);
        }
        if (DataStateCodeConstant.NOT_ANNOTATION_STATE.equals(dataset.getStatus())
                || DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(dataset.getStatus())) {
            return fileIds;
        }
        //改变数据集的状态
        if(!dataset.isImport()){
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(DataStateMachineConstant.DATA_UPLOAD_FILES_EVENT);
                setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            }});
        }
        return fileIds;
    }

    /**
     * 上传文本文件数据之保存数据到ES
     *
     * @param datasetId 数据集ID
     */
    public void transportTextToEsForUploadFiles(Long datasetId, List<Long> fileIds,Boolean ifImport) {
        Dataset dataset = getBaseMapper().selectById(datasetId);
        if (dataset.getDataType().equals(MagicNumConstant.TWO) || dataset.getDataType().equals(MagicNumConstant.THREE)) {
            fileService.transportTextToEs(dataset, fileIds,ifImport);
        }
    }

    /**
     * 上传视频
     *
     * @param datasetId     数据集id
     * @param fileCreateDTO 文件参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadVideo(Long datasetId, FileCreateDTO fileCreateDTO) {
        if (!exist(datasetId)) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH, "id:" + datasetId, null);
        }
        checkPublic(datasetId, OperationTypeEnum.UPDATE);
        autoAnnotatingCheck(datasetId);
//        fileService.isExistVideo(datasetId);
        List<FileCreateDTO> videoFile = new ArrayList<>();
        videoFile.add(fileCreateDTO);
        fileService.saveVideoFiles(datasetId, videoFile, DatatypeEnum.VIDEO.getValue(), PID_OF_VIDEO, null);
        //将任务存放入redis队列中
        Task task = Task.builder()
                .datasets(JSON.toJSONString(Collections.singletonList(datasetId)))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .labels(JSONArray.toJSONString(Collections.emptyList()))
                .annotateType(MagicNumConstant.SIX)
                .dataType(MagicNumConstant.ONE)
                .datasetId(datasetId)
                .type(MagicNumConstant.FIVE)
                .url(fileCreateDTO.getUrl())
                .frameInterval(fileCreateDTO.getFrameInterval()).build();
        taskMapper.insert(task);
        //创建入参请求体
        StateChangeDTO stateChangeDTO = new StateChangeDTO();
        //创建需要执行事件的方法的传入参数
        Object[] objects = new Object[1];
        objects[0] = datasetId.intValue();
        stateChangeDTO.setObjectParam(objects);
        //添加需要执行的状态机类
        stateChangeDTO.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        //采样事件
        stateChangeDTO.setEventMethodName(DataStateMachineConstant.DATA_SAMPLED_EVENT);
        StateMachineUtil.stateChange(stateChangeDTO);
    }

    /**
     * 判断数据集是否存在
     *
     * @param id 数据集id
     * @return boolean 判断结果
     */
    public boolean exist(Long id) {
        return getBaseMapper().selectById(id) != null;
    }

    /**
     * 修改数据集当前版本
     *
     * @param id          数据集id
     * @param versionName 版本名称
     */
    public void updateVersionName(Long id, String versionName) {
        baseMapper.updateVersionName(id, versionName);
    }


    /**
     * 查询有版本的数据集
     *
     * @param page                分页条件
     * @param datasetIsVersionDTO 查询数据集(有版本)条件
     * @return Map<String, Object> 查询数据集(有版本)列表
     */
    @DataPermissionMethod(dataType = DatasetTypeEnum.PUBLIC)
    @Override
    public Map<String, Object> dataVersionListVO(Page<Dataset> page, DatasetIsVersionDTO datasetIsVersionDTO) {
        Integer annotateType = AnnotateTypeEnum.getConvertAnnotateType(datasetIsVersionDTO.getAnnotateType());
        QueryWrapper<Dataset> datasetQueryWrapper = new QueryWrapper();
        datasetQueryWrapper.isNotNull("current_version_name")
                .eq(annotateType != null, "annotate_type", annotateType)
                .in(datasetIsVersionDTO.getIds() != null, "id", datasetIsVersionDTO.getIds())
                .eq("deleted", MagicNumConstant.ZERO)
                .select("id", "name");
        IPage<Dataset> datasetPage = baseMapper.selectPage(page, datasetQueryWrapper);
        List<DatasetVersionQueryVO> dataVersionQueryVos = datasetPage.getRecords().stream().map(dataset -> {
            DatasetVersionQueryVO dataVersionQueryVo = new DatasetVersionQueryVO();
            BeanUtils.copyProperties(dataset, dataVersionQueryVo);
            return dataVersionQueryVo;
        }).collect(Collectors.toList());

        return PageUtil.toPage(page, dataVersionQueryVos);
    }

    /**
     * 数据扩容
     *
     * @param datasetEnhanceRequestDTO 数据集增强请求详情
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void enhance(DatasetEnhanceRequestDTO datasetEnhanceRequestDTO) {
        //判断数据集是否存在
        Dataset dataset = getById(datasetEnhanceRequestDTO.getDatasetId());
        if (ObjectUtil.isNull(dataset)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        if (CollectionUtil.isEmpty(datasetEnhanceRequestDTO.getTypes())) {
            throw new BusinessException(ErrorEnum.DATASET_LABEL_EMPTY);
        }
        //判断数据集是否在发布中
        if (!StringUtils.isBlank(dataset.getCurrentVersionName())) {
            if (datasetVersionService.getDatasetVersionSourceVersion(dataset).getDataConversion().equals(NumberConstant.NUMBER_4)) {
                throw new BusinessException(ErrorEnum.DATASET_PUBLISH_ERROR);
            }
        }
        if (fileService.getOriginalFileCountOfDataset(datasetEnhanceRequestDTO.getDatasetId()
                , dataset.getCurrentVersionName()) == MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATASET_ORIGINAL_FILE_IS_EMPTY);
        }
        //只有是完成状态的数据集才可以进行增强操作
        DataStateEnum dataStateEnum = stateIdentify.getStatus(
                datasetEnhanceRequestDTO.getDatasetId(),
                dataset.getCurrentVersionName(),
                true
        );
        if (dataStateEnum == null || !COMPLETE_STATUS.contains(dataStateEnum.getCode())) {
            throw new BusinessException(ErrorEnum.DATASET_NOT_ENHANCE);
        }
        // 获取当前版本文件数据
        List<DatasetVersionFile> datasetVersionFiles =
                datasetVersionFileService.getNeedEnhanceFilesByDatasetIdAndVersionName(
                        dataset.getId(),
                        dataset.getCurrentVersionName()
                );
        //创建任务
        Task task = Task.builder()
                .status(TaskStatusEnum.INIT.getValue())
                .datasets(JSON.toJSONString(Arrays.asList(datasetEnhanceRequestDTO.getDatasetId())))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .dataType(dataset.getDataType())
                .labels(JSONArray.toJSONString(Collections.emptyList()))
                .annotateType(dataset.getAnnotateType())
                .finished(MagicNumConstant.ZERO)
                .total(datasetVersionFiles.size() * datasetEnhanceRequestDTO.getTypes().size())
                .enhanceType(JSON.toJSONString(datasetEnhanceRequestDTO.getTypes()))
                .datasetId(dataset.getId())
                .type(MagicNumConstant.THREE).build();
        taskMapper.insert(task);

        //状态调用 修改数据集状态为标注完成/自动标注完成 -> 数据增强中
        StateMachineUtil.stateChange(StateChangeDTO.builder()
                .objectParam(
                        new Object[]{dataset.getId().intValue()})
                .eventMethodName(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE.compareTo(dataset.getStatus()) == 0
                        ? DataStateMachineConstant.DATA_STRENGTHENING_EVENT : DataStateMachineConstant.DATA_COMPLETE_STRENGTHENING_EVENT)
                .stateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE)
                .build());
    }

    /**
     * 获取数据集标签类型
     *
     * @param datasetId 数据集ID
     * @return DatasetLabelEnum 数据集标签类型
     */
    @Override
    public DatasetLabelEnum getDatasetLabelType(Long datasetId) {
        List<Integer> datasetLabelTypes = labelService.getDatasetLabelTypes(datasetId);
        if (CollectionUtil.isNotEmpty(datasetLabelTypes)) {
            if (datasetLabelTypes.contains(DatasetLabelEnum.MS_COCO.getType())) {
                return DatasetLabelEnum.MS_COCO;
            } else if (datasetLabelTypes.contains(DatasetLabelEnum.IMAGE_NET.getType())) {
                return DatasetLabelEnum.IMAGE_NET;
            } else if (datasetLabelTypes.contains(DatasetLabelEnum.AUTO.getType())) {
                return DatasetLabelEnum.AUTO;
            }
            return DatasetLabelEnum.CUSTOM;
        }
        return null;
    }

    /**
     * 查询公共和个人数据集的数量
     *
     * @return DatasetCountVO 数据集数量
     */
    @Override
    public DatasetCountVO queryDatasetsCount() {
        Long curUserId = JwtUtils.getCurUserId();
        if (curUserId == null) {
            throw new BusinessException("当前未登录无资源信息");
        }
        Integer publicCount = baseMapper.selectCountByPublic(DatasetTypeEnum.PUBLIC.getValue(), NumberConstant.NUMBER_0);
        Integer privateCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Dataset>() {{
                    eq(Dataset::getType, DatasetTypeEnum.PRIVATE.getValue());
                    eq(Dataset::getDeleted, NumberConstant.NUMBER_0);
                    if (!BaseService.isAdmin()) {
                        eq(Dataset::getCreateUserId, curUserId);
                    }
                }}
        );
        return new DatasetCountVO(publicCount, privateCount);
    }

    /**
     * 根据数据集ID获取数据集详情
     *
     * @param datasetId 数据集ID
     * @return dataset 数据集
     */
    @Override
    public Dataset getOneById(Long datasetId) {
        return getById(datasetId);
    }

    /**
     * 条件查询数据集
     *
     * @param datasetQueryWrapper
     * @return List 数据集列表
     */
    @Override
    public List<Dataset> queryList(QueryWrapper<Dataset> datasetQueryWrapper) {
        return list(datasetQueryWrapper);
    }


    /**
     * 导入用户自定义数据集
     *
     * @param datasetCustomCreateDTO 用户导入自定义数据集请求实体
     * @return Long 数据集ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importDataset(DatasetCustomCreateDTO datasetCustomCreateDTO) {
        Dataset dataset = new Dataset(datasetCustomCreateDTO);
        dataset.setUri(fileUtil.getDatasetAbsPath(dataset.getId()));
        dataset.setOriginUserId(JwtUtils.getCurUserId());
        try {
            baseMapper.insert(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR);
        }
        return dataset.getId();
    }


    /**
     * 数据集置顶
     *
     * @param datasetId 数据集id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void topDataset(Long datasetId) {
        if (!exist(datasetId)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        checkPublic(datasetId, OperationTypeEnum.UPDATE);
        Dataset dataset = getBaseMapper().selectById(datasetId);
        boolean isTop = dataset.isTop();
        if (isTop) {
            isTop = false;
        } else {
            isTop = true;
        }
        dataset.setTop(isTop);
        dataset.setUpdateTime(null);
        getBaseMapper().updateById(dataset);
    }

    /**
     * 查询数据集状态
     *
     * @param datasetIds 数据集Id
     * @return Map<Long, IsImportVO> 返回数据集状态
     */
    @Override
    public Map<Long, IsImportVO> determineIfTheDatasetIsAnImport(List<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) {
            return Collections.emptyMap();
        }
        List<Dataset> datasets = new ArrayList<>();
        datasetIds.forEach(datasetId -> {
            Dataset dataset = getBaseMapper().selectById(datasetId);
            datasets.add(dataset);
        });
        Map<Long, IsImportVO> res = new HashMap<>(datasets.size());
        datasets.forEach(dataset -> {
            IsImportVO isImportVO = IsImportVO.builder().build();
            isImportVO.setStatus(dataset.getStatus());
            res.put(dataset.getId(), isImportVO);
        });
        return res;
    }


    /**
     * 整体删除数据还原
     *
     * @param dto 还原实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allRollback(RecycleCreateDTO dto) {
        List<RecycleDetailCreateDTO> detailList = dto.getDetailList();
        if (CollectionUtil.isNotEmpty(detailList)) {
            for (RecycleDetailCreateDTO recycleDetailCreateDTO : detailList) {
                if (!Objects.isNull(recycleDetailCreateDTO) &&
                        RecycleTypeEnum.TABLE_DATA.getCode().compareTo(recycleDetailCreateDTO.getRecycleType()) == 0) {
                    Long datasetId = Long.valueOf(recycleDetailCreateDTO.getRecycleCondition());
                    //根据数据集源ID查询数据集是否存在
                    Dataset dataset = baseMapper.selectById(datasetId);
                    if (!Objects.isNull(dataset) && Objects.isNull(dataset.getSourceId()) && DatasetTypeEnum.PUBLIC.getValue().compareTo(dataset.getType()) == 0) {
                        LogUtil.error(LogEnum.BIZ_DATASET, "预置数据集ID：{} 已存在，禁止还原");
                        throw new BusinessException(ErrorEnum.DATASET_PUBLIC_LIMIT_ERROR);
                    }
                    //还原数据集状态
                    baseMapper.updateStatusById(datasetId, false);
                    //还原数据集标签状态
                    labelService.updateStatusByDatasetId(datasetId, false);
                    //还原数据集版本状态
                    datasetVersionService.updateStatusByDatasetId(datasetId, false);
                    return;
                }
            }

        }

    }


    /**
     * 普通数据集转预置 整个方法异步处理
     *
     * @param datasetConvertPresetDTO 普通数据集转预置请求实体
     */
    @Override
    @RolePermission
    public void convertPreset(DatasetConvertPresetDTO datasetConvertPresetDTO) {
        //1 数据集非空校验/名称校验/状态校验
        Dataset originDataset = verificationDatasetBaseInfo(datasetConvertPresetDTO);
        if (DatatypeEnum.AUTO_IMPORT.getValue().compareTo(originDataset.getAnnotateType()) != 0) {
            //3 校验原版本数据集是否已转换过预制数据集
            List<Dataset> oldDatasets = baseMapper.selectList(new LambdaQueryWrapper<Dataset>().eq(Dataset::getSourceId, datasetConvertPresetDTO.getDatasetId()));
            //4 如已转换， 删除数据集版本文件 数据集 信息 ，删除minio文件数据
            if (!Objects.isNull(oldDatasets)) {
                oldDatasets.forEach(oldDataset -> {
                    try {
                        addRecycleDataByDeleteDataset(oldDataset);
                    } catch (Exception e) {
                        LogUtil.error(LogEnum.BIZ_DATASET, "add recycle task error: {}", e);
                    }
                    oldDataset.setDeleted(true);
                    baseMapper.updateById(oldDataset);
                });
            }
        }
        //5 根据源版本数据集信息构建目标版本数据集信息
        Dataset targetDataset = buildTargetDataset(originDataset, datasetConvertPresetDTO);
        baseMapper.insert(targetDataset);
        targetDataset.setUri(fileUtil.getDatasetAbsPath(targetDataset.getId()));
        updateById(targetDataset);
        Task task = Task.builder()
                .status(TaskStatusEnum.INIT.getValue())
                .datasetId(datasetConvertPresetDTO.getDatasetId())
                .type(MagicNumConstant.ELEVEN)
                .status(MagicNumConstant.ZERO)
                .labels(JSONArray.toJSONString(Collections.emptyList()))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .targetId(targetDataset.getId())
                .versionName(datasetConvertPresetDTO.getVersionName())
                .build();
        taskService.createTask(task);
    }

    /**
     * 备份数据集DB和MINIO数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionFiles  原版本列表
     */
    @Override
    public void backupDatasetDBAndMinioData(Dataset originDataset, Dataset targetDataset, List<DatasetVersionFile> versionFiles) {
        LogUtil.info(LogEnum.BIZ_DATASET, "备份数据集DB和MINIO数据 start");
        String versionName = SymbolConstant.BLANK;
        List<DatasetVersionFile> versionFilesSource = new ArrayList<>();
        versionFiles.forEach(versionFile -> {
            DatasetVersionFile datasetVersionFile = new DatasetVersionFile();
            BeanUtils.copyProperties(versionFile, datasetVersionFile);
            versionFilesSource.add(datasetVersionFile);
        });
        versionName = versionFilesSource.get(MagicNumConstant.ZERO).getVersionName();
        //6 备份数据集标签关系数据
        datasetLabelService.backupDatasetLabelDataByDatasetId(originDataset.getId(), targetDataset);
        //7 备份数据集版本数据
        datasetVersionService.backupDatasetVersionDataByDatasetId(originDataset, targetDataset, originDataset.getCurrentVersionName());
        if (!CollectionUtils.isEmpty(versionFiles)) {
            //8 备份数据集文件数据
            List<File> files = fileService.backupFileDataByDatasetId(originDataset, targetDataset);
            if(targetDataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_CLASSIFICATION.getValue())
                    ||targetDataset.getAnnotateType().equals(AnnotateTypeEnum.TEXT_SEGMENTATION.getValue())
                    ||targetDataset.getAnnotateType().equals(AnnotateTypeEnum.NAMED_ENTITY_RECOGNITION.getValue())){
                Map<String, Long> fileNameMap = files.stream().collect(Collectors.toMap(File::getName, File::getId));
                datasetVersionService.insertEsData(versionName, versionName, originDataset.getId() ,targetDataset.getId(), fileNameMap);
            }
            //9 备份数据集版本文件数据
            datasetVersionFileService.backupDatasetVersionFileDataByDatasetId(originDataset, targetDataset, versionFiles, files);
            //10 数据集需备份标注数据
            dataFileAnnotationService.backupDataFileAnnotationDataByDatasetId(originDataset, targetDataset, versionFiles);
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "备份数据集DB end");
        //11 备份MINIO文件数据
        LogUtil.info(LogEnum.BIZ_DATASET, "备份MINIO数据 start");
        copyMinioData(originDataset, targetDataset, versionName, versionFilesSource);
    }


    /**
     * 清理数据集老数据
     *
     * @param oldDataset 数据集实体
     */
    @Async
    public void clearOldDatasetData(Dataset oldDataset) {
        //异步删除原minio文件数据
        recycleTool.delTempInvalidResources(prefixPath + bucket + SymbolConstant.SLASH + oldDataset.getUri());
    }


    /**
     * 数据集非空校验/名称校验/状态校验
     *
     * @param datasetConvertPresetDTO 普通数据集转预置请求实体
     * @return 原数据集实体
     */
    private Dataset verificationDatasetBaseInfo(DatasetConvertPresetDTO datasetConvertPresetDTO) {
        Dataset originDataset = baseMapper.selectOne(new LambdaQueryWrapper<Dataset>().eq(Dataset::getId, datasetConvertPresetDTO.getDatasetId()));
        if (Objects.isNull(originDataset)) {
            throw new BusinessException("数据集不存在");
        }
        if (Objects.isNull(originDataset.getCurrentVersionName())) {
            throw new BusinessException("数据集未发版");
        }
        if (DatasetTypeEnum.PRIVATE.getValue().compareTo(originDataset.getType()) != 0) {
            throw new BusinessException("只支持我的数据集转预置数据集");
        }
        if (!(DataStateEnum.AUTO_TAG_COMPLETE_STATE.getCode().compareTo(originDataset.getStatus()) == 0 ||
                DataStateEnum.ANNOTATION_COMPLETE_STATE.getCode().compareTo(originDataset.getStatus()) == 0 ||
                DataStateEnum.TARGET_COMPLETE_STATE.getCode().compareTo(originDataset.getStatus()) == 0)) {
            throw new BusinessException("数据集状态不支持转预置");
        }
        return originDataset;
    }


    /**
     * 复制minio文件数据
     *
     * @param originDataset 原数据集实体
     * @param targetDataset 目标数据集实体
     * @param versionName   版本名称
     */
    private void copyMinioData(Dataset originDataset, Dataset targetDataset, String versionName, List<DatasetVersionFile> versionFiles) {
        LogUtil.info(LogEnum.BIZ_DATASET, "复制minio文件数据 start");
        try {
            //文件复制
            List<String> annotationNames = new ArrayList<>();
            List<String> picNames = new ArrayList<>();
            //获取当前版本（新版本）的文件URL
            List<Long> fileIds = new ArrayList<>();
            versionFiles.forEach(dataVersionFile -> fileIds.add(dataVersionFile.getFileId()));
            Set<File> files = fileService.get(fileIds, originDataset.getId());
            files.forEach(file -> {
                picNames.add(StringUtils.substringAfter(file.getUrl(), "/"));
                String fileName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(file.getUrl(), "/"), ".");
                String annotationUrl = originDataset.getUri() + SymbolConstant.SLASH + "versionFile" + SymbolConstant.SLASH +
                        versionName + SymbolConstant.SLASH + "annotation" + SymbolConstant.SLASH + fileName;
                annotationNames.add(annotationUrl);
            });
            String fileTargetDir = targetDataset.getUri() + java.io.File.separator + "origin";
            String fileTargetDirVersion = targetDataset.getUri() + java.io.File.separator + "versionFile" + java.io.File.separator
                    + targetDataset.getCurrentVersionName() + java.io.File.separator + "origin";
            String annotationTargetDir = targetDataset.getUri() + java.io.File.separator + "versionFile" + java.io.File.separator
                    + targetDataset.getCurrentVersionName() + java.io.File.separator + "annotation";
            minioUtil.copyDir(bucket, annotationNames, annotationTargetDir);
            minioUtil.copyDir(bucket, picNames, fileTargetDir);
            minioUtil.copyDir(bucket, picNames, fileTargetDirVersion);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "文件资源复制失败!  error:{}", e);
            throw new BusinessException(ResponseCode.ERROR, e.getMessage());
        }
    }


    /**
     * 构建目标数据集
     *
     * @param originDataset           原数据集实体
     * @param datasetConvertPresetDTO 数据集转预置实体
     * @return 目标数据集实体
     */
    private Dataset buildTargetDataset(Dataset originDataset, DatasetConvertPresetDTO datasetConvertPresetDTO) {
        return Dataset.builder()
                .annotateType(originDataset.getAnnotateType())
                .dataType(originDataset.getDataType())
                .type(MagicNumConstant.TWO)
                .archiveUrl(originDataset.getArchiveUrl())
                .deleted(originDataset.getDeleted())
                .originUserId(MagicNumConstant.ZERO_LONG)
                .currentVersionName(DEFAULT_VERSION)
                .remark(originDataset.getRemark())
                .name(datasetConvertPresetDTO.getName())
                .sourceId(originDataset.getId())
                .isImport(originDataset.isImport())
                .status(originDataset.getStatus())
                .decompressState(originDataset.getDecompressState())
                .decompressFailReason(originDataset.getDecompressFailReason())
                .labelGroupId(originDataset.getLabelGroupId())
                .build();
    }


    /**
     * 根据数据集ID查询数据集是否转换信息
     *
     * @param datasetId 数据集ID
     * @return true: 允许 false: 不允许
     */
    @Override
    @RolePermission
    public Boolean getConvertInfoByDatasetId(Long datasetId) {
        return !Objects.isNull(baseMapper.selectOne(new LambdaQueryWrapper<Dataset>()
                .eq(Dataset::getSourceId, datasetId).eq(Dataset::getDeleted, false)));
    }


    /**
     * 根据数据集ID删除数据信息
     *
     * @param datasetId 数据集ID
     */
    @Override
    public void deleteInfoById(Long datasetId) {
        baseMapper.deleteInfoById(datasetId);
    }


    /**
     * 新增标签数据
     *
     * @param label     标签实体
     * @param datasetId 数据集ID
     */
    public void insertLabelData(Label label, Long datasetId) {
        labelService.insert(label);
        datasetLabelService.insert(DatasetLabel.builder().datasetId(datasetId).labelId(label.getId()).build());
    }


    /**
     * 修改数据集和标签的关系
     *
     * @param dataset          数据集实体
     * @param datasetCreateDTO 数据集修改实体
     * @param datasetId        原数据集ID
     */
    private void doDatasetLabelByUpdate(Dataset dataset, DatasetCreateDTO datasetCreateDTO, Long datasetId) {
        //数据集未标注状态下可操作标签组
        if (DataStateCodeConstant.NOT_ANNOTATION_STATE.compareTo(dataset.getStatus()) == 0) {
            List<Label> labels = labelService.listByGroupId(datasetCreateDTO.getLabelGroupId());
            if (!Objects.isNull(dataset.getLabelGroupId()) &&
                    !dataset.getLabelGroupId().equals(datasetCreateDTO.getLabelGroupId())) {
                //删除原先数据集与标签关系
                datasetLabelService.del(datasetId);
                insertDatasetLabelAndUpdateDataset(labels, datasetCreateDTO, datasetId);
            } else if (Objects.isNull(dataset.getLabelGroupId()) &&
                    !Objects.isNull(datasetCreateDTO.getLabelGroupId())) {
                //新增数据集标签和修改数据集信息
                insertDatasetLabelAndUpdateDataset(labels, datasetCreateDTO, datasetId);
            }
        } else if (!Objects.isNull(dataset.getLabelGroupId()) && !Objects.isNull(datasetCreateDTO.getLabelGroupId()) &&
                !dataset.getLabelGroupId().equals(datasetCreateDTO.getLabelGroupId())) {
            throw new BusinessException(ErrorEnum.LABELGROUP_IN_USE_STATUS);
        }
    }


    /**
     * 新增数据集标签和修改数据集信息
     *
     * @param labels           标签列表
     * @param datasetCreateDTO 数据集修改实体
     * @param datasetId        原数据集ID
     */
    private void insertDatasetLabelAndUpdateDataset(List<Label> labels, DatasetCreateDTO datasetCreateDTO, Long datasetId) {
        //修改数据集标签组ID
        baseMapper.updateById(Dataset.builder().id(datasetId).labelGroupId(datasetCreateDTO.getLabelGroupId()).build());
        //新增数据集标签关系
        if (!CollectionUtils.isEmpty(labels)) {
            datasetLabelService.saveList(
                    labels.stream()
                            .map(a -> DatasetLabel.builder().datasetId(datasetId)
                                    .labelId(a.getId()).build()
                            ).collect(Collectors.toList())
            );
        }
    }

    /**
     * 获取预置数据集列表
     *
     * @return Map<String, Object> 数据集详情
     */
    @Override
    public List<Dataset> getPresetDataset() {
        QueryWrapper<Dataset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", MagicNumConstant.TWO)
        .ne("deleted", MagicNumConstant.ONE);
        return baseMapper.selectList(queryWrapper);
    }
}
