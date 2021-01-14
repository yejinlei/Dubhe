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
package org.dubhe.service;

import org.dubhe.domain.dto.RecycleTaskCreateDTO;
import org.dubhe.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.domain.entity.RecycleTask;

import java.util.List;
import java.util.Map;

/**
 * @description 回收垃圾 服务类
 * @date 2020-09-17
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
     * 创建垃圾回收任务
     *
     * @param recycleTaskCreateDTO 垃圾回收任务信息
     */
    void createRecycleTask(RecycleTaskCreateDTO recycleTaskCreateDTO);


    /**
     * 实时删除临时目录无效文件
     *
     * @param sourcePath 删除路径
     */
    void delTempInvalidResources(String sourcePath);


    /**
     * 实时执行回收任务
     *
     * @param taskId 回收任务ID
     */
    void recycleTaskResources(Long taskId);

    /**
     *  获取垃圾回收任务列表
     *
     * @return List<RecycleTask> 垃圾回收任务列表
     */
    List<RecycleTask> getRecycleTaskList();

    /**
     * 回收文件资源
     *
     * @param recycleTask 回收任务
     * @return String 回收任务失败返回的失败信息
     */
    String deleteFileByCMD(RecycleTask recycleTask);

    /**
     * 修改回收任务状态
     *
     * @param recycleTask 回收任务
     * @param recycleIsOk 是否回收成功(true:回收成功，false:未回收成功)
     */
    void updateRecycleStatus(RecycleTask recycleTask, boolean recycleIsOk);

    /**
     * 根据路径回收无效文件
     *
     * @param sourcePath 文件路径
     */
    void deleteInvalidResourcesByCMD(String sourcePath);

}
