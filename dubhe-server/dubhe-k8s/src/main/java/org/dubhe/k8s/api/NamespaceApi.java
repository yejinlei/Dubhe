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

package org.dubhe.k8s.api;

import io.fabric8.kubernetes.api.model.ResourceQuota;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.dubhe.k8s.enums.ValidationTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description k8s中资源为命名空间操作接口
 * @date 2020-07-03
 */
public interface NamespaceApi {
    /**
     * 创建NamespaceLabels，为null则不添加标签
     *
     * @param namespace 命名空间
     * @param labels 标签Map
     * @return BizNamespace Namespace 业务类
     */
    BizNamespace create(@K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME) String namespace, Map<String, String> labels);

    /**
     * 根据namespace查询BizNamespace
     *
     * @param namespace 命名空间
     * @return BizNamespace Namespace 业务类
     */
    BizNamespace get(String namespace);

    /**
     * 查询所有的BizNamespace
     *
     * @return List<BizNamespace> Namespace 业务类集合
     */
    List<BizNamespace> listAll();

    /**
     * 根据label标签查询所有的BizNamespace
     *
     * @param labelKey 标签的键
     * @return List<BizNamespace> Namespace 业务类集合
     */
    List<BizNamespace> list(String labelKey);

    /**
     * 根据label标签集合查询所有的BizNamespace数据
     *
     * @param labels 标签键的集合
     * @return List<BizNamespace> Namespace业务类集合
     */
    List<BizNamespace> list(Set<String> labels);

    /**
     * 删除命名空间
     *
     * @param namespace 命名空间
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult delete(String namespace);

    /**
     * 删除命名空间的标签
     *
     * @param namespace 命名空间
     * @param labelKey 标签的键
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult removeLabel(String namespace, String labelKey);

    /**
     * 删除命名空间下的多个标签
     *
     * @param namespace 命名空间
     * @param labels 标签键的集合
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult removeLabels(String namespace, Set<String> labels);

    /**
     * 将labelKey和labelValue添加到指定的命名空间
     *
     * @param namespace 命名空间
     * @param labelKey 标签的键
     * @param labelValue 标签的值
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult addLabel(String namespace, String labelKey, String labelValue);

    /**
     * 将多个label标签添加到指定的命名空间
     *
     * @param namespace 命名空间
     * @param labels 标签
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult addLabels(String namespace, Map<String, String> labels);

    /**
     * 命名空间的资源限制
     *
     * @param namespace 命名空间
     * @param quota 资源限制参数类
     * @return ResourceQuota 资源限制参数类
     */
    ResourceQuota addResourceQuota(String namespace, ResourceQuota quota);

    /**
     * 获得命名空间下的所有的资源限制
     *
     * @param namespace 命名空间
     * @return List<ResourceQuota> 资源限制参数类
     */
    List<ResourceQuota> listResourceQuotas(String namespace);

    /**
     * 解除对命名空间的资源限制
     *
     * @param namespace 命名空间
     * @param quota 资源限制参数类
     * @return boolean true删除成功 false删除失败
     */
    boolean removeResourceQuota(String namespace, ResourceQuota quota);
}
