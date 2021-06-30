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

package org.dubhe.k8s.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.k8s.domain.bo.K8sTaskBO;
import org.dubhe.k8s.domain.entity.K8sTask;

import java.util.List;

/**
 * @description k8s任务mapper接口
 * @date 2020-8-31
 */
public interface K8sTaskMapper extends BaseMapper<K8sTask> {

    /**
     * 保存任务
     *
     * @param k8sTask k8s任务类
     */
    int insertOrUpdate(K8sTask k8sTask);

    /**
     * 查询待执行任务
     *
     * @param k8sTaskBO k8s任务查询类
     * @return List<k8sTask> k8s任务集合类
     */
    List<K8sTask> selectUnexecutedTask(K8sTaskBO k8sTaskBO);

    /**
     * 根据namespace 和 resourceName 删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return boolean
     */
    @Update("update k8s_task set deleted = #{deleteFlag} where namespace = #{namespace} and resource_name = #{resourceName}")
    int deleteByNamespaceAndResourceName(@Param("namespace") String namespace,@Param("resourceName") String resourceName, @Param("deleteFlag") boolean deleteFlag);
}
