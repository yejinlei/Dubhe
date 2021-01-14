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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.base.BaseService;
import org.dubhe.base.DataContext;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.NumberConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.domain.vo.*;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.machine.utils.identify.service.StateIdentify;
import org.dubhe.data.service.*;
import org.dubhe.data.service.http.DatasetVersionHttpService;
import org.dubhe.data.util.ZipUtil;
import org.dubhe.domain.dto.CommonPermissionDataDTO;
import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.dto.StateChangeDTO;
import org.dubhe.enums.*;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.*;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    @Value("${k8s.nfs-root-path:/nfs/}")
    private String prefixPath;

    /**
     * 算法调用服务(训练算法)
     */
    @Autowired
    private DatasetVersionHttpService datasetVersionHttpService;

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
    private StateIdentify stateIdentify;

    /**
     * 任务mapper
     */
    @Autowired
    private TaskMapper taskMapper;

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
    private RecycleTaskService recycleTaskService;

    /**
     * 标签组服务
     */
    @Autowired
    private LabelGroupServiceImpl labelGroupService;


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



        List<File> files = fileService.listFile(new QueryWrapper<File>().eq("dataset_id", datasetId));
        if (!dataset.getDataType().equals(datasetCreateDTO.getDataType())
                && !CollectionUtils.isEmpty(files) && datasetCreateDTO.getDataType() != null) {
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
        doDatasetLabelByUpdate(dataset,datasetCreateDTO,datasetId);

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
        if(Objects.isNull(dataset)){
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        checkPublic(dataset, OperationTypeEnum.UPDATE);

        //校验是否是预置数据集
        DatatypeEnum enumValue = DatatypeEnum.getEnumValue(dataset.getDataType());
        List<Label> labelList = labelService.getPubLabels(enumValue.getValue());

        //名称重复性校验
        if(labelService.checkoutLabelIsRepeat(datasetId,label.getName())){
            throw new BusinessException(ErrorEnum.LABEL_NAME_REPEAT);
        }
        if(!CollectionUtils.isEmpty(labelList)){
            Map<String, Long> labelNameMap = labelList.stream().collect(Collectors.toMap(Label::getName, Label::getId));
            if(!Objects.isNull(labelNameMap.get(label.getName()))){
                datasetLabelService.insert(DatasetLabel.builder().datasetId(datasetId).labelId(labelNameMap.get(label.getName())).build());
                datasetGroupLabelService.insert(DatasetGroupLabel.builder().labelGroupId(dataset.getLabelGroupId()).labelId(labelNameMap.get(label.getName())).build());
            }else {
                insertLabelData(label,datasetId);
            }
        }else {
            insertLabelData(label,datasetId);
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
            DatasetVO datasetVO = DatasetVO.from(ds, labelGroup.getName(), labelGroup.getType());
            datasetVO.setProgress(statistics.get(datasetVO.getId()));
            return datasetVO;
        }

        DatasetVO datasetVO = DatasetVO.from(ds, null, null);

        return datasetVO;
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
        org.dubhe.utils.FileUtil.download(zipFile, httpServletResponse);
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
        dataset.setOriginUserId(Objects.isNull(JwtUtils.getCurrentUserDto()) ? null : JwtUtils.getCurrentUserDto().getId());
        try {
            save(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR);
        }
        if(!dataset.isImport()) {
            //新增数据标签关系
            List<Label> labels = labelService.listByGroup(datasetCreateDTO.getLabelGroupId());
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
        }
        dataset.setUri(fileUtil.getDatasetAbsPath(dataset.getId()));
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
        int count = baseMapper.deleteById(id);
        if (count <= MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        //根据数据集ID删除数据集标签关联数据
        labelService.delDataset(id);

        //删除版本数据 标注数据
        datasetVersionService.datasetVersionDelete(id);

        //落地数据文件回收信息
        recycleTaskService.createRecycleTask(RecycleTaskCreateDTO.builder()
                .recycleCustom(RecycleResourceEnum.DATASET_RECYCLE_FILE.getClassName())
                .recycleCondition(id.toString())
                .recycleDelayDate(NumberConstant.NUMBER_1)
                .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                .recycleNote(RecycleResourceEnum.DATASET_RECYCLE_FILE.getMessage())
                .build());

        //落地数据版本文件回收信息
        recycleTaskService.createRecycleTask(RecycleTaskCreateDTO.builder()
                .recycleCustom(RecycleResourceEnum.DATASET_RECYCLE_VERSION_FILE.getClassName())
                .recycleCondition(id.toString())
                .recycleDelayDate(NumberConstant.NUMBER_1)
                .recycleType(RecycleTypeEnum.TABLE_DATA.getCode())
                .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                .recycleNote(RecycleResourceEnum.DATASET_RECYCLE_VERSION_FILE.getMessage())
                .build());
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
            if (DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(dataset.getStatus())) {
                List<Integer> filesStatus = datasetVersionFileService.getFileStatusListByDatasetAndVersion(dataset.getId(), dataset.getCurrentVersionName());
                //构建数据集状态
                StateChangeDTO dto = buildStateChangeDTO(filesStatus, dataset.getId(), dataset.getStatus());
                if (dto != null) {
                    StateMachineUtil.stateChange(dto);
                }
            }
            if (DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE.equals(dataset.getStatus())) {
                List<Integer> status = new ArrayList<>();
                List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.findStatusByDatasetIdAndVersionName(dataset.getId(), dataset.getCurrentVersionName());
                for (DatasetVersionFile datasetVersionFile : datasetVersionFiles) {
                    status.add(datasetVersionFile.getStatus());
                }
                StateChangeDTO dto = buildStateChangeDTO(dataset.getId(), status);
                if(dto != null){
                    StateMachineUtil.stateChange(dto);
                }
            }
            if (DataStateCodeConstant.ANNOTATION_COMPLETE_STATE.equals(dataset.getStatus())) {
                List<Integer> status = new ArrayList<>();
                List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.findStatusByDatasetIdAndVersionName(dataset.getId(), dataset.getCurrentVersionName());
                for (DatasetVersionFile datasetVersionFile : datasetVersionFiles) {
                    status.add(datasetVersionFile.getStatus());
                }
                StateChangeDTO dto = markedCompleteBuildStateChangeDTO(dataset.getId(),status);
                if(dto != null){
                    StateMachineUtil.stateChange(dto);
                }
            }

            List<String> annPaths = new LinkedList<>();
            Arrays.asList(fileDeleteDTO.getFileIds()).forEach(fileId -> {
                File file = fileService.selectById(fileId, fileDeleteDTO.getDatasetIds()[0]);
                String path = org.dubhe.utils.StringUtils.substringBefore(file.getUrl(), ORIGIN_DIRECTORY);
                String annPath = "";
                if (dataset.getCurrentVersionName() != null) {
                    annPath = path + ANNOTATION_DIRECTORY + "/" + dataset.getCurrentVersionName() + "/" + file.getName();
                } else {
                    annPath = path + ANNOTATION_DIRECTORY + "/" + file.getName();
                }
                annPaths.add(org.dubhe.utils.StringUtils.substringAfter(annPath, "/"));
            });
            try {
                //新增文件回收任务
                addRecycleTask(dataset,annPaths);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "delete error" + e);
                throw new BusinessException(ErrorEnum.FILE_DELETE_ERROR);
            }
        }

    }

    /**
     * 新增文件回收任务
     *
     * @param dataset   数据集实体
     * @param annPaths  minio 回收文件路径
     */
    public void addRecycleTask(Dataset dataset,List<String> annPaths ){
        if (!Objects.isNull(dataset.getUri()) && !CollectionUtils.isEmpty(annPaths)) {
            annPaths.stream().forEach(path->{
                recycleTaskService.createRecycleTask(RecycleTaskCreateDTO.builder()
                        .recycleCondition(prefixPath + bucket + SymbolConstant.SLASH + path)
                        .recycleDelayDate(NumberConstant.NUMBER_1)
                        .recycleType(RecycleTypeEnum.FILE.getCode())
                        .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                        .build());
                if(DatatypeEnum.TEXT.getValue().compareTo(dataset.getDataType()) == 0){
                    String afterPath = StringUtils.substringAfterLast(path, SymbolConstant.SLASH);
                    String beforePath = StringUtils.substringBeforeLast(path, ANNOTATION_DIRECTORY);
                    String newPath = beforePath+ORIGIN_DIRECTORY+SymbolConstant.SLASH+ABSTRACT_NAME_PREFIX+afterPath;
                    recycleTaskService.createRecycleTask(RecycleTaskCreateDTO.builder()
                            .recycleCondition(prefixPath + bucket + SymbolConstant.SLASH + newPath)
                            .recycleDelayDate(NumberConstant.NUMBER_1)
                            .recycleType(RecycleTypeEnum.FILE.getCode())
                            .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                            .build());
                }
            });

        }
    }


    /**
     * 构建状态机器参数
     *
     * @param fileStatus 文件状态
     * @param datasetId  数据集ID
     * @return 状态机参数
     */
    public StateChangeDTO buildStateChangeDTO(List<Integer> fileStatus, Long datasetId, Integer originStatus) {

        StateChangeDTO dto = new StateChangeDTO();
        dto.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        dto.setObjectParam(new Object[]{datasetId.intValue()});
        if (DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(originStatus) &&
                fileStatus.contains(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE) &&
                !fileStatus.contains(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)) {
            //标注中 -> 标注完成
            dto.setEventMethodName(DataStateMachineConstant.DATA_MANUAL_ANNOTATION_COMPLETE_EVENT);
            return dto;
        } else if (DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(originStatus) &&
                fileStatus.contains(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE) &&
                !fileStatus.contains(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE)) {
            //标注中 -> 自动标注完成
            dto.setEventMethodName(DataStateMachineConstant.DATA_MANUAL_AUTOMATIC_LABEKING_COMPLETION_EVENT);
            return dto;
        } else if (DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(originStatus) &&
                fileStatus.contains(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE) &&
                !fileStatus.contains(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE) &&
                !fileStatus.contains(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE)) {
            //标注中 -> 未标注
            dto.setEventMethodName(DataStateMachineConstant.DATA_MANUAL_NOT_MARKED_EVENT);
            return dto;
        } else {
            return null;
        }
    }

    /**
     * 构建状态机器参数
     *
     * @param status 文件状态
     * @param datasetId  数据集ID
     * @return
     */
    public StateChangeDTO buildStateChangeDTO(Long datasetId, List<Integer> status) {
        StateChangeDTO dto = new StateChangeDTO();
        dto.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        dto.setObjectParam(new Object[]{datasetId.intValue()});
        if (status.contains(MagicNumConstant.ONE) &&
                !status.contains(MagicNumConstant.ZERO) &&
                !status.contains(MagicNumConstant.TWO)) {
            dto.setEventMethodName(DataStateMachineConstant.DATA_DELETE_PICTRUE__NOT_MARKED_EVENT);
            return dto;
        }
        return null;
    }

    /**
     * 构建状态机器参数
     *
     * @param status 文件状态
     * @param datasetId  数据集ID
     * @return  状态机实体
     */
    public StateChangeDTO markedCompleteBuildStateChangeDTO(Long datasetId, List<Integer> status) {
        StateChangeDTO dto = new StateChangeDTO();
        dto.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        dto.setObjectParam(new Object[]{datasetId.intValue()});
        if (status.contains(MagicNumConstant.ONE) &&
                !status.contains(MagicNumConstant.ZERO) &&
                !status.contains(MagicNumConstant.TWO)) {
            dto.setEventMethodName(DataStateMachineConstant.DATA_DELETE_PICTURE_EVENT);
            return dto;
        }
        return null;
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
        //取出当前数据集信息
        List<DatasetVersionVO> datasetVersionVos = datasetVersionService.versionList(id);
        List<String> datasetVersionUrls = new ArrayList<>();
        datasetVersionVos.forEach(url -> {
            datasetVersionUrls.add(url.getVersionUrl());
            datasetVersionUrls.add(url.getVersionUrl() + StrUtil.SLASH + "ofrecord" + StrUtil.SLASH + "train");
        });
        if (CollectionUtil.isNotEmpty(datasetVersionUrls)) {
            //训练中的url进行比较
            boolean status = datasetVersionHttpService.urlStatus(datasetVersionUrls);
            if (status) {
                ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id);
            } else {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_PTJOB_STATUS);
            }
        } else {
            ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id);
        }
        //删除MinIO文件
        try {
            //落地 minio 数据文件回收信息
            if (!Objects.isNull(dataset.getUri())) {
                recycleTaskService.createRecycleTask(RecycleTaskCreateDTO.builder()
                        .recycleCondition(prefixPath + bucket + "/" + dataset.getUri())
                        .recycleDelayDate(NumberConstant.NUMBER_1)
                        .recycleType(RecycleTypeEnum.FILE.getCode())
                        .recycleModule(RecycleModuleEnum.BIZ_DATASET.getValue())
                        .build());
            }

        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset file error", e);
        }
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
                    DatasetVO datasetVO = DatasetVO.from(dataset, null, null);
                    if(dataset.getCurrentVersionName() != null){
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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadFiles(Long datasetId, BatchFileCreateDTO batchFileCreateDTO) {
        Dataset dataset = getBaseMapper().selectById(datasetId);
        if (null == dataset) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH, "id:" + datasetId, null);
        }
        checkPublic(datasetId, OperationTypeEnum.UPDATE);
        autoAnnotatingCheck(datasetId);
        List<File> list = fileService.saveFiles(datasetId, batchFileCreateDTO.getFiles());
        if (!CollectionUtils.isEmpty(list)) {
            List<DatasetVersionFile> datasetVersionFiles = new ArrayList<>();
            for (File file : list) {
                datasetVersionFiles.add(
                        new DatasetVersionFile(datasetId, dataset.getCurrentVersionName(), file.getId(),file.getName())
                );
            }
            datasetVersionFileService.insertList(datasetVersionFiles);
        }
        if (DataStateCodeConstant.NOT_ANNOTATION_STATE.equals(dataset.getStatus())
                || DataStateCodeConstant.MANUAL_ANNOTATION_STATE.equals(dataset.getStatus())) {
            return;
        }
        StateChangeDTO stateChangeDTO = buildStateChangeDTO(datasetId, dataset.getStatus());
        StateMachineUtil.stateChange(stateChangeDTO);

    }

    /**
     * 构建状态机器参数
     *
     * @param datasetId
     * @param originStatus
     * @return StateChangeDTO
     */
    public StateChangeDTO buildStateChangeDTO(Long datasetId, Integer originStatus) {
        StateChangeDTO dto = new StateChangeDTO();
        dto.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        dto.setObjectParam(new Object[]{datasetId.intValue()});
        if (DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE.equals(originStatus)) {
            //自动标注完成 -> 标注中
            dto.setEventMethodName(DataStateMachineConstant.DATA_UPLOAD_PICTURES_EVENT);
        } else {
            //标注完成 -> 标注中
            dto.setEventMethodName(DataStateMachineConstant.DATA_UPLOAD_SAVE_PICTURE_EVENT);
        }
        return dto;
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
        fileService.isExistVideo(datasetId);
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


        TaskServiceImpl.taskIds.add(task.getId());
        //生成并提交任务
//        datasetEnhanceService.commitEnhanceTask(datasetVersionFiles, task, datasetEnhanceRequestDTO);
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
        UserDTO userDTO = JwtUtils.getCurrentUserDto();
        if (userDTO == null) {
            throw new BusinessException("当前未登录无资源信息");
        }
        Integer publicCount = baseMapper.selectCountByPublic(DatasetTypeEnum.PUBLIC.getValue());
        Integer privateCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Dataset>()
                        .eq(Dataset::getType, DatasetTypeEnum.PRIVATE.getValue()));
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
        dataset.setOriginUserId(Objects.isNull(JwtUtils.getCurrentUserDto()) ?
                null : JwtUtils.getCurrentUserDto().getId());
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
     * @Param datasetId 数据集id
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
     * 新增标签数据
     *
     * @param label      标签实体
     * @param datasetId  数据集ID
     */
    public void insertLabelData(Label label , Long datasetId){
        labelService.insert(label);
        datasetLabelService.insert(DatasetLabel.builder().datasetId(datasetId).labelId(label.getId()).build());
    }


    /**
     * 修改数据集和标签的关系
     *
     * @param dataset           数据集实体
     * @param datasetCreateDTO  数据集修改实体
     * @param datasetId         原数据集ID
     */
    private void doDatasetLabelByUpdate(Dataset dataset, DatasetCreateDTO datasetCreateDTO, Long datasetId){
        //数据集未标注状态下可操作标签组
        if(DataStateCodeConstant.NOT_ANNOTATION_STATE.compareTo(dataset.getStatus()) == 0){
            List<Label> labels = labelService.listByGroup(datasetCreateDTO.getLabelGroupId());
            if(!Objects.isNull(dataset.getLabelGroupId()) &&
                    !dataset.getLabelGroupId().equals(datasetCreateDTO.getLabelGroupId())){
                //删除原先数据集与标签关系
                datasetLabelService.del(datasetId);
                insertDatasetLabelAndUpdateDataset(labels,datasetCreateDTO,datasetId);
            }else if(Objects.isNull(dataset.getLabelGroupId()) &&
                    !Objects.isNull(datasetCreateDTO.getLabelGroupId())){
                //新增数据集标签和修改数据集信息
                insertDatasetLabelAndUpdateDataset(labels,datasetCreateDTO,datasetId);
            }
        }else if(!Objects.isNull(dataset.getLabelGroupId()) &&!Objects.isNull(datasetCreateDTO.getLabelGroupId()) &&
                !dataset.getLabelGroupId().equals(datasetCreateDTO.getLabelGroupId())){
            throw new BusinessException(ErrorEnum.LABELGROUP_IN_USE_STATUS);
        }
    }


    /**
     * 新增数据集标签和修改数据集信息
     *
     * @param labels            标签列表
     * @param datasetCreateDTO  数据集修改实体
     * @param datasetId         原数据集ID
     */
    private void insertDatasetLabelAndUpdateDataset(List<Label> labels ,DatasetCreateDTO datasetCreateDTO, Long datasetId){
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
}
