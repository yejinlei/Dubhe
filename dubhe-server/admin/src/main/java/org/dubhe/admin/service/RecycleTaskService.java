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
package org.dubhe.admin.service;

import org.dubhe.recycle.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.recycle.domain.entity.Recycle;

import java.util.List;
import java.util.Map;

/**
 * @description 垃圾回收
 * @date 2021-02-23
 */
public interface RecycleTaskService {


    /**
     * 查询回收任务列表
     *
     * @param recycleTaskQueryDTO 查询任务列表条件
     * @return Map<String, Object> 可回收任务列表
     */
    Map<String, Object> getRecycleTasks(RecycleTaskQueryDTO recycleTaskQueryDTO);


    /**
     *  获取垃圾回收任务列表
     *  资源回收单次执行任务数量限制（默认10000）
     * @return List<Recycle> 垃圾回收任务列表
     */
    List<Recycle> getRecycleTaskList();

    /**
     * 执行回收任务(单个)
     * @param recycle 回收实体类
     * @param userId 当前操作用户
     */
    void recycleTask(Recycle recycle,long userId);


    /**
     * 实时删除临时目录无效文件
     *
     * @param sourcePath 删除路径
     */
    void delTempInvalidResources(String sourcePath);

    /**
     * 立即执行回收任务
     *
     * @param taskId 回收任务ID
     */
    void recycleTaskResources(long taskId);

    /**
     * 还原回收任务
     *
     * @param taskId 回收任务ID
     */
    void restore(long taskId);

    /**
     * 根据路径回收无效文件
     *
     * @param sourcePath 文件路径
     */
    void deleteInvalidResourcesByCMD(String sourcePath);

}
