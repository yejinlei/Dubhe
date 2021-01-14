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

import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtModelOptimizationJobBO;
import org.dubhe.k8s.domain.resource.BizJob;

import java.util.List;

/**
 * @description 模型压缩操作接口
 * @date 2020-07-03
 */
public interface ModelOptJobApi {
    /**
     * 创建模型优化 Job
     *
     * @param bo 模型优化 Job BO
     * @return BizJob Job业务类
     */
    BizJob create(PtModelOptimizationJobBO bo);

    /**
     * 通过命名空间和资源名称查找Job资源
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizJob Job 业务类
     */
    BizJob getWithResourceName(String namespace,String resourceName);

    /**
     * 通过命名空间查找Job资源
     *
     * @param namespace 命名空间
     * @return List<BizJob> Job 业务类集合
     */
    List<BizJob> getWithNamespace(String namespace);

    /**
     * 查询所有Job资源
     *
     * @return List<BizJob> Job 业务类集合
     */
    List<BizJob> listAll();

    /**
     * 通过命名空间和资源名删除Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult  基础结果类
     */
    PtBaseResult deleteByResourceName(String namespace, String resourceName);
}
