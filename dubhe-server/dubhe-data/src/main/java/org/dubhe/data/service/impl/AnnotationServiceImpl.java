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
import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.annotation.DataPermissionMethod;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DataTaskTypeEnum;
import org.dubhe.data.constant.DatatypeEnum;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.vo.FileVO;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.constant.FileStateMachineConstant;
import org.dubhe.data.machine.dto.StateChangeDTO;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.service.*;
import org.dubhe.data.service.store.IStoreService;
import org.dubhe.data.service.store.MinioStoreServiceImpl;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.OperationTypeEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * @description 标注service
 * @date 2020-03-27
 */
@Service
public class AnnotationServiceImpl implements AnnotationService {

    /**
     * 文件信息服务
     */
    @Autowired
    private FileService fileService;

    /**
     * 任务服务类
     */
    @Autowired
    private TaskService taskService;

    /**
     * 数据集服务类
     */
    @Autowired
    private DatasetService datasetService;

    /**
     * 文件存储服务类
     */
    @Resource(type = MinioStoreServiceImpl.class)
    private IStoreService storeService;

    /**
     * 版本文件服务类
     */
    @Autowired
    private DatasetVersionFileService datasetVersionFileService;

    /**
     * minIO桶名
     */
    @Value("${minio.bucketName}")
    private String bucket;

    /**
     * minIO工具类
     */
    @Autowired
    private MinioUtil client;

    /**
     * 标注信息路径
     */
    @Value("${minio.annotation}")
    private String annotation;

    /**
     * 标注任务队列
     */
    static PriorityBlockingQueue<TaskSplitBO> queue;

    /**
     * 自动标注任务
     */
    private ConcurrentHashMap<String, TaskSplitBO> autoAnnotating;

    /**
     * 目标跟踪任务
     */
    private ConcurrentHashSet<Long> tracking;

    /**
     * 文件工具类
     */
    @Autowired
    private org.dubhe.data.util.FileUtil fileUtil;


    /**
     * 队列长度
     */
    public static final int QUEUE_SIZE = MagicNumConstant.FIFTY;

    /**
     * 跟踪数量
     */
    public static final int TRACKING_SIZE = MagicNumConstant.FIVE;


    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        queue = new PriorityBlockingQueue<>(QUEUE_SIZE, Comparator.comparingInt(TaskSplitBO::getPriority).reversed());
        autoAnnotating = new ConcurrentHashMap<>(MagicNumConstant.SIXTEEN);
        tracking = new ConcurrentHashSet<>(MagicNumConstant.SIXTEEN);
    }

    /**
     * 标注保存(分类批量)
     *
     * @param batchAnnotationInfoCreateDTO 标注信息
     * @return int 标注修改的数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void save(Long datasetId,BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        for (AnnotationInfoCreateDTO annotationInfoCreateDTO : batchAnnotationInfoCreateDTO.getAnnotations() ) {
            save(datasetId,annotationInfoCreateDTO);
        }
    }

    /**
     * 标注保存实现
     *
     * @param annotationInfoCreateDTO 标注信息
     * @return int 标注修改的数量
     */
    @Override
    public void save(Long datasetId,AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        FileVO fileVO = fileService.get(annotationInfoCreateDTO.getId(),datasetId);
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        annotationInfoCreateDTO.setDatasetId(datasetId);
        doSave(annotationInfoCreateDTO);
        //改变文件的状态为标注完成
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{new DatasetVersionFile() {{
                setDatasetId(dataset.getId());
                setFileId(fileVO.getId());
                setVersionName(dataset.getCurrentVersionName());
            }}});
            setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
            setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        }});
        //改变数据集的状态为标注完成
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_FINISH_MANUAL_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
    }

    /**
     * 标注保存(单个)
     *
     * @param annotationInfoCreateDTO 标注信息
     * @return int 标注成功数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void save(Long fileId,Long datasetId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        FileVO fileVO = fileService.get(fileId,datasetId);
        if (fileVO == null) {
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());

        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        annotationInfoCreateDTO.setId(fileId);
        annotationInfoCreateDTO.setDatasetId(datasetId);
        doSave(annotationInfoCreateDTO);
        //改变数据集的状态为标注中
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_MANUAL_ANNOTATION_SAVE_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
        //将文件改为标注中的状态
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{new DatasetVersionFile() {{
                setDatasetId(dataset.getId());
                setFileId(fileId);
                setVersionName(dataset.getCurrentVersionName());
            }}});
            setEventMethodName(FileStateMachineConstant.FILE_MANUAL_ANNOTATION_SAVE_EVENT);
            setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        }});
    }

    /**
     * 标注文件保存
     *
     * @param annotationInfoCreateDTO 标注信息
     */
    private void doSave(AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        if (annotationInfoCreateDTO == null || annotationInfoCreateDTO.getAnnotation() == null
                || annotationInfoCreateDTO.getId() == null) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "annotation info invalid. annotation:{}", annotationInfoCreateDTO);
            return;
        }
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper
                .eq("id", annotationInfoCreateDTO.getId()).eq("dataset_id", annotationInfoCreateDTO.getDatasetId());
        File fileOne = fileService.selectOne(fileQueryWrapper);
        if (fileOne == null) {
            LogUtil.warn(LogEnum.BIZ_DATASET, ErrorEnum.FILE_ABSENT.getMsg() + "fileId is" + annotationInfoCreateDTO.getId());
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        datasetService.autoAnnotatingCheck(fileOne);
        String filePath = fileUtil.getAnnotationAbsPath(fileOne.getDatasetId(), fileOne.getName());
        storeService.write(filePath, annotationInfoCreateDTO.getAnnotation());
    }

    /**
     * 标注完成
     *
     * @param annotationInfoCreateDTO 标注信息
     * @param fileId                  文件id
     * @return int 标注完成的数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void finishManual(Long fileId,Long datasetId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        annotationInfoCreateDTO.setDatasetId(datasetId);
        FileVO fileVO = fileService.get(fileId,datasetId);
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());
        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        annotationInfoCreateDTO.setId(fileId);
        doSave(annotationInfoCreateDTO);
        //改变文件的状态为标注完成
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{new DatasetVersionFile() {{
                setDatasetId(dataset.getId());
                setFileId(fileVO.getId());
                setVersionName(dataset.getCurrentVersionName());
            }}});
            setEventMethodName(FileStateMachineConstant.FILE_SAVE_COMPLETE_EVENT);
            setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
        }});
        //改变数据集的状态为标注完成
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_FINISH_MANUAL_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
    }

    /**
     * 重新自动标注
     *
     * @param annotationDeleteDTO 标注清除条件
     * @return boolean 清除标注是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataPermissionMethod
    public void reAuto(AnnotationDeleteDTO annotationDeleteDTO) {
        Dataset dataset = datasetService.getOneById(annotationDeleteDTO.getDatasetId());
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            setEventMethodName(DataStateMachineConstant.DATA_DELETE_ANNOTATING_EVENT);
            setObjectParam(new Object[]{annotationDeleteDTO.getDatasetId().intValue()});
        }});
        //删除数据集标注信息 改数据集相关状态
        deleteMinioAnnotation(dataset);
        taskService.auto(new AutoAnnotationCreateDTO() {{
            setDatasetIds(new Long[]{annotationDeleteDTO.getDatasetId()});
        }});
    }

    /**
     * 标注文件删除
     *
     * @param files 文件set
     */
    public void delete(Set<File> files) {
        files.forEach(this::delete);
    }

    /**
     * 标注文件删除
     *
     * @param file 文件
     */
    public void delete(File file) {
        if (file == null) {
            return;
        }
        String filePath = fileUtil.getAnnotationAbsPath(file.getDatasetId(), file.getName());
        storeService.delete(filePath);
        LogUtil.info(LogEnum.BIZ_DATASET, "delete file. file:{}", filePath);
    }

    /**
     * 获取任务map
     *
     * @return Map<String, TaskSplitBO> 当前正在进行中的任务(已经发送给算法的)
     */
    @Override
    public Map<String, TaskSplitBO> getTaskPool() {
        return autoAnnotating;
    }

    /**
     * 完成自动标注
     *
     * @param taskId                       子任务id
     * @param batchAnnotationInfoCreateDTO 标注信息
     * @return boolean 标注任务完成return true 失败抛出异常
     */
    @Override
    public boolean finishAuto(String taskId, BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        LogUtil.info(LogEnum.BIZ_DATASET, "finishAuto log is:" + taskId, batchAnnotationInfoCreateDTO);
        TaskSplitBO taskSplitBO = autoAnnotating.get(taskId);
        if (taskSplitBO == null) {
            throw new BusinessException(ErrorEnum.TASK_SPLIT_ABSENT);
        }
        doFinishAuto(taskSplitBO, batchAnnotationInfoCreateDTO.toMap());
        return true;
    }

    /**
     * 状态为标记完成的文件推送到追踪算法
     *
     * @return
     */
    public void autoTrack() {
        if (tracking.size() >= TRACKING_SIZE) {
            return;
        }
        //查询要跟踪的数据集
        List<Dataset> datasets = queryDatasetsToBeTracked();
        if (CollectionUtil.isEmpty(datasets)) {
            LogUtil.info(LogEnum.BIZ_DATASET, "there is currently no data to track");
            return;
        }
        datasets.stream().forEach(dataset -> {
            if (CollectionUtil.isEmpty(taskService.getExecutingTask(dataset.getId(), DataTaskTypeEnum.TARGET_TRACK.getValue()))) {
                taskService.track(dataset);
            }
        });
    }

    /**
     * 查询需要目标跟踪的数据集
     *
     * @return List<Dataset> 需要目标跟踪的数据集
     */
    public List<Dataset> queryDatasetsToBeTracked() {
        //读所有数据集
        QueryWrapper<Dataset> datasetQueryWrapper = new QueryWrapper<>();
        datasetQueryWrapper.lambda()
                .eq(Dataset::getDataType, DatatypeEnum.VIDEO.getValue())
                .in(Dataset::getStatus, Constant.AUTO_TRACK_NEED_STATUS);
        return datasetService.queryList(datasetQueryWrapper);
    }

    /**
     * 根据当前版本和状态查询文件
     *
     * @param dataset 数据集
     * @return Map<Long, List < DatasetVersionFile>> 根据当前版本和状态查询文件列表
     */
    @Override
    public Map<Long, List<DatasetVersionFile>> queryFileAccordingToCurrentVersionAndStatus(Dataset dataset) {
        Map<Long, List<DatasetVersionFile>> fileMap = new HashMap<>(MagicNumConstant.SIXTEEN);
        //根据数据集读数据集版本文件中间表
        List<DatasetVersionFile> fileList = filterFilesThatNeedToBeTracked(dataset.getId(), dataset.getCurrentVersionName());
        if (fileList != null) {
            fileMap.put(dataset.getId(), fileList);
        }
        return fileMap;
    }

    /**
     * 筛选需要跟踪的文件
     *
     * @param datasetId   数据集id
     * @param versionName 版本名称
     * @return List<DatasetVersionFile> 版本文件列表
     */
    public List<DatasetVersionFile> filterFilesThatNeedToBeTracked(Long datasetId, String versionName) {
        List<DatasetVersionFile> versionFiles = datasetVersionFileService.getFilesByDatasetIdAndVersionName(datasetId, versionName);
        long size = versionFiles.stream().filter(f ->
                !FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE.equals(f.getStatus()) || FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE.equals(f.getStatus())).count();
        return size == versionFiles.size() ? versionFiles : null;
    }

    /**
     * 完成自动标注
     *
     * @param taskSplit 标注任务
     * @param resMap    标注文件保存条件
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void doFinishAuto(TaskSplitBO taskSplit, Map<Long, AnnotationInfoCreateDTO> resMap) {
        LogUtil.info(LogEnum.BIZ_DATASET, "finish auto. ts:{}, resMap:{}", taskSplit, resMap);
        //图片状态变更为自动标注完成
        Dataset dataset = datasetService.getOneById(taskSplit.getDatasetId());
        //保存标注信息
        taskSplit.getFiles().forEach(fileBO -> {
            AnnotationInfoCreateDTO annotationInfo = resMap.get(fileBO.getId());
            if (annotationInfo == null) {
                return;
            }
            storeService.write(fileUtil.getAnnotationAbsPath(taskSplit.getDatasetId(), fileBO.getName()), annotationInfo.getAnnotation());

        });
        taskSplit.setVersionName(dataset.getCurrentVersionName());

        HashSet<Long> annotationInfoIsNotEmpty = new HashSet<Long>() {{
            addAll(resMap.keySet().stream().filter(k -> !JSON.parseArray(resMap.get(k).getAnnotation()).isEmpty()).collect(Collectors.toSet()));
        }};
        //嵌入状态机（改变文件状态，标记文件状态被改变）->改变有标注数据的文件
        if (!annotationInfoIsNotEmpty.isEmpty()) {
            StateMachineUtil.stateChange(
                    new StateChangeDTO() {{
                        setObjectParam(new Object[]{annotationInfoIsNotEmpty, taskSplit.getDatasetId(), taskSplit.getVersionName()});
                        setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_ANNOTATION_BATCH_EVENT);
                        setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
                    }});
        }
        HashSet<Long> annotationInfoIsEmpty = new HashSet<Long>() {{
            addAll(resMap.keySet().stream().filter(k -> JSON.parseArray(resMap.get(k).getAnnotation()).isEmpty()).collect(Collectors.toSet()));
        }};
        //嵌入状态机（改变文件状态，标记文件状态被改变）->改变无标注数据的文件
        if (!annotationInfoIsEmpty.isEmpty()) {
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{annotationInfoIsEmpty, taskSplit.getDatasetId(), taskSplit.getVersionName()});
                setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_ANNOTATION_INFO_IS_EMPTY_BATCH_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
        }
        //任务加文件数量
        // 判断是否需要去做目标跟踪
        taskService.finishFile(taskSplit.getTaskId(), taskSplit.getFiles().size(), dataset);
    }


    /**
     * 完成目标跟踪
     *
     * @param datasetId          数据集id
     * @param autoTrackCreateDTO 自动跟踪结果
     * @return boolean 目标跟踪是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void finishAutoTrack(Long datasetId, AutoTrackCreateDTO autoTrackCreateDTO) {
        if (!ResponseCode.SUCCESS.equals(autoTrackCreateDTO.getCode())) {
            LogUtil.info(LogEnum.BIZ_DATASET, "auto track is error" + autoTrackCreateDTO.getMsg());
            return;
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "target tracking success modify status");
        Dataset dataset = datasetService.getOneById(datasetId);
        if (dataset == null) {
            LogUtil.error(LogEnum.BIZ_DATASET, "datasetId can't null");
        } else if (!DatatypeEnum.VIDEO.getValue().equals(dataset.getDataType())) {
            LogUtil.error(LogEnum.BIZ_DATASET, "wrong dataset type, not video. dataset:{}", datasetId);
        } else {
            //嵌入状态机（目标跟踪中—>目标跟踪完成）
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(DataStateMachineConstant.DATA_TARGET_COMPLETE_EVENT);
                setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            }});
            //嵌入状态机（自动标注完成->目标跟踪完成）
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(FileStateMachineConstant.FILE_DO_FINISH_AUTO_TRACK_EVENT);
                setStateMachineType(FileStateMachineConstant.FILE_STATE_MACHINE);
            }});
            tracking.remove(datasetId);
            LogUtil.info(LogEnum.BIZ_DATASET, "target tracking is complete dataset:{}", datasetId);
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "exception update of target tracking algorithm callback. dataset:{}", datasetId);
    }

    /**
     * 重新目标跟踪
     *
     * @param datasetId
     */
    @Override
    public void track(Long datasetId) {
        Dataset dataset = datasetService.getOneById(datasetId);
        if (dataset == null|| !DatatypeEnum.VIDEO.getValue().equals(dataset.getDataType())) {
            throw new BusinessException(ErrorEnum.DATASET_TRACK_TYPE_ERROR);
        }
        taskService.track(dataset);
    }

    /**
     * 删除minio上标注文件
     *
     * @param dataset 数据集
     */
    public void deleteMinioAnnotation(Dataset dataset) {
        try {
            client.del(bucket, dataset.getUri() + annotation +
                    (dataset.getCurrentVersionName() == null ? "" : (dataset.getCurrentVersionName())));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset annotation file error", e);
        }
    }

}
