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
import org.dubhe.k8s.domain.bo.PtModelOptimizationDeploymentBO;
import org.dubhe.k8s.domain.resource.BizDeployment;

import java.util.List;

/**
 * @description k8s中资源为Deployment的操作接口
 * @date 2020-07-03
 */
public interface DubheDeploymentApi {
    /**
     * 创建模型压缩Deployment
     *
     * @param bo 模型压缩 Deployment BO
     * @return BizDeployment Deployment 业务类
     */
    BizDeployment create(PtModelOptimizationDeploymentBO bo);

    /**
     * 通过命名空间和资源名称查找Deployment资源
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizDeployment Deployment 业务类
     */
    BizDeployment getWithResourceName(String namespace,String resourceName);

    /**
     * 通过命名空间查找Deployment资源集合
     *
     * @param namespace 命名空间
     * @return List<BizDeployment> Deployment 业务类集合
     */
    List<BizDeployment> getWithNamespace(String namespace);

    /**
     * 查询集群所有Deployment资源
     *
     * @return List<BizDeployment> Deployment 业务类集合
     */
    List<BizDeployment> listAll();

    /**
     * 通过资源名进行删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult deleteByResourceName(String namespace, String resourceName);
}
