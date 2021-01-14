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

import org.dubhe.k8s.domain.entity.K8sResource;
import org.dubhe.k8s.domain.resource.BizPod;

import java.util.List;

/**
 * @description k8s资源服务接口
 * @date 2020-07-13
 */
public interface K8sResourceService {
    /**
     * 根据pod插入
     *
     * @param pod Pod对象
     * @return int 插入数量
     */
    int create(BizPod pod);

    /**
     * 新增
     *
     * @param k8sResource 对象
     * @return int 插入数量
     */
    int create(K8sResource k8sResource);

    /**
     * 根据资源名查询
     *
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return List<K8sResource> K8sResource资源集合
     */
    List<K8sResource> selectByResourceName(String kind, String namespace, String resourceName);

    /**
     * 根据对象名查询
     *
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @return List<K8sResource> K8sResource资源集合
     */
    List<K8sResource> selectByName(String kind, String namespace, String name);

    /**
     * 根据resourceName删除
     * @param kind  资源类型
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return int 删除数量
     */
    int deleteByResourceName(String kind,String namespace,String resourceName);

    /**
     * 根据名称删除
     * @param kind
     * @param namespace
     * @param name 比如kind 是 pod那name就对应 podName
     * @return int 删除数量
     */
    int deleteByName(String kind,String namespace,String name);
}
