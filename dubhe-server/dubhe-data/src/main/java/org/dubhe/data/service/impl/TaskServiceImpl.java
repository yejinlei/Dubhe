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

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.constant.DatasetStatusEnum;
import org.dubhe.data.constant.ErrorEnum;
import org.dubhe.data.constant.TaskStatusEnum;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.File;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetLabelService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.TaskService;
import org.dubhe.enums.LogEnum;
import org.dubhe.data.util.StatusIdentifyUtil;
import org.dubhe.exception.BusinessException;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @description 标注任务信息服务实现类
 * @date 2020-04-10
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService, ApplicationRunner {

    private static final Set<Integer> NEED_AUTO_ANNOTATE = new HashSet<Integer>() {{
        add(DatasetStatusEnum.INIT.getValue());
        add(DatasetStatusEnum.MANUAL_ANNOTATING.getValue());
    }};

    /**
     * 任务不更新时间超过failTime，则会变成失败。单位s
     */
    @Value("${data.annotation.task.failTime}")
    private Integer failTime;

    @Autowired
    private FileService fileService;
    @Autowired
    private DatasetServiceImpl datasetService;
    @Autowired
    private DatasetLabelService datasetLabelService;
    @Autowired
    private BasePool pool;
    @Autowired
    private StatusIdentifyUtil statusIdentifyUtil;

    public static ConcurrentHashSet<Long> taskIds = new ConcurrentHashSet<>();

    /**
     * 十分钟(单位ms)
     */
    private final static Long FAIL_TIME = 600000L;

    /**
     * 暂时只支持数据集的提交
     *
     * @param autoAnnotationCreateDTO 自动标注dto
     * @return List<Long> 自动标注生成的父任务id列表
     */
    @Override
    public List<Long> auto(AutoAnnotationCreateDTO autoAnnotationCreateDTO) {
        return create(autoAnnotationCreateDTO);
    }

    /**
     * 暂时只支持数据集的提交
     *
     * @param autoAnnotationCreateDTO 自动标注dto
     * @return List<Long> 自动标注生成的父任务id列表
     */
    public List<Long> create(AutoAnnotationCreateDTO autoAnnotationCreateDTO) {
        if (ArrayUtil.isEmpty(autoAnnotationCreateDTO.getDatasetIds())) {
            return Collections.emptyList();
        }
        return Arrays.stream(autoAnnotationCreateDTO.getDatasetIds()).map(autoAnnotationCreate -> create(autoAnnotationCreate)).collect(Collectors.toList());
    }

    /**
     * 暂时一个任务只包含一个数据集
     * 只对未标注和手动标注中的数据集进行标注
     * 只对未标注状态的文件进行标注
     * 如果待标注的文件为空，则抛异常
     *
     * @param datasetId 数据集id
     * @return Long 父任务id
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long datasetId) {
        if (datasetId == null) {
            return MagicNumConstant.ZERO_LONG;
        }

        Dataset dataset = datasetService.getById(datasetId);

        datasetService.checkPublic(dataset);
        //当前不是手动标注和未标注报错
        if (dataset == null || !NEED_AUTO_ANNOTATE.contains(dataset.getStatus())) {
            throw new BusinessException(ErrorEnum.AUTO_DATASET_ERROR);
        }
        List<Long> datasetIds = Arrays.asList(datasetId);
        Set<File> files = fileService.toFiles(datasetIds, dataset, Constant.AUTO_ANNOTATION_NEED_STATUS, true);

        if (CollectionUtils.isEmpty(files)) {
            throw new BusinessException(ErrorEnum.AUTO_FILE_EMPTY);
        }

        List<Label> labels = datasetLabelService.listLabelByDatasetId(datasetId);
        if (CollectionUtils.isEmpty(labels) ||
                CollectionUtils.isEmpty(labels.stream().filter(label -> (!label.getType().equals(DatasetLabelEnum.CUSTOM))).collect(Collectors.toList()))) {
            throw new BusinessException(ErrorEnum.AUTO_LABEL_EMPTY_ERROR);
        }
        List<Long> labelIds = new ArrayList<>();
        labels.stream().forEach(label -> {
            labelIds.add(label.getId());
        });
        Task task = Task.builder()
                .status(TaskStatusEnum.ING.getValue())
                .datasets(JSON.toJSONString(datasetIds))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .dataType(dataset.getDataType())
                .labels(JSON.toJSONString(labelIds))
                .annotateType(dataset.getAnnotateType())
                .finished(MagicNumConstant.ZERO)
                .total(files.size()).build();
        baseMapper.insert(task);

        start(dataset);
        commit(files, task, dataset);
        taskIds.add(task.getId());
        return task.getId();
    }

    /**
     * 任务开始
     *
     * @param dataset 数据集
     */
    public void start(Dataset dataset) {
        if (dataset == null) {
            return;
        }
        datasetService.transferStatus(dataset, DatasetStatusEnum.AUTO_ANNOTATING);
    }

    /**
     * 任务失败
     */
    @Override
    public void fail() {
        QueryWrapper<Task> taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.lambda().select(Task::getId, Task::getDatasets).eq(Task::getStatus, TaskStatusEnum.ING.getValue())
                .lt(Task::getUpdateTime, LocalDateTime.now().minusSeconds(failTime));
        List<Task> tasks = getBaseMapper().selectList(taskQueryWrapper);
        tasks.forEach(this::fail);
    }

    /**
     * 任务失败
     *
     * @param task 只能有id，或者其它字段与数据库保持一致，否则会被写入数据库
     */
    public void fail(Task task) {
        task.setStatus(TaskStatusEnum.FAIL.getValue());
        getBaseMapper().updateById(task);

        List<Long> datasetIds = JSON.parseArray(task.getDatasets(), Long.class);
        if (CollectionUtils.isEmpty(datasetIds)) {
            return;
        }
        datasetIds.forEach(i -> {
                    datasetService.updateStatus(i,
                            statusIdentifyUtil.failTaskGetStatus(i, datasetService.getById(i).getCurrentVersionName())
                    );
                }
        );
    }

    /**
     * 只允许按数据集的方式提交任务，如果提交零散文件，会导致状态出错
     *
     * @param dataset 其中属性除了状态，不持久化的属性不可更改，否则会写入数据库
     */
    public void finish(Dataset dataset) {
        if (dataset == null) {
            return;
        }
        if (fileService.hasManualAnnotating(dataset.getId())) {
            datasetService.transferStatus(dataset, DatasetStatusEnum.MANUAL_ANNOTATING);
            return;
        }
        datasetService.transferStatus(dataset, DatasetStatusEnum.AUTO_FINISHED);
    }

    /**
     * 完成文件
     *
     * @param taskId 任务id
     */
    @Override
    public void finishFile(Long taskId, Integer filesCount) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        getBaseMapper().finishFile(taskId, filesCount);
        task = baseMapper.selectById(taskId);
        if (isDatasetFinish(task)) {
            finishDataset(task);
        }
    }

    /**
     * 数据集任务完成
     *
     * @param task 任务
     */
    private void finishDataset(Task task) {
        List<Long> datasetIds = JSON.parseArray(task.getDatasets(), Long.class);
        taskIds.remove(task.getId());
        datasetIds.forEach(i -> {
            Dataset dataset = datasetService.getById(i);
            finish(dataset);
        });

        task.setStatus(TaskStatusEnum.FINISHED.getValue());
        getBaseMapper().updateById(task);
    }

    /**
     * 一个任务只对应一个数据集，完成则数据集完成，task也完成
     *
     * @param task task 任务
     * @return boolean 完成结果
     */
    private boolean isDatasetFinish(Task task) {
        return task.getFinished() >= task.getTotal();
    }

    /**
     * recommit unfinished task
     */
    private void resume() {
        List<Task> tasks = listUnfinished();
        commit(tasks);
    }

    /**
     * 提交任务
     *
     * @param tasks 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void commit(List<Task> tasks) {
        for (Task task : tasks) {
            List<Long> datasetIds = JSON.parseArray(task.getDatasets(), Long.class);
            Dataset dataset = datasetService.getOneById(datasetIds.get(MagicNumConstant.ZERO));
            Set<File> files = fileService.toFiles(null, dataset, Constant.AUTO_ANNOTATION_NEED_STATUS, true);
            commit(files, task, dataset);
        }
    }

    /**
     * 同一个数据集的任务需一起提交
     * @param files
     * @param task
     * @param dataset
     */
    public void commit(Collection<File> files, Task task, Dataset dataset) {
        LogUtil.info(LogEnum.BIZ_DATASET, "commit files size:{}", files == null ? MagicNumConstant.ZERO : files.size());
        if (CollectionUtils.isEmpty(files)) {
            return;
        }

        pool.getExecutor().submit(() -> {
            DatasetLabelEnum datasetLabelEnum = datasetService.getDatasetLabelType(files.stream().findFirst().get().getDatasetId());
            List<TaskSplitBO> ts = fileService.split(files, task);
            ts.stream().forEach(taskSplitBO -> {
                if (ObjectUtil.isNotNull(datasetLabelEnum)) {
                    taskSplitBO.setDatasetId(dataset.getId());
                    taskSplitBO.setVersionName(dataset.getCurrentVersionName());
                    taskSplitBO.setLabelType(datasetLabelEnum.getType());
                }
            });
            AnnotationServiceImpl.queue.addAll(ts);
            LogUtil.info(LogEnum.BIZ_DATASET, "commit task. ts:{}", ts);
        });
    }

    /**
     * 未完成任务列表
     *
     * @return List<Task> 未完成任务列表
     */
    public List<Task> listUnfinished() {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Task::getStatus, TaskStatusEnum.ING.getValue());
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * recommit unfinished task
     *
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        pool.getExecutor().submit(this::resume);
    }

    /**
     * 完成任务的文件
     *
     * @param taskId  任务id
     * @param fileNum 文件数量
     */
    @Override
    public void finishTaskFile(Long taskId, Integer fileNum) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        getBaseMapper().finishFileNum(taskId, fileNum);
        task = baseMapper.selectById(taskId);
        if (isDatasetFinish(task)) {
            finishDataset(task);
        }
    }


    /**
     * 任务失败
     *
     * @param id             任务id 为空则代表是定时任务
     * @param autoAnnotating 自动标注
     * @param enhancing      数据增强
     */
    @Override
    public void doRemoveTask(Long id, ConcurrentHashMap<String, TaskSplitBO> autoAnnotating, ConcurrentHashMap<String, EnhanceTaskSplitBO> enhancing) {
        //删除子任务
        if (autoAnnotating != null) {
            autoAnnotating.forEach((k, v) -> {
                if (id == null ? v.getSendTime() < System.currentTimeMillis() - FAIL_TIME : v.getTaskId().equals(id)) {
                    autoAnnotating.remove(k);
                    LogUtil.warn(LogEnum.BIZ_DATASET, "the autoAnnotating task was removed :{}", v);
                    if (id == null) {
                        taskIds.remove(v.getTaskId());
                        fail(getById(v.getTaskId()));
                    }
                }
            });
        }
        if (enhancing != null) {
            enhancing.forEach((k, v) -> {
                if (id == null ? v.getSendTime() < System.currentTimeMillis() - FAIL_TIME : v.getId().equals(id)) {
                    enhancing.remove(k);
                    LogUtil.warn(LogEnum.BIZ_DATASET, "the enhancing task was removed :{}", v);
                    if (id == null) {
                        taskIds.remove(v.getId());
                        fail(getById(v.getId()));
                    }
                }
            });
        }
        if (id != null) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "the task was removed :{}", id);
            taskIds.remove(id);
            fail(getById(id));
        }

    }

}
