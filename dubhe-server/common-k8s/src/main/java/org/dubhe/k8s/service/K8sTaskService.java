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

package org.dubhe.k8s.service;

import org.dubhe.k8s.domain.bo.K8sTaskBO;
import org.dubhe.k8s.domain.entity.K8sTask;

import java.util.List;

/**
 * @description k8s任务服务
 * @date 2020-08-31
 */
public interface K8sTaskService {
    /**
     * 创建或者更新任务
     *
     * @param k8sTask 对象
     * @return int 插入数量
     */
    int createOrUpdateTask(K8sTask k8sTask);

    /**
     * 修改任务
     * @param k8sTask k8s任务
     * @return int 更新数量
     */
    int update(K8sTask k8sTask);

    /**
     * 根据namesapce 和 resourceName 查询
     * @param k8sTask
     * @return
     */
    List<K8sTask> selectByNamespaceAndResourceName(K8sTask k8sTask);

    /**
     * 根据条件查询未执行的任务
     * @param k8sTaskBO k8s任务参数
     * @return List<k8sTask> k8s任务类集合
     */
    List<K8sTask> selectUnexecutedTask(K8sTaskBO k8sTaskBO);

    /**
     * 查询未执行的任务
     * @return List<k8sTask> k8s任务类集合
     */
    List<K8sTask> selectUnexecutedTask();

    /**
     * 添加redis延时队列
     * @param k8sTask
     * @return
     */
    boolean addRedisDelayTask(K8sTask k8sTask);

    /**
     * 加载任务到延时队列
     */
    void loadTaskToRedis();

    /**
     * 根据namespace 和 resourceName 删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return boolean
     */
    boolean deleteByNamespaceAndResourceName(String namespace,String resourceName);
}
