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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.NumberConstant;
import org.dubhe.data.constant.*;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;
import org.dubhe.data.domain.entity.*;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.data.machine.constant.DataStateMachineConstant;
import org.dubhe.data.machine.constant.FileStateCodeConstant;
import org.dubhe.data.machine.dto.StateChangeDTO;
import org.dubhe.data.machine.enums.DataStateEnum;
import org.dubhe.data.machine.utils.StateMachineUtil;
import org.dubhe.data.machine.utils.identify.service.StateIdentify;
import org.dubhe.data.pool.BasePool;
import org.dubhe.data.service.DatasetLabelService;
import org.dubhe.data.service.DatasetVersionFileService;
import org.dubhe.data.service.FileService;
import org.dubhe.data.service.TaskService;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.OperationTypeEnum;
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
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private static final Set<Integer> NEED_AUTO_ANNOTATE = new HashSet<Integer>() {{
        add(DataStateCodeConstant.NOT_ANNOTATION_STATE);
        add(DataStateCodeConstant.MANUAL_ANNOTATION_STATE);
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
    private DatasetVersionFileService datasetVersionFileService;
    @Autowired
    private BasePool pool;
    @Autowired
    private StateIdentify stateIdentify;

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
        return Arrays.stream(autoAnnotationCreateDTO.getDatasetIds()).map(this::create).collect(Collectors.toList());
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

        datasetService.checkPublic(dataset, OperationTypeEnum.UPDATE);
        //当前不是手动标注和未标注报错
        if (dataset == null || !NEED_AUTO_ANNOTATE.contains(dataset.getStatus())) {
            throw new BusinessException(ErrorEnum.AUTO_DATASET_ERROR);
        }
        List<Long> datasetIds = Arrays.asList(datasetId);
        LambdaQueryWrapper<DatasetVersionFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DatasetVersionFile::getDatasetId,dataset.getId());
        if ((dataset.getCurrentVersionName() == null)) {
            wrapper.isNull(DatasetVersionFile::getVersionName);
        } else {
            wrapper.eq(DatasetVersionFile::getVersionName, dataset.getCurrentVersionName());
        }
        wrapper.eq(DatasetVersionFile::getAnnotationStatus,DataStateEnum.NOT_ANNOTATION_STATE.getCode());
        wrapper.ne(DatasetVersionFile::getStatus,NumberConstant.NUMBER_1);
        Integer filesCount = datasetVersionFileService.getFileCountByDatasetIdAndVersion(wrapper);
        if (filesCount<NumberConstant.NUMBER_1) {
            throw new BusinessException(ErrorEnum.AUTO_FILE_EMPTY);
        }

        List<Label> labels = datasetLabelService.listLabelByDatasetId(datasetId);
        if (CollectionUtils.isEmpty(labels) ||
                CollectionUtils.isEmpty(labels.stream().filter(label -> (!label.getType().equals(DatasetLabelEnum.CUSTOM))).collect(Collectors.toList()))) {
            throw new BusinessException(ErrorEnum.AUTO_LABEL_EMPTY_ERROR);
        }
        List<Long> labelIds = new ArrayList<>();
        labels.forEach(label -> {
            labelIds.add(label.getId());
        });

        Task task = Task.builder()
                .status(TaskStatusEnum.INIT.getValue())
                .datasets(JSON.toJSONString(datasetIds))
                .files(JSON.toJSONString(Collections.EMPTY_LIST))
                .dataType(dataset.getDataType())
                .labels(JSON.toJSONString(labelIds))
                .annotateType(dataset.getAnnotateType())
                .finished(MagicNumConstant.ZERO)
                .total(filesCount)
                .datasetId(datasetId)
                .type(DataTaskTypeEnum.ANNOTATION.getValue())
                .build();
        baseMapper.insert(task);

        //嵌入状态机
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            setEventMethodName(DataStateMachineConstant.DATA_AUTO_ANNOTATIONS_EVENT);
            setObjectParam(new Object[]{dataset.getId().intValue()});
        }});
        return task.getId();
    }

    /**
     * 目标跟踪
     * @param dataset
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void track(Dataset dataset) {
        //目标追踪中
        //嵌入数据集状态机
        StateMachineUtil.stateChange(new StateChangeDTO() {{
            setObjectParam(new Object[]{dataset});
            setEventMethodName(DataStateMachineConstant.DATA_TRACK_EVENT);
            setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        }});
        Task task = Task.builder().total(NumberConstant.NUMBER_1)
                .datasetId(dataset.getId())
                .type(DataTaskTypeEnum.TARGET_TRACK.getValue())
                .labels("").build();
        baseMapper.insert(task);
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
        datasetService.transferStatus(dataset, DataStateEnum.AUTOMATIC_LABELING_STATE);
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
                            stateIdentify.getStatusForRollback(i, datasetService.getById(i).getCurrentVersionName())
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
            datasetService.transferStatus(dataset, DataStateEnum.MANUAL_ANNOTATION_STATE);
            return;
        }
        datasetService.transferStatus(dataset, DataStateEnum.AUTO_TAG_COMPLETE_STATE);
    }

    /**
     * 完成任务的文件
     *
     * @param taskId       任务id
     * @param fileNum      文件数量
     */
    @Override
    public void finishTaskFile(Long taskId, Integer fileNum) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        getBaseMapper().finishFileNum(taskId, fileNum);
        task = baseMapper.selectById(taskId);
        if (task.getFinished() >= task.getTotal()) {
            finishDataset(task);
        }
    }

    /**
     * 完成文件
     *
     * @param taskId       任务id
     * @param filesCount   完成的文件数量
     * @param dataset      数据集
     * @return             ture or false
     */
    @Override
    public boolean finishFile(Long taskId, Integer filesCount, Dataset dataset) {
        getBaseMapper().finishFile(taskId, filesCount);
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorEnum.TASK_ABSENT);
        }
        if (task.getFinished() >= task.getTotal()) {
            task.setStatus(TaskStatusEnum.FINISHED.getValue());
            getBaseMapper().updateById(task);
            //嵌入数据集状态机
            StateMachineUtil.stateChange(new StateChangeDTO() {{
                setObjectParam(new Object[]{dataset});
                setEventMethodName(DataStateMachineConstant.DATA_DO_FINISH_AUTO_ANNOTATION_EVENT);
                setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
            }});
            return true;
        }
        return false;
    }

    /**
     * 数据集任务完成
     *
     * @param task 任务
     */
    private void finishDataset(Task task) {
        List<Long> datasetIds = JSON.parseArray(task.getDatasets(), Long.class);
        //批量查询当前版本文件数据集列表信息
        List<DatasetVersionFile> datasetVersionFiles = datasetVersionFileService.listDatasetVersionFileByDatasetIds(datasetIds);

        if (!CollectionUtils.isEmpty(datasetVersionFiles)) {
            Map<Long, List<DatasetVersionFile>> datasetVersionMap = datasetVersionFiles.stream().collect(Collectors.groupingBy(DatasetVersionFile::getDatasetId));
            datasetVersionMap.forEach((k, v) -> {
                List<Integer> fileStatus = v.stream()
                        .map(DatasetVersionFile::getAnnotationStatus).collect(Collectors.toList());
                if (!fileStatus.contains(FileStateCodeConstant.MANUAL_ANNOTATION_FILE_STATE)) {
                    //构建数据集状态
                    StateChangeDTO dto = buildStateChangeDTO(fileStatus, k);
                    StateMachineUtil.stateChange(dto);
                }
            });
        }

        //修改任务状态
        task.setStatus(TaskStatusEnum.FINISHED.getValue());
        getBaseMapper().updateById(task);
        taskIds.remove(task.getId());
    }

    /**
     * 构建状态机器参数
     *
     * @param fileStatus      文件状态
     * @param datasetId       数据集ID
     * @return StateChangeDTO 状态变更DTO
     */
    private StateChangeDTO buildStateChangeDTO(List<Integer> fileStatus, Long datasetId) {

        StateChangeDTO dto = new StateChangeDTO();
        dto.setStateMachineType(DataStateMachineConstant.DATA_STATE_MACHINE);
        dto.setObjectParam(new Object[]{datasetId.intValue()});
        if ( fileStatus.contains(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE)) {
            //增强中 -> 自动标注完成
            dto.setEventMethodName(DataStateMachineConstant.DATA_STRENGTHENING_AUTO_COMPLETE_EVENT);
        } else {
            //增强中 -> 标注完成
            dto.setEventMethodName(DataStateMachineConstant.DATA_STRENGTHENING_COMPLETE_EVENT);
        }
        return dto;
    }

    /**
     * 同一个数据集的任务需一起提交
     *
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
            taskIds.add(task.getId());
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
     * 任务失败
     *
     * @param id             任务id 为空则代表是定时任务
     * @param autoAnnotating ignoresMethod
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

    /**
     * 获取一个需要执行的任务
     *
     * @return  任务
     */
    @Override
    public Task getOnePendingTask() {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", MagicNumConstant.ZERO);
        queryWrapper.last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 更新任务状态
     *
     * @param taskId        任务ID
     * @param sourceStatus  原状态
     * @param targetStatus  目的状态
     * @return              更新数量
     */
    @Override
    public int updateTaskStatus(Long taskId, Integer sourceStatus, Integer targetStatus) {
        UpdateWrapper<Task> updateWrapper = new UpdateWrapper<>();
        Task task = new Task();
        task.setStatus(targetStatus);
        updateWrapper.eq("id", taskId).eq("status", sourceStatus);
        return baseMapper.update(task, updateWrapper);
    }

    /**
     * 创建一个任务
     *
     * @param task 任务实体
     */
    @Override
    public void createTask(Task task) {
        baseMapper.insert(task);
    }

    /**
     * 完成任务
     *
     * @param id      任务ID
     * @param fileNum 本次完成文件数
     * @return Boolean 是否完成
     */
    @Override
    public Boolean finishTask(Long id, Integer fileNum) {
        baseMapper.finishFileNum(id, fileNum);
        Task task = baseMapper.selectById(id);
        if (task.getTotal() > task.getFinished() + task.getFailed()) {
            return false;
        }
        updateTaskStatus(id, 2, 3);
        return true;
    }

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return   任务
     */
    @Override
    public Task detail(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 获取执行中的任务
     *
     * @param datasetId 任务ID
     * @param type      任务类型
     * @return          任务列表
     */
    @Override
    public List<Task> getExecutingTask(Long datasetId, Integer type) {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id", datasetId);
        queryWrapper.eq("type", type);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 设置任务总数属性
     *
     * @param taskId 任务ID
     * @param total  总数
     */
    @Override
    public void setTaskTotal(Long taskId,Integer total) {
        UpdateWrapper<Task> updateWrapper = new UpdateWrapper<>();
        Task task = new Task();
        task.setTotal(total);
        updateWrapper.eq("id", taskId);
        baseMapper.update(task, updateWrapper);
    }

    /**
     * 获取一个任务
     *
     * @param taskQueryWrapper 条件搜索任务
     * @return                 任务
     */
    @Override
    public Task selectOne(QueryWrapper<Task> taskQueryWrapper) {
        return baseMapper.selectOne(taskQueryWrapper);
    }

    /**
     * 更新任务
     *
     * @param task 任务详情
     */
    @Override
    public void updateByTaskId(Task task) {
        baseMapper.updateById(task);
    }

}
