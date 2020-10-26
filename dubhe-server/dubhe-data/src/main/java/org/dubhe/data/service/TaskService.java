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

package org.dubhe.data.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dubhe.data.constant.DatasetStatusEnum;
import org.dubhe.data.dao.TaskMapper;
import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;
import org.dubhe.data.domain.entity.Dataset;
import org.dubhe.data.domain.entity.Task;
import org.dubhe.data.machine.enums.DataStateEnum;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 标注任务信息服务
 * @date 2020-04-10
 */
public interface TaskService {

    /**
     * 自动标注任务提交
     *
     * @param autoAnnotationCreateDTO 自动标注任务
     * @return 自动标注生成的父任务id列表
     */
    List<Long> auto(AutoAnnotationCreateDTO autoAnnotationCreateDTO);

    /**
     * 任务失败处理(定时任务中使用)
     */
    void fail();

    /**
     * 完成文件
     *
     * @param taskId       任务id
     * @param filesCount   完成的文件数量
     */
    void finishTaskFile(Long taskId, Integer filesCount);

    /**
     * 完成文件
     *
     * @param taskId       任务id
     * @param filesCount   完成的文件数量
     * @param dataset      数据集
     * @return             执行是否成功
     */
    boolean finishFile(Long taskId, Integer filesCount, Dataset dataset);

    /**
     * 完成任务
     *
     * @param id            任务ID
     * @param fileNum       本次完成文件数
     * @return Boolean      是否完成
     */
    Boolean finishTask(Long id, Integer fileNum);

    /**
     * 任务失败
     *
     * @param id             任务id 为空则代表是定时任务
     * @param autoAnnotating 自动标注
     * @param enhancing      数据增强
     */
    void doRemoveTask(Long id, ConcurrentHashMap<String, TaskSplitBO> autoAnnotating, ConcurrentHashMap<String, EnhanceTaskSplitBO> enhancing);

    /**
     * 获取一个待处理任务
     *
     * @return  任务
     */
    Task getOnePendingTask();

    /**
     * 修改任务状态(给任务加锁，解决并发问题)
     *
     * @param taskId        任务ID
     * @param sourceStatus  原状态
     * @param targetStatus  目标状态
     * @return              更新的数量
     */
    int updateTaskStatus(Long taskId, Integer sourceStatus, Integer targetStatus);

    /**
     * 创建一个任务
     *
     * @param task 任务实体
     */
    void createTask(Task task);

    /**
     * 数据集跟踪
     *
     * @param dataset 数据集详情
     */
    void track(Dataset dataset);

    /**
     * 获取数据集详情
     *
     * @param id 数据集ID
     * @return   任务
     */
    Task detail(Long id);

    /**
     * 获取正在执行的任务
     *
     * @param datasetId 数据集ID
     * @param type      任务类型
     * @return          任务列表
     */
    List<Task> getExecutingTask(Long datasetId, Integer type);


    /**
     * 设置任务总数
     *
     * @param taskId 任务ID
     * @param total  总数
     */
    void setTaskTotal(Long taskId,Integer total);

    /**
     * 查询任务
     *
     * @param taskQueryWrapper 任务查询条件
     * @return                 任务
     */
    Task selectOne(QueryWrapper<Task> taskQueryWrapper);

    /**
     * 更新任务数据
     *
     * @param task 任务详情
     */
    void updateByTaskId(Task task);

}
