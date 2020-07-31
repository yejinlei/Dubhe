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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.base.ResponseCode;
import org.dubhe.data.constant.*;
import org.dubhe.data.domain.bo.FileBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AnnotationDeleteDTO;
import org.dubhe.data.domain.dto.AnnotationInfoCreateDTO;
import org.dubhe.data.domain.dto.AutoTrackCreateDTO;
import org.dubhe.data.domain.dto.BatchAnnotationInfoCreateDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.vo.FileVO;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.*;
import org.dubhe.data.service.http.AnnotationHttpService;
import org.dubhe.data.service.http.TrackHttpService;
import org.dubhe.data.service.store.IStoreService;
import org.dubhe.data.service.store.MinioStoreServiceImpl;
import org.dubhe.data.util.StatusIdentifyUtil;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import static org.dubhe.constant.PermissionConstant.ADMIN_USER_ID;

/**
 * @description 标注service
 * @date 2020-03-27
 */
@Service
public class AnnotationServiceImpl implements AnnotationService, ApplicationRunner {

    /**
     * 文件信息服务
     */
    @Autowired
    private FileService fileService;

    /**
     * 标注算法调用服务
     */
    @Autowired
    private AnnotationHttpService annotationHttpService;

    /**
     * 跟踪算法调用服务
     */
    @Autowired
    private TrackHttpService trackHttpService;

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
     * 线程池
     */
    @Autowired
    private BasePool pool;

    /**
     * 文件工具类
     */
    @Autowired
    private org.dubhe.data.util.FileUtil fileUtil;

    /**
     * 数据集状态工具类
     */
    @Autowired
    private StatusIdentifyUtil statusIdentifyUtil;

    /**
     * 队列长度
     */
    public static final int QUEUE_SIZE = MagicNumConstant.FIFTY;

    /**
     * 自动标注数量
     */
    public static final int AUTO_ANNOTATING_SIZE = MagicNumConstant.TWENTY;

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
    public int save(BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO) {
        return batchAnnotationInfoCreateDTO.getAnnotations().stream().mapToInt(this::save).sum();
    }

    /**
     * 标注保存实现
     *
     * @param annotationInfoCreateDTO 标注信息
     * @return int 标注修改的数量
     */
    @Override
    public int save(AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        if (annotationInfoCreateDTO.getId() == null) {
            return MagicNumConstant.ZERO;
        }
        FileVO fileVO = fileService.get(annotationInfoCreateDTO.getId());
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());
        datasetService.checkPublic(dataset);
        doSave(annotationInfoCreateDTO);
        return datasetVersionFileService.updateAnnotationStatus(dataset.getId(),
                dataset.getCurrentVersionName(), new HashSet<Long>() {{
                    add(annotationInfoCreateDTO.getId());
                }}, null, FileStatusEnum.FINISHED.getValue());
    }

    /**
     * 标注保存(单个)
     *
     * @param annotationInfoCreateDTO 标注信息
     * @return int 标注成功数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int save(Long fileId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        FileVO fileVO = fileService.get(fileId);
        if (fileVO == null) {
            throw new BusinessException(ErrorEnum.FILE_ABSENT);
        }
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        datasetService.checkPublic(dataset);
        annotationInfoCreateDTO.setId(fileId);
        doSave(annotationInfoCreateDTO);
        return datasetVersionFileService.updateAnnotationStatus(dataset.getId(),
                dataset.getCurrentVersionName(), new HashSet<Long>() {{
                    add(fileId);
                }}, null, FileStatusEnum.ANNOTATING.getValue());
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
        File file = fileService.selectById(annotationInfoCreateDTO.getId());
        Dataset dataset = datasetService.getOneById(file.getDatasetId());
        QueryWrapper<File> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper
                .in("create_user_id", new HashSet<Long>() {{
                    add(dataset.getCreateUserId());
                    add(ADMIN_USER_ID);
                }})
                .eq("id", annotationInfoCreateDTO.getId());
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
    public int finishManual(Long fileId, AnnotationInfoCreateDTO annotationInfoCreateDTO) {
        FileVO fileVO = fileService.get(fileId);
        Dataset dataset = datasetService.getOneById(fileVO.getDatasetId());
        datasetService.checkPublic(dataset);
        annotationInfoCreateDTO.setId(fileId);
        doSave(annotationInfoCreateDTO);
        return datasetVersionFileService.updateAnnotationStatus(dataset.getId(),
                dataset.getCurrentVersionName(), new HashSet<Long>() {{
                    add(annotationInfoCreateDTO.getId());
                }}, null, FileStatusEnum.FINISHED.getValue());
    }

    /**
     * 标注清除
     *
     * @param annotationDeleteDTO 标注清除条件
     * @return boolean 清除标注是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(AnnotationDeleteDTO annotationDeleteDTO) {
        Dataset dataset = datasetService.getOneById(annotationDeleteDTO.getDatasetId());
        //判断数据集标注信息不可以清除的情况
        if (dataset == null) {
            throw new BusinessException(ErrorEnum.DATASET_ABSENT);
        }
        //获取数据集的实时状态
        DatasetStatusEnum status = statusIdentifyUtil.getStatus(dataset);
        switch (status) {
            case INIT:
                throw new BusinessException(ErrorEnum.ANNOTATION_EMPTY_ERROR);
            case AUTO_ANNOTATING:
                throw new BusinessException(ErrorEnum.AUTO_ERROR);
            case NOT_SAMPLE:
                throw new BusinessException(ErrorEnum.DATASET_SAMPLE_IS_UNDONE);
            case SAMPLING:
                throw new BusinessException(ErrorEnum.DATASET_SAMPLING);
            case ENHANCING:
                throw new BusinessException(ErrorEnum.DATASET_ENHANCEMENT);
            default:
                if (dataset.getDataType().equals(DatatypeEnum.VIDEO.getValue()) &&
                        status.getValue() == DatasetStatusEnum.AUTO_FINISHED.getValue()) {
                    throw new BusinessException(ErrorEnum.DATASET_VIDEO_HAS_NOT_BEEN_AUTOMATICALLY_TRACKED);
                }
        }
        //删除数据集标注信息 改数据集相关状态
        deleteMinioAnnotation(dataset);
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
     * 阻塞队列初始化
     *
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        initAnnotationWorker(Constant.ANNOTATION_TASK_WORKER_NUM);
    }

    /**
     * 线程任务提交
     *
     * @param num 线程数量
     */
    private void initAnnotationWorker(int num) {
        while (num-- > MagicNumConstant.ZERO) {
            pool.getExecutor().submit((Runnable) this::annotate);
        }
    }

    /**
     * 任务调用算法
     */
    private void annotate() {
        while (true) {
            if (autoAnnotating.size() > AUTO_ANNOTATING_SIZE) {
                continue;
            }
            try {
                TaskSplitBO t = queue.take();
                annotate(t);
            } catch (InterruptedException e) {
                LogUtil.warn(LogEnum.BIZ_DATASET, "annotation worker has been interrupted. thread:{}", Thread.currentThread());
                return;
            }
        }
    }

    /**
     * 单个标注任务调用算法
     *
     * @param taskSplit 标注任务
     */
    public void annotate(TaskSplitBO taskSplit) {
        LogUtil.info(LogEnum.BIZ_DATASET, "annotate task split. taskSplit:{}", taskSplit);
        if (taskSplit == null || !TaskServiceImpl.taskIds.contains(taskSplit.getTaskId())) {
            return;
        }
        String id = annotationHttpService.annotate(taskSplit);
        if (id != null) {
            // invoke success
            taskSplit.setId(id);
            taskSplit.setSendTime(System.currentTimeMillis());
            autoAnnotating.putIfAbsent(id, taskSplit);
        } else {
            LogUtil.warn(LogEnum.BIZ_DATASET, "task send fail. task:{}", taskSplit);
            doRemoveTask(taskSplit.getTaskId());
        }
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
        //过滤已发送给算法的
        datasets.stream().filter(dataset -> !tracking.contains(dataset.getId())).forEach(this::executionTracking);
    }

    /**
     * 执行自动追踪
     *
     * @param dataset 数据集
     */
    public void executionTracking(Dataset dataset) {
        pool.getExecutor().submit(() -> {
            //查当前版本数据集的版本文件中间表
            Map<Long, List<DatasetVersionFile>> fileMap = queryFileAccordingToCurrentVersionAndStatus(dataset);
            if (fileMap.isEmpty()) {
                LogUtil.info(LogEnum.BIZ_DATASET, "there is currently no data to track");
                return;
            }
            //修改数据集更新时间,以此来保证数据集不会多次调用跟踪接口
            if (!datasetService.updataTimeByIdSet(fileMap)) {
                LogUtil.info(LogEnum.BIZ_DATASET, "dataset modification update time failed.datasetIds:{}", fileMap.keySet().toString());
                return;
            }
            track(fileMap);
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
                .in(Dataset::getStatus, Constant.AUTO_TRACK_NEED_STATUS)
                .lt(Dataset::getUpdateTime, new Timestamp(System.currentTimeMillis() - (MagicNumConstant.ONE_THOUSAND * MagicNumConstant.SIXTY)));
        return datasetService.queryList(datasetQueryWrapper);
    }

    /**
     * 根据当前版本和状态查询文件
     *
     * @param dataset 数据集
     * @return Map<Long, List < DatasetVersionFile>> 根据当前版本和状态查询文件列表
     */
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
                FileStatusEnum.AUTO_ANNOTATION.getValue() != f.getStatus() || FileStatusEnum.FINISHED.getValue() != f.getStatus()).count();
        return size == versionFiles.size() ? versionFiles : null;
    }

    /**
     * 执行目标跟踪
     *
     * @param fileMap 跟踪文件map
     */
    public void track(Map<Long, List<DatasetVersionFile>> fileMap) {
        if (trackHttpService.track(fileMap)) {
            fileMap.forEach((k, v) -> {
                if (tracking.size() < TRACKING_SIZE) {
                    tracking.add(k);
                }
            });
        }
    }

    /**
     * 完成自动标注
     *
     * @param taskSplit 标注任务
     * @param resMap    标注文件保存条件
     */
    @Transactional(rollbackFor = Exception.class)
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
        boolean success = fileService.finishAnnotation(dataset, taskSplit
                .getFiles()
                .stream()
                .map(FileBO::getId).collect(Collectors.toSet())
        );
        if (!success) {
            return;
        }
        //任务加文件数量
        taskService.finishFile(taskSplit.getTaskId(), taskSplit.getFiles().size());
        autoAnnotating.remove(taskSplit.getId());
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
            if (updateDatasetStatusIsFinishAutoTrack(dataset)) {
                boolean update = updateFilesStatusIsFinishAutoTrack(dataset);
                if (update) {
                    tracking.remove(datasetId);
                    LogUtil.info(LogEnum.BIZ_DATASET, "target tracking is complete dataset:{}", datasetId);
                }
            }
        }
        LogUtil.info(LogEnum.BIZ_DATASET, "exception update of target tracking algorithm callback. dataset:{}", datasetId);
    }

    /**
     * 完成跟踪后修改状态
     *
     * @param dataset 数据集
     * @return boolean 状态修改是否成功
     */
    public boolean updateDatasetStatusIsFinishAutoTrack(Dataset dataset) {
        //修改数据集的状态为目标跟踪完成
        return datasetService.updateEntity(null, new UpdateWrapper<Dataset>()
                .lambda()
                .eq(Dataset::getId, dataset.getId())
                .set(Dataset::getStatus, DatasetStatusEnum.FINISHED_TRACK.getValue())
                .set(Dataset::getUpdateUserId, dataset.getCreateUserId())
        );
    }

    /**
     * 更新数据集文件版本状态为自动跟踪
     *
     * @param dataset 数据集
     * @return boolean 状态修改是否成功
     */
    public boolean updateFilesStatusIsFinishAutoTrack(Dataset dataset) {
        QueryWrapper<DatasetVersionFile> fileQueryWrapper = new QueryWrapper<>();
        fileQueryWrapper.lambda().eq(DatasetVersionFile::getDatasetId, dataset.getId());
        String currentVersionName = dataset.getCurrentVersionName();
        if (currentVersionName == null || currentVersionName.isEmpty()) {
            fileQueryWrapper.lambda().isNull(DatasetVersionFile::getVersionName);
        } else {
            fileQueryWrapper.lambda().eq(DatasetVersionFile::getVersionName, currentVersionName);
        }
        List<DatasetVersionFile> datasetVersionFileList = datasetVersionFileService.queryList(fileQueryWrapper);
        return datasetVersionFileService.updateEntity(null, new UpdateWrapper<DatasetVersionFile>().lambda()
                .in(DatasetVersionFile::getFileId, datasetVersionFileList.stream().map(DatasetVersionFile::getFileId)
                        .filter(Objects::nonNull).collect(Collectors.toList())).set(DatasetVersionFile::getAnnotationStatus, FileStatusEnum.FINISH_AUTO_TRACK.getValue()));
    }

    /**
     * 删除minio上标注文件
     *
     * @param dataset 数据集
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMinioAnnotation(Dataset dataset) {
        datasetVersionFileService.updateStatus(dataset, FileStatusEnum.INIT);
        datasetService.updateStatus(dataset.getId(), DatasetStatusEnum.INIT);
        try {
            client.del(bucket, dataset.getUri() + annotation +
                    (dataset.getCurrentVersionName() == null ? "" : (dataset.getCurrentVersionName())));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "MinIO delete the dataset annotation file error", e);
        }
    }

    /**
     * 失败移除任务
     *
     * @param taskId 任务ID
     */
    public void doRemoveTask(Long taskId) {
        taskService.doRemoveTask(taskId, autoAnnotating, null);
    }

}
