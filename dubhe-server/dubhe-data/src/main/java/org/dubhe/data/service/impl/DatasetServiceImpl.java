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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.catalina.core.ApplicationContext;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.PermissionConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.DatasetMapper;
import org.dubhe.data.dao.DatasetVersionMapper;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.domain.vo.*;
import org.dubhe.data.service.*;
import org.dubhe.data.service.http.DatasetVersionHttpService;
import org.dubhe.data.util.StatusIdentifyUtil;
import org.dubhe.data.util.ZipUtil;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.*;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.dubhe.data.constant.Constant.*;

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
        add(DatasetStatusEnum.INIT.getValue());
        add(DatasetStatusEnum.MANUAL_ANNOTATING.getValue());
        add(DatasetStatusEnum.AUTO_FINISHED.getValue());
        add(DatasetStatusEnum.FINISHED.getValue());
        add(DatasetStatusEnum.NOT_SAMPLE.getValue());
        add(DatasetStatusEnum.FINISHED_TRACK.getValue());
    }};

    /**
     * 执行数据扩容数据集应该具备的状态
     */
    private static final Set<Integer> COMPLETE_STATUS = new HashSet<Integer>() {{
        add(DatasetStatusEnum.AUTO_FINISHED.getValue());
        add(DatasetStatusEnum.FINISHED.getValue());
        add(DatasetStatusEnum.FINISHED_TRACK.getValue());
    }};

    /**
     * 需要同步的状态
     */
    private static final DatasetQueryDTO NEED_SYNC_QUERY = DatasetQueryDTO.builder()
            .status(NEED_SYNC_STATUS).build();

    /**
     * bucket
     */
    @Value("${minio.bucketName}")
    private String bucket;

    /**
     * minIO客户端
     */
    @Autowired
    private MinioUtil client;

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
     * 数据集转换
     */
    @Autowired
    private DatasetConvert datasetConvert;

    /**
     * 数据集标签服务类
     */
    @Autowired
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
    private StatusIdentifyUtil statusIdentifyUtil;

    /**
     * 任务mapper
     */
    @Autowired
    private TaskMapper taskMapper;

    /**
     * 数据增强服务
     */
    @Autowired
    private DatasetEnhanceService datasetEnhanceService;

    /**
     * 数据集标签服务
     */
    @Autowired
    private DatasetLabelService datasetLabelService;

    @Autowired
    private DatasetVersionMapper datasetVersionMapper;

    /**
     * 检测是否为公共数据集
     *
     * @param id 数据集id
     */
    @Override
    public void checkPublic(Long id) {
        Dataset dataset = getById(id);
        checkPublic(dataset);
    }

    /**
     * 检测是否为公共数据集
     *
     * @param dataset 数据集
     */
    @Override
    public void checkPublic(Dataset dataset) {
        if (dataset == null) {
            return;
        }
        if (!DatasetTypeEnum.PUBLIC.getValue().equals(dataset.getType())) {
            return;
        }
        throw new BusinessException(ErrorEnum.DATASET_PUBLIC_ERROR);
    }

    /**
     * 同步状态
     */
    public void syncStatus() {
        List<Dataset> datasets = list(WrapperHelp.getWrapper(NEED_SYNC_QUERY));
        datasets.forEach(dataset -> {
            DatasetStatusEnum datasetStatusEnum = statusIdentifyUtil.getStatus(dataset.getId(), dataset.getCurrentVersionName());
            if (null != datasetStatusEnum && dataset.getStatus() != datasetStatusEnum.getValue()) {
                Dataset datasetOne = new Dataset(dataset.getId(), datasetStatusEnum.getValue());
                LogUtil.info(
                        LogEnum.BIZ_DATASET,
                        "transfer dataset status. id:{}, from:{}, to:{}",
                        dataset.getId(),
                        dataset.getStatus(),
                        datasetOne.getStatus()
                );
                getBaseMapper().updateStatus(datasetOne.getId(), datasetOne.getStatus());
            }
        });
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
                .eq(Dataset::getStatus, DatasetStatusEnum.AUTO_ANNOTATING.getValue());
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
    @Override
    public boolean update(DatasetCreateDTO datasetCreateDTO, Long datasetId) {
        Dataset dataset = DatasetCreateDTO.update(datasetCreateDTO);
        dataset.setId(datasetId);
        int count;
        try {
            count = getBaseMapper().updateById(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR, null, e);
        }
        if (count == MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
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
    public boolean updateStatus(Dataset dataset, DatasetStatusEnum pre) {
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
    public boolean updateStatus(Long id, DatasetStatusEnum to) {
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
    public boolean updateStatus(Long id, DatasetStatusEnum pre, DatasetStatusEnum to) {
        Dataset dataset = new Dataset();
        dataset.setStatus(to.getValue());
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
    public boolean transferStatus(Dataset dataset, DatasetStatusEnum to) {
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
    public boolean transferStatus(Dataset dataset, DatasetStatusEnum pre, DatasetStatusEnum to) {
        if (dataset == null || to == null) {
            return false;
        }
        dataset.setStatus(to.getValue());
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
    public Long saveLabel(Label label, Long datasetId) {
        if (label.getId() == null && StringUtils.isEmpty(label.getName())) {
            throw new BusinessException(ErrorEnum.LABEL_ERROR);
        }
        if (!exist(datasetId)) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        checkPublic(datasetId);
        return labelService.save(label, datasetId);
    }

    /**
     * 获取数据集详情
     *
     * @param datasetId 数据集id
     * @return DatasetVO 数据集详情
     */
    @Override
    public DatasetVO get(Long datasetId) {
        Dataset ds = baseMapper.selectById(datasetId);
        if (ds == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        Map<Long, ProgressVO> statistics = fileService.listStatistics(Arrays.asList(datasetId));
        DatasetVO datasetVO = DatasetVO.from(ds);
        datasetVO.setProgress(statistics.get(datasetVO.getId()));
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
        try {
            save(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR);
        }
        labelService.save(datasetCreateDTO.getLabels(), dataset.getId());
        //预置标签处理
        if (datasetCreateDTO.getPresetLabelType() != null) {
            List<Label> labels = labelService.listByType(datasetCreateDTO.getPresetLabelType());
            if (CollectionUtil.isNotEmpty(labels)) {
                List<DatasetLabel> datasetLabels = new ArrayList<>();
                labels.stream().forEach(label -> {
                    datasetLabels.add(
                            DatasetLabel.builder()
                                    .datasetId(dataset.getId())
                                    .labelId(label.getId())
                                    .build());
                });
                if (CollectionUtil.isNotEmpty(datasetLabels)) {
                    datasetLabelService.saveList(datasetLabels);
                }
            }
        }

        dataset.setUri(fileUtil.getDatasetAbsPath(dataset.getId()));
        if (DatatypeEnum.VIDEO.getValue().equals(datasetCreateDTO.getDataType())) {
            dataset.setStatus(DatasetStatusEnum.NOT_SAMPLE.getValue());
        }
        updateById(dataset);
        return dataset.getId();
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
    public void deleteAll(Long id, Dataset dataset) {
        int count = baseMapper.deleteById(id);
        if (count <= MagicNumConstant.ZERO) {
            throw new BusinessException(ErrorEnum.DATA_ABSENT_OR_NO_AUTH);
        }
        labelService.delDataset(id);
        fileService.delete(id);
        //删除版本数据 版本文件 标注数据
        datasetVersionService.datasetVersionDelete(id);
        datasetVersionFileService.datasetDelete(id);
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
            checkPublic(dataset);
            //删除文件时，需要对文件做标记
            datasetVersionFileService.deleteShip(
                    datasetId,
                    dataset.getCurrentVersionName(),
                    Arrays.asList(fileDeleteDTO.getFileIds())
            );
            List<String> annPaths = new LinkedList<>();
            Arrays.asList(fileDeleteDTO.getFileIds()).forEach(fileId -> {
                File file = fileService.selectById(fileId);
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
                client.delFiles(bucket, annPaths);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "delete error" + e);
                throw new BusinessException(ErrorEnum.FILE_DELETE_ERROR);
            }
        }
    }

    /**
     * 删除数据集
     *
     * @param id 数据集id
     */
    public void delete(Long id) {
        checkPublic(id);
        //数据增强中不可以删除
        Dataset dataset = baseMapper.selectById(id);
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        if (dataset.getStatus().equals(DatasetStatusEnum.ENHANCING.getValue())) {
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
                ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id, dataset);
            } else {
                throw new BusinessException(ErrorEnum.DATASET_VERSION_PTJOB_STATUS);
            }
        } else {
            ((DatasetServiceImpl) AopContext.currentProxy()).deleteAll(id, dataset);
        }
        //删除MinIO文件
        try {
            client.del(bucket, dataset.getUri());
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
     * @return: java.util.Map<java.lang.String, java.lang.Object> 数据集列表
     */
    public Map<String, Object> queryDatasets(Page<Dataset> page, DatasetQueryDTO queryCriteria, Long datasetId) {
        queryCriteria.timeConvert();
        QueryWrapper<Dataset> datasetQueryWrapper = WrapperHelp.getWrapper(queryCriteria);
        datasetQueryWrapper.eq("deleted", MagicNumConstant.ZERO);
        if (datasetId != null) {
            datasetQueryWrapper.or().eq("id", datasetId);
        }
        if (StringUtils.isNotEmpty(queryCriteria.getSort()) && StringUtils.isNotEmpty(queryCriteria.getOrder())) {
            datasetQueryWrapper.orderBy(
                    true,
                    SORT_ASC.equals(queryCriteria.getOrder().toLowerCase()),
                    StringUtils.humpToLine(queryCriteria.getSort())
            );
        } else {
            datasetQueryWrapper.orderByDesc("update_time");
        }

        page = getBaseMapper().listPage(page, datasetQueryWrapper);

        Collection<Long> datasetIds = page.getRecords().stream()
                .mapToLong(Dataset::getId).boxed().collect(Collectors.toList());
        Map<Long, ProgressVO> statistics = fileService.listStatistics(datasetIds);
        List<DatasetVO> data = page.getRecords().stream()
                .map(i -> datasetConvert.toDto(i, statistics.get(i.getId())))
                .collect(Collectors.toList());
        return PageUtil.toPage(page, data);
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
        checkPublic(dataset);
        autoAnnotatingCheck(datasetId);
        List<Long> list = fileService.saveFiles(datasetId, batchFileCreateDTO.getFiles());
        if (!CollectionUtils.isEmpty(list)) {
            List<DatasetVersionFile> datasetVersionFiles = new ArrayList<>();
            for (Long fileId : list) {
                datasetVersionFiles.add(
                        new DatasetVersionFile(datasetId, dataset.getCurrentVersionName(), fileId)
                );
            }
            datasetVersionFileService.insertList(datasetVersionFiles);
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
        checkPublic(datasetId);
        autoAnnotatingCheck(datasetId);
        fileService.isExistVideo(datasetId);
        List<FileCreateDTO> videoFile = new ArrayList<>();
        videoFile.add(fileCreateDTO);
        fileService.saveVideoFiles(datasetId, videoFile, DatatypeEnum.VIDEO.getValue(), PID_OF_VIDEO, null);
        Dataset dataset = new Dataset();
        dataset.setId(datasetId);
        dataset.setStatus(DatasetStatusEnum.SAMPLING.getValue());
        getBaseMapper().updateById(dataset);
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
     * 更新时间
     *
     * @param fileMap 文件map
     * @return boolean 更新结果
     */
    @Override
    public boolean updataTimeByIdSet(Map<Long, List<DatasetVersionFile>> fileMap) {
        Dataset dataset = new Dataset();
        dataset.setId(fileMap.keySet().iterator().next());
        dataset.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return updateById(dataset);
    }

    /**
     * 查询有版本的数据集
     *
     * @param page                分页条件
     * @param datasetIsVersionDTO 查询数据集(有版本)条件
     * @return: Map<String, Object> 查询数据集(有版本)列表
     */
    @Override
    public Map<String, Object> dataVersionlistVO(Page<Dataset> page, DatasetIsVersionDTO datasetIsVersionDTO) {
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
     * @param datasetEnhanceRequestDTO
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
        //只有是完成状态的数据集才可以进行增强操作
        DatasetStatusEnum datasetStatusEnum = statusIdentifyUtil.getStatus(
                datasetEnhanceRequestDTO.getDatasetId(),
                dataset.getCurrentVersionName()
        );
        if (datasetStatusEnum == null || !COMPLETE_STATUS.contains(datasetStatusEnum.getValue())) {
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
                .status(TaskStatusEnum.ING.getValue())
                .datasets(JSON.toJSONString(Arrays.asList(datasetEnhanceRequestDTO.getDatasetId())))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .dataType(dataset.getDataType())
                .labels(JSONArray.toJSONString(Collections.emptyList()))
                .annotateType(dataset.getAnnotateType())
                .finished(MagicNumConstant.ZERO)
                .total(datasetVersionFiles.size() * datasetEnhanceRequestDTO.getTypes().size()).build();
        taskMapper.insert(task);
        //修改数据集状态为数据增强中
        transferStatus(dataset, DatasetStatusEnum.ENHANCING);
        TaskServiceImpl.taskIds.add(task.getId());
        //生成并提交任务
        datasetEnhanceService.commitEnhanceTask(datasetVersionFiles, task, datasetEnhanceRequestDTO);
    }

    /**
     * 数据增强完成
     *
     * @param datasetEnhanceFinishDTO 数据增强回调参数
     * @return boolean 增强结果
     */
    @Override
    public boolean enhanceFinish(DatasetEnhanceFinishDTO datasetEnhanceFinishDTO) {
        datasetEnhanceService.enhanceFinish(datasetEnhanceFinishDTO);
        return true;
    }

    /**
     * 获取数据集标签类型
     *
     * @param datasetId 数据集ID
     * @return DatasetLabelEnum 数据集标签类型
     */
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
        boolean flag =
                userDTO.getId() == PermissionConstant.ANONYMOUS_USER || userDTO.getId() == PermissionConstant.ADMIN_USER_ID;
        Integer publicCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Dataset>()
                        .eq(Dataset::getType, DatasetTypeEnum.PUBLIC.getValue())
        );
        Integer privateCount = flag ?
                baseMapper.selectCount(
                        new LambdaQueryWrapper<Dataset>()
                                .eq(Dataset::getType, DatasetTypeEnum.PRIVATE.getValue())
                )
                :
                baseMapper.selectCount(
                        new LambdaQueryWrapper<Dataset>()
                                .eq(Dataset::getCreateUserId, userDTO.getId())
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
     * 数据集更新
     *
     * @param dataset       数据集对象
     * @param updateWrapper 更新操作类
     * @return boolean 更新是否成功
     */
    @Override
    public boolean updateEntity(Dataset dataset, Wrapper<Dataset> updateWrapper) {
        return update(dataset, updateWrapper);
    }

    /**
     * 导入用户自定义数据集
     * @param datasetCustomCreateDTO 用户导入自定义数据集请求实体
     * @return Long 数据集ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importDataset(DatasetCustomCreateDTO datasetCustomCreateDTO) {
        Dataset dataset = new Dataset(datasetCustomCreateDTO);
        dataset.setUri(fileUtil.getDatasetAbsPath(dataset.getId()));
        try {
            save(dataset);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorEnum.DATASET_NAME_DUPLICATED_ERROR);
        }
        return dataset.getId();
    }

    /**
     * 初始化版本数据
     *
     * @param dataset 数据集
     */
    @Override
    public void initVersion(Dataset dataset) {
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDatasetId(dataset.getId());
        datasetVersion.setVersionName(DEFAULT_VERSION);
        datasetVersion.setVersionNote("");
        datasetVersion.setVersionUrl(DATASET_DIRECTORY + java.io.File.separator + dataset.getId() + VERSION_PATH_NAME + DEFAULT_VERSION);
        datasetVersion.setDataConversion(ConversionStatusEnum.UNABLE_CONVERSION.getValue());
        datasetVersion.setCreateUserId(dataset.getCreateUserId());
        datasetVersionMapper.insert(datasetVersion);
        dataset.setCurrentVersionName(DEFAULT_VERSION);
        dataset.setDecompressState(DatasetDecompressStateEnum.DECOMPRESS_COMPLETE.getValue());
        baseMapper.updateById(dataset);
    }

}
