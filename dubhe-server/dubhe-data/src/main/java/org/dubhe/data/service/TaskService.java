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

import org.dubhe.data.domain.bo.EnhanceTaskSplitBO;
import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AutoAnnotationCreateDTO;

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
     * @param taskId 任务id
     * @param filesCount 完成的文件数量
     */
    void finishFile(Long taskId, Integer filesCount);

    /**
     * 任务失败
     *
     * @param id             任务id 为空则代表是定时任务
     * @param autoAnnotating 自动标注
     * @param enhancing      数据增强
     */
    void doRemoveTask(Long id, ConcurrentHashMap<String, TaskSplitBO> autoAnnotating, ConcurrentHashMap<String, EnhanceTaskSplitBO> enhancing);

    /**
     * 完成任务的文件
     *
     * @param taskId  任务id
     * @param fileNum 文件数量
     */
    void finishTaskFile(Long taskId, Integer fileNum);

}
