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

package org.dubhe.task.data;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.dubhe.biz.base.constant.DataStateCodeConstant;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.biz.statemachine.dto.StateChangeDTO;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.DatasetEnhanceRequestDTO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.dto.OfRecordTaskDto;
import org.dubhe.data.domain.entity.*;
import org.dubhe.biz.base.vo.DatasetVO;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.*;
import org.dubhe.data.util.TaskUtils;
import org.dubhe.dcm.domain.entity.DataMedicineFile;
import org.dubhe.dcm.service.DataMedicineFileService;
import org.dubhe.task.util.TableDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description 数据集任务处理方法(主要进行任务的拆解和分发)
 * @date 2020-08-27
 */
@Slf4j
@Component
public class DataTaskExecuteThread implements Runnable {

    @Autowired
    private TaskService taskService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private DatasetLabelService datasetLabelService;
    @Autowired
    private DatasetVersionService datasetVersionService;
    @Autowired
    private DatasetVersionFileService datasetVersionFileService;
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private DatasetEnhanceService datasetEnhanceService;
    @Autowired
    private DataMedicineFileService dataMedicineFileService;

    @Resource
    private TaskUtils taskUtils;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${storage.file-store-root-path}")
    private String nfsRootPath;

    @Autowired
    private TableDataUtil tableDataUtil;

    /**
     * 线程池
     */
    @Autowired
    private BasePool pool;

    /**
     * 路径名前缀
     */
    @Value("${storage.file-store-root-path:/nfs/}")
    private String prefixPath;
    /**
     * 标注任务一次查询的数量
     */
    private static final Integer ANNOTATION_BATCH_SIZE = MagicNumConstant.SIXTEEN * MagicNumConstant.TEN_THOUSAND;

    /**
     * 文本分类算法待处理任务队列
     */
    private static final String TC_TASK_QUEUE = "text_classification_task_queue";
    /**
     * 标注算法待处理任务队列
     */
    private static final String ANNOTATION_TASK_QUEUE = "annotation_task_queue";
    /**
     * ImageNet算法待处理任务队列
     */
    private static final String IMAGENET_TASK_QUEUE = "imagenet_task_queue";
    /**
     * ofRecord算法待处理任务队列
     */
    private static final String OFRECORD_TASK_QUEUE = "ofrecord_task_queue";
    /**
     * 目标跟踪算法待处理任务队列
     */
    private static final String TRACK_TASK_QUEUE = "track_task_queue";
    /**
     * 医学标注算法待处理任务队列
     */
    private static final String MEDICINE_PENDING_QUEUE = "dcm_task_queue";

    /**
     * 启动生成任务线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "数据集任务生成");
        thread.start();
    }

    /**
     * 生成任务run方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                work();
                TimeUnit.MILLISECONDS.sleep(MagicNumConstant.ONE_THOUSAND);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get algorithm task failed:{}", e);
            }
        }
    }

    /**
     * 单个任务处理
     */
    public void work() {
        // 获取一个待处理任务
        Task task = taskService.getOnePendingTask();
        if (ObjectUtil.isNotNull(task)) {
            // 执行任务
            execute(task);
        }
    }

    /**
     * 执行任务
     *
     * @param task 任务详情
     */
    public void execute(Task task) {
        // 任务加锁
        int count = taskService.updateTaskStatus(task.getId(), MagicNumConstant.ZERO, MagicNumConstant.ONE);
        if (count != 0) {
            switch (task.getType()) {
                case MagicNumConstant.ZERO:
                    annotationExecute(NumberConstant.NUMBER_0,task);
                    break;
                case MagicNumConstant.ONE:
                    ofRecordExecute(task);
                    break;
                case MagicNumConstant.FOUR:
                    trackExecute(task);
                    break;
                case MagicNumConstant.THREE:
                    enhanceExecute(task);
                    break;
                case MagicNumConstant.FIVE:
                    videoSampleExecute(task);
                    break;
                case MagicNumConstant.SIX:
                    medicineExecute(task);
                    break;
                case MagicNumConstant.SEVEN:
                    textClassificationExecute(task);
                    break;
                case MagicNumConstant.EIGHT:
                    annotationService.deleteAnnotating(task.getDatasetId());
                    annotationExecute(NumberConstant.NUMBER_0,task);
                    break;
                case MagicNumConstant.TEN:
                    csvImport(task);
                    break;
                case MagicNumConstant.ELEVEN:
                    convertPreDataset(task);
                default:
                    LogUtil.info(LogEnum.BIZ_DATASET, "未识别任务");
                    break;
            }
            taskService.updateTaskStatus(task.getId(), MagicNumConstant.ONE, MagicNumConstant.TWO);
        }
    }

    /**
     * 跟踪任务
     *
     * @param task 任务详情
     */
    public void trackExecute(Task task) {
        Dataset dataset = datasetService.getOneById(task.getDatasetId());
        Map<Long, List<DatasetVersionFile>> fileMap = annotationService.queryFileAccordingToCurrentVersionAndStatus(dataset);
        List<File> fileList = datasetVersionFileService.getFileListByVersionFileList(fileMap.get(task.getDatasetId()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", nfsRootPath + bucketName + java.io.File.separator + dataset.getUri() +
                (dataset.getCurrentVersionName() != null ? "/versionFile/" + dataset.getCurrentVersionName() : ""));
        String taskId = UUID.randomUUID().toString();
        jsonObject.put("id", task.getId().toString());
        List<String> images = new ArrayList<>();
        fileList.stream().forEach(file -> {
            images.add(file.getUrl().substring(file.getUrl().lastIndexOf("/") + 1, file.getUrl().length()));
        });
        jsonObject.put("images", images);
        redisUtils.set(taskId, jsonObject);
        redisUtils.zSet(TRACK_TASK_QUEUE, -1, taskId);
    }


    /**
     * 标注任务处理
     *
     * @param task 任务信息
     */
    public void textClassificationExecute(Task task) {
        int offset = 0;
        while (true) {
            if (!generateTextClassificationTask(offset, task)) {
                break;
            }
        }
    }

    /**
     * ofRecord转换任务处理
     *
     * @param task 任务信息
     */
    public void ofRecordExecute(Task task) {
        List<Label> labels = datasetLabelService.listLabelByDatasetId(task.getDatasetId());
        Map<String, String> datasetLabels = new HashMap<>(labels.size());
        labels.forEach(label -> {
            datasetLabels.put(label.getId().toString(), label.getName());
        });
        DatasetVersion datasetVersion = datasetVersionService.detail(task.getDatasetVersionId());
        int partSize = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE + 1;
        int batchSize = task.getTotal() <= partSize ? 1 : (task.getTotal() / partSize);
        Integer offset = 0;
        int partNum = 0;
        while (true) {
            if (task.getTotal() > partSize && partNum == MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE) {
                batchSize = Integer.MAX_VALUE;
            }
            offset = generateOfRecordTask(offset, task, datasetLabels, batchSize, datasetVersion, partNum);
            partNum++;
            if (offset == null) {
                break;
            }
        }
    }

    /**
     * 生成ofRecord任务
     *
     * @param offset         偏移量
     * @param task           任务信息
     * @param datasetLabels  数据集标签
     * @param batchSize      批大小
     * @param datasetVersion 数据集版本
     * @param partNum        part编号
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer generateOfRecordTask(Integer offset, Task task, Map<String, String> datasetLabels, int batchSize, DatasetVersion datasetVersion, int partNum) {
        OfRecordTaskDto ofRecordTaskDto = new OfRecordTaskDto();
        ofRecordTaskDto.setId(task.getId());
        ofRecordTaskDto.setDatasetPath(bucketName + java.io.File.separator + datasetVersion.getVersionUrl());
        ofRecordTaskDto.setDatasetLabels(datasetLabels);
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.getPages(offset, batchSize, task.getDatasetId(), datasetVersion.getVersionName());
        if (CollectionUtil.isNotEmpty(datasetVersionFiles)) {
            offset = offset + datasetVersionFiles.size();
            Set<File> fileSet = fileService.get(datasetVersionFiles.stream().map(datasetVersionFile -> datasetVersionFile.getFileId()).collect(Collectors.toList()), task.getDatasetId());
            List<String> fileNames = fileSet.stream().map(file -> file.getUrl().substring(file.getUrl().lastIndexOf("/") + 1, file.getUrl().length())).collect(Collectors.toList());
            ofRecordTaskDto.setFiles(fileNames);
            ofRecordTaskDto.setPartNum(partNum);
            ofRecordTaskDto.setDatasetVersionId(task.getDatasetVersionId());
            String taskId = UUID.randomUUID().toString();
            ofRecordTaskDto.setReTaskId(taskId);
            redisUtils.zSet(OFRECORD_TASK_QUEUE, -1, taskId);
            redisUtils.set(taskId, ofRecordTaskDto);
            return offset;
        }
        return null;
    }

    /**
     * 生成自动文本分类任务
     *
     * @param offset  偏移量
     * @param task    任务信息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTextClassificationTask(int offset, Task task) {
        List<File> files = fileService.listBatchFile(task.getDatasetId(), offset, ANNOTATION_BATCH_SIZE);
        if (CollectionUtil.isNotEmpty(files)) {
            //处理文件生成任务
            DatasetVO datasetVO = datasetService.get(task.getDatasetId());
            DatasetLabelEnum datasetLabelEnum = datasetService.getDatasetLabelType(task.getDatasetId());
            List<TaskSplitBO> taskSplitBOList = fileService.split(files, task);
            taskSplitBOList.stream().forEach(taskSplitBO -> {
                if (ObjectUtil.isNotNull(datasetLabelEnum)) {
                    taskSplitBO.setDatasetId(datasetVO.getId());
                    taskSplitBO.setVersionName(datasetVO.getCurrentVersionName());
                    taskSplitBO.setLabelType(datasetLabelEnum.getType());
                }
            });

            String queue = TC_TASK_QUEUE;
            redisPipeline(taskSplitBOList, queue);
            if (files.size() < ANNOTATION_BATCH_SIZE) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 生成自动标注任务
     *
     * @param offset  偏移量
     * @param task    任务信息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void annotationExecute(int offset, Task task) {
        List<File> files = fileService.listBatchFile(task.getDatasetId(), offset, ANNOTATION_BATCH_SIZE);
        if (CollectionUtil.isNotEmpty(files)) {
            //处理文件生成任务
            DatasetVO datasetVO = datasetService.get(task.getDatasetId());
            DatasetLabelEnum datasetLabelEnum = datasetService.getDatasetLabelType(task.getDatasetId());
            List<TaskSplitBO> taskSplitBOList = fileService.split(files, task);
            taskSplitBOList.stream().forEach(taskSplitBO -> {
                if (ObjectUtil.isNotNull(datasetLabelEnum)) {
                    taskSplitBO.setDatasetId(datasetVO.getId());
                    taskSplitBO.setVersionName(datasetVO.getCurrentVersionName());
                    taskSplitBO.setLabelType(datasetLabelEnum.getType());
                }
            });
            String queue = ANNOTATION_TASK_QUEUE;
            if (DatasetLabelEnum.IMAGE_NET.getType().equals(taskSplitBOList.get(0).getLabelType())) {
                queue = IMAGENET_TASK_QUEUE;
            }
            redisPipeline(taskSplitBOList, queue);
            if (files.size() >= ANNOTATION_BATCH_SIZE) {
                offset += files.size();
                annotationExecute(offset, task);
            }
        }
    }

    /**
     * redis任务生成
     *
     * @param taskSplitBOList  任务详情
     * @param queue            队列名
     */
    public void redisPipeline(List<TaskSplitBO> taskSplitBOList, String queue) {
        try{
            FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @SneakyThrows
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    redisConnection.openPipeline();
                    redisConnection.multi();
                    for (int i = 0; i < taskSplitBOList.size(); i++) {
                        String taskId = UUID.randomUUID().toString();
                        taskSplitBOList.get(i).setReTaskId(taskId);
                        redisConnection.set(taskId.getBytes("utf-8"), fastJsonRedisSerializer.serialize(taskSplitBOList.get(i)));
                        redisConnection.zAdd(queue.getBytes("utf-8"), i, ("\"" + taskId + "\"").getBytes("utf-8"));
                    }
                    redisConnection.exec();
                    redisConnection.closePipeline();
                    return null;
                }
            });
        } catch (Exception e){
            LogUtil.error(LogEnum.BIZ_DATASET, "redis pipeline error {}", e);
        }
    }

    /**
     * 增强任务
     *
     * @param task 任务详情
     */
    public void enhanceExecute(Task task) {
        Dataset dataset = datasetService.getOneById(task.getDatasetId());
        List<DatasetVersionFile> datasetVersionFiles =
                datasetVersionFileService.getNeedEnhanceFilesByDatasetIdAndVersionName(
                        dataset.getId(),
                        dataset.getCurrentVersionName()
                );
        DatasetEnhanceRequestDTO datasetEnhanceRequestDTO = new DatasetEnhanceRequestDTO();
        datasetEnhanceRequestDTO.setDatasetId(task.getDatasetId());
        datasetEnhanceRequestDTO.setTypes(JSON.parseObject(task.getEnhanceType(), ArrayList.class));
        datasetEnhanceService.commitEnhanceTask(datasetVersionFiles, task, datasetEnhanceRequestDTO);
    }

    /**
     * 采样任务
     *
     * @param task 任务详情
     */
    private void videoSampleExecute(Task task) {
        String samplePendingQueue = "videoSample_task_queue";
        java.io.File file = new java.io.File(prefixPath + task.getUrl());
        int lengthInFrames = 0;
        try {
            FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(file);
            ff.start();
            lengthInFrames = ff.getLengthInVideoFrames();
            ff.stop();
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "get frames error:{}", e);
        }
        List<Integer> frames = new ArrayList<>();
        for (int i = 1; i < lengthInFrames; ) {
            frames.add(i);
            i += task.getFrameInterval();
        }
        List<List<Integer>> framesSplitTasks = CollectionUtil.split(frames, 500);
        taskService.setTaskTotal(task.getId(), framesSplitTasks.size());
        AtomicInteger j = new AtomicInteger(1);
        framesSplitTasks.forEach(framesSplitTask -> {
            JSONObject param = new JSONObject();
            param.put("datasetId", task.getDatasetId() + ":" + j);
            param.put("path", prefixPath + task.getUrl());
            param.put("frames", framesSplitTask);
            param.put("id", task.getId());
            String taskDetails = param.toJSONString();
            JSONObject paramKey = new JSONObject();
            paramKey.put("datasetIdKey", task.getDatasetId() + ":" + j);
            String detailKey = UUID.randomUUID().toString();
            taskUtils.addTask(samplePendingQueue, taskDetails, detailKey, Integer.valueOf(String.valueOf(j)));
            j.addAndGet(1);
        });
    }

    /**
     * 医学标注
     *
     * @param task 任务详情
     */
    private void medicineExecute(Task task) {
        QueryWrapper<DataMedicineFile> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DataMedicineFile::getMedicineId, task.getDatasetId());
        List<DataMedicineFile> dataMedicineFiles = dataMedicineFileService.listFile(wrapper);
        List<List<DataMedicineFile>> medicalTasks = CollectionUtil.split(dataMedicineFiles, 16);
        medicalTasks.forEach(medicalTask -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("taskId", task.getId().toString());
            List<String> dataMedicineFilesPaths = new ArrayList<>();
            medicalTask.forEach(dataMedicineFile -> {
                String dataMedicineFilesPath = "/nfs/" + dataMedicineFile.getUrl();
                dataMedicineFilesPaths.add(dataMedicineFilesPath);
            });
            jsonObject.put("dcms", dataMedicineFilesPaths);
            List<String> medicineFileIds = new ArrayList<>();
            medicalTask.forEach(dataMedicineFile -> medicineFileIds.add(dataMedicineFile.getId().toString()));
            jsonObject.put("medicineFileIds", medicineFileIds);
            String medicineFileUrl = dataMedicineFilesPaths.get(0);
            jsonObject.put("annotationPath", StringUtils.substringBeforeLast(medicineFileUrl, "/")
                    .replace("origin", "annotation"));
            String detailKey = UUID.randomUUID().toString();
            jsonObject.put("reTaskId", detailKey);
            redisUtils.set(detailKey, jsonObject);
            taskUtils.zAdd(MEDICINE_PENDING_QUEUE, detailKey, 10L);
        });
    }

    /**
     * csv导入任务
     *
     * @param task 任务详情
     */
    private void csvImport(Task task) {
        String[] ids = task.getFiles().split(",");
        Long datasetId = task.getDatasetId();
        Dataset dataset = datasetService.getOneById(datasetId);
        for (String id : ids) {
            File file = fileService.selectById(Long.parseLong(id), task.getDatasetId());
            try {
                List<File> files = new ArrayList<>();
                switch (file.getUrl().substring(file.getUrl().lastIndexOf(".") + 1, file.getUrl().length()).toUpperCase()) {
                    case "XLSX":
                        files = tableDataUtil.excelRead(nfsRootPath + java.io.File.separator + file.getUrl(),
                                file.getName(), dataset.getUri() + "/origin/", task.getMergeColumn(), file.getExcludeHeader());
                        break;
                    case "CSV":
                        files = tableDataUtil.csvRead(nfsRootPath + java.io.File.separator + file.getUrl(),
                                file.getName(), dataset.getUri() + "/origin/", task.getMergeColumn(), file.getExcludeHeader());
                        break;
                    default:
                        LogUtil.error(LogEnum.BIZ_DATASET, "import table format not support");
                        break;
                };
                LogUtil.info(LogEnum.BIZ_DATASET, "table import size is {}, datasetid:{}", files.size(), datasetId);
                if(CollectionUtil.isNotEmpty(files)) {
                    LogUtil.info(LogEnum.BIZ_DATASET, "table import save db datasetid:{}", datasetId);
                    List<List<File>> lists = ListUtils.partition(files, NumberConstant.NUMBER_1000 * NumberConstant.NUMBER_3);
                    LogUtil.info(LogEnum.BIZ_DATASET, "table import save db datasetid:{} pars:{}", datasetId, lists.size());
                    for (List<File> el : lists) {
                        List<Long> fileIds = csvImportSaveDb(el, dataset);
                        LogUtil.info(LogEnum.BIZ_DATASET, "table import transport to es datasetid:{}", datasetId);
                        fileService.transportTextToEs(dataset, fileIds,Boolean.FALSE);
                    }
                }
                //-------  导入完成后 更改数据集状态 ---------
                //创建入参请求体
                StateChangeDTO stateChangeDTO = new StateChangeDTO();
                //更新数据集状态为导入中
                stateChangeDTO.setObjectParam(new Object[]{dataset});
                //添加需要执行的状态机类
                stateChangeDTO.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
                //数据集导入完成
                stateChangeDTO.setEventMethodName(DataStateMachineConstant.TABLE_IMPORT_FINISH_EVENT);
                StateMachineUtil.stateChange(stateChangeDTO);
                //-------  导入完成后 更改数据集状态 ---------
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "read csv error {}", e);
            }
        }
    }

    /**
     * 批量保存文件数据到DB
     *
     * @param files 文件数据
     * @param dataset 数据集详情
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> csvImportSaveDb(List<File> files,Dataset dataset) {
        LogUtil.info(LogEnum.BIZ_DATASET, "table import save db start datasetid:{} fileSize:{}", dataset.getId(), files.size());
        List<FileCreateDTO> fileCreateDTOS = new ArrayList<>();
        files.stream().forEach(file -> {
            FileCreateDTO fileCreateDTO = FileCreateDTO.builder().build();
            fileCreateDTO.setName(file.getName());
            fileCreateDTO.setUrl(file.getUrl());
            fileCreateDTOS.add(fileCreateDTO);
        });
        List<File> saveFiles = fileService.saveFiles(dataset.getId(), fileCreateDTOS);
        List<Long> fileIds = new ArrayList<>();
        saveFiles.forEach(file -> fileIds.add(file.getId()));
        //写入版本包
        List<DatasetVersionFile> data = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(saveFiles)) {
            saveFiles.stream().forEach(file -> {
                data.add(new DatasetVersionFile(
                        dataset.getId(),
                        dataset.getCurrentVersionName(),
                        file.getId(),
                        DataStateCodeConstant.NOT_ANNOTATION_STATE,
                        file.getName(),
                        Constant.UNCHANGED));
            });
        }
        datasetVersionFileService.insertList(data);
        LogUtil.info(LogEnum.BIZ_DATASET, "table import save db end datasetid:{} fileSize:{}", dataset.getId(), files.size());
        return fileIds;
    }

    /**
     * 转预置任务
     *
     * @param task 任务详情
     */
    public void convertPreDataset(Task task){
        Dataset originDataset = datasetService.getOneById(task.getDatasetId());
        Dataset targetDataset = datasetService.getOneById(task.getTargetId());
        List<DatasetVersionFile> versionFiles = datasetVersionFileService
                .getDatasetVersionFileByDatasetIdAndVersion(originDataset.getId(), task.getVersionName());
        pool.getExecutor().submit(() -> datasetService.backupDatasetDBAndMinioData(originDataset, targetDataset, versionFiles));
    }

}
