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
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.domain.bo.DatasetFileBO;
import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.data.domain.dto.DatasetEnhanceFinishDTO;
import org.dubhe.data.domain.dto.DatasetEnhanceRequestDTO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetEnhanceService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.TaskService;
import org.dubhe.data.service.http.EnhanceHttpService;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

/**
 * @description 数据集增强
 * @date 2020-06-28
 */
@Service
public class DatasetEnhanceServiceImpl implements DatasetEnhanceService {

    static PriorityBlockingQueue<EnhanceTaskSplitBO> queue;
    private ConcurrentHashMap<String, EnhanceTaskSplitBO> enhancing;
    @Autowired
    private BasePool pool;
    @Autowired
    private EnhanceHttpService enhanceHttpService;
    @Autowired
    private DatasetVersionFileService datasetVersionFileService;

    @Autowired
    private FileService fileService;
    @Autowired
    @Lazy
    private TaskService taskService;
    @Value("${data.annotation.task.splitSize:16}")
    private Integer taskSplitSize;
    @Value("${k8s.nfs-root-path}")
    private String nfs;
    @Value("${minio.bucketName}")
    private String bucketName;

    public static final int QUEUE_SIZE = MagicNumConstant.FIFTY;
    private static int ENHANCE_TASK_WORKER_NUM = MagicNumConstant.TEN;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        queue = new PriorityBlockingQueue<>(QUEUE_SIZE, Comparator.comparingInt(EnhanceTaskSplitBO::getPriority).reversed());
        enhancing = new ConcurrentHashMap<>(MagicNumConstant.SIXTEEN);
        while (ENHANCE_TASK_WORKER_NUM-- > MagicNumConstant.ZERO) {
            pool.getExecutor().submit((Runnable) this::enhance);
        }
    }

    /**
     * 增强
     */
    private void enhance() {
        while (true) {
            try {
                EnhanceTaskSplitBO enhanceTaskSplitBO = queue.take();
                LogUtil.info(LogEnum.BIZ_DATASET, "get one enhance task");
                enhance(enhanceTaskSplitBO);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "data enhance thread exception to terminate {}, {}", Thread.currentThread(), e);
                return;
            }
        }
    }

    /**
     * 任务处理
     *
     * @param enhanceTaskSplitBO 增强任务条件
     */
    private void enhance(EnhanceTaskSplitBO enhanceTaskSplitBO) {
        if (!TaskServiceImpl.taskIds.contains(enhanceTaskSplitBO.getId())) {
            return;
        }
        String id = enhanceHttpService.enhance(enhanceTaskSplitBO);
        if (StringUtils.isNotEmpty(id)) {
            enhanceTaskSplitBO.setSendTime(System.currentTimeMillis());
            enhancing.putIfAbsent(id, enhanceTaskSplitBO);
        } else {
            LogUtil.warn(LogEnum.BIZ_DATASET, "enhancingTask send fail. task:{}", enhanceTaskSplitBO);
            doRemoveTask(enhanceTaskSplitBO.getId());
        }
    }

    /**
     * 提交任务
     *
     * @param datasetVersionFiles      数据版本文件关系列表
     * @param task                     任务
     * @param datasetEnhanceRequestDTO 数据增强请求条件
     */
    @Override
    public void commitEnhanceTask(List<DatasetVersionFile> datasetVersionFiles, Task task, DatasetEnhanceRequestDTO datasetEnhanceRequestDTO) {
        Set<File> fileSet = fileService.get(datasetVersionFiles.stream().
                map(datasetVersionFile -> datasetVersionFile.getFileId()).collect(Collectors.toList()));
        Map<Long, Integer> fileAnnotationStatus = new HashMap<>(datasetVersionFiles.size());
        datasetVersionFiles.stream().forEach(datasetVersionFile -> {
            fileAnnotationStatus.put(datasetVersionFile.getFileId(), datasetVersionFile.getAnnotationStatus());
        });
        List<EnhanceTaskSplitBO> tasks = new ArrayList<>();
        for (int i = MagicNumConstant.ZERO; i < datasetEnhanceRequestDTO.getTypes().size(); i++) {
            List<List<File>> files = CollectionUtil.split(fileSet, taskSplitSize);
            for (List<File> list : files) {
                EnhanceTaskSplitBO enhanceTaskSplitBO = new EnhanceTaskSplitBO(task.getId(), list, nfs, bucketName,
                        datasetVersionFiles.get(MagicNumConstant.ZERO).getDatasetId(), datasetVersionFiles.get(MagicNumConstant.ZERO).getVersionName(),
                        datasetEnhanceRequestDTO, fileAnnotationStatus, datasetEnhanceRequestDTO.getTypes().get(i));
                tasks.add(enhanceTaskSplitBO);
            }
        }
        pool.getExecutor().submit(() -> {
            queue.addAll(tasks);
        });
    }

    /**
     * 增强任务完成
     *
     * @param datasetEnhanceFinishDTO 数据增强回调条件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enhanceFinish(DatasetEnhanceFinishDTO datasetEnhanceFinishDTO) {
        EnhanceTaskSplitBO enhanceTaskSplitBO = enhancing.get(datasetEnhanceFinishDTO.getId());
        if (ObjectUtil.isNull(enhanceTaskSplitBO)) {
            throw new BusinessException(ErrorEnum.TASK_SPLIT_ABSENT);
        }
        Integer fileNum = enhanceTaskSplitBO.getFileDtos().size();
        LogUtil.info(LogEnum.BIZ_DATASET, "enhance finish file count {}", fileNum);
        //写入图片到data_file表
        List<DatasetFileBO> datasetFileBOList = enhanceTaskSplitBO.getFileDtos();
        List<FileCreateDTO> fileDTOList = Lists.transform(datasetFileBOList, new Function<DatasetFileBO, FileCreateDTO>() {
            @Nullable
            @Override
            public FileCreateDTO apply(@Nullable DatasetFileBO datasetFileBO) {
                return new FileCreateDTO(enhanceTaskSplitBO.createEnhanceFilePath(datasetEnhanceFinishDTO.getSuffix(), datasetFileBO).replaceFirst(nfs, ""),
                        datasetFileBO.getFileId(), datasetFileBO.getAnnotationStatus(), enhanceTaskSplitBO.getType(), enhanceTaskSplitBO.getUserId(),
                        datasetFileBO.getWidth(), datasetFileBO.getHeight());
            }
        });
        Map<Long, Integer> fileStatus = new HashMap<>(fileDTOList.size());
        datasetFileBOList.stream().forEach(datasetFileBO -> {
            fileStatus.put(datasetFileBO.getFileId(), datasetFileBO.getAnnotationStatus());
        });
        List<Long> fileIds = fileService.saveFiles(enhanceTaskSplitBO.getDatasetId(), fileDTOList);
        LogUtil.info(LogEnum.BIZ_DATASET, "enhance finish file save success {}", datasetEnhanceFinishDTO.getId());
        Set<File> files = fileService.get(fileIds);
        //文件写入关系表
        datasetVersionFileService.insertList(Lists.transform(new ArrayList<>(files), new Function<File, DatasetVersionFile>() {
            @Nullable
            @Override
            public DatasetVersionFile apply(@Nullable File file) {
                return new DatasetVersionFile(enhanceTaskSplitBO.getDatasetId(), enhanceTaskSplitBO.getVersionName(), file.getId(), fileStatus.get(file.getPid()));
            }
        }));
        LogUtil.info(LogEnum.BIZ_DATASET, "enhance finish version file save success {}", datasetEnhanceFinishDTO.getId());
        taskService.finishTaskFile(enhanceTaskSplitBO.getId(), fileNum);
        enhancing.remove(datasetEnhanceFinishDTO.getId());
    }

    /**
     * 失败移除任务
     */
    public void doRemoveTask(Long taskId) {
        taskService.doRemoveTask(taskId, null, enhancing);
    }
}
