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
import org.dubhe.k8s.domain.bo.PtJupyterResourceBO;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;

import java.util.List;

/**
 * @description Jupyter Notebook 操作接口
 * @date 2020-07-03
 */
public interface JupyterResourceApi {

    /**
     * 创建Notebook Deployment
     *
     * @param bo 模型管理 Notebook BO
     * @return PtJupyterDeployVO  Notebook 结果类
     */
    PtJupyterDeployVO create(PtJupyterResourceBO bo);

    /**
     * 创建Notebook Deployment并使用pvc存储
     *
     * @param bo 模型管理 Notebook BO
     * @return PtJupyterDeployVO Notebook 结果类
     */
    PtJupyterDeployVO createWithPvc(PtJupyterResourceBO bo);

    /**
     * 删除Notebook Deployment
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult delete(String namespace, String resourceName);

    /**
     * 查询命名空间下所有Notebook
     *
     * @param namespace 命名空间
     * @return List<PtJupyterDeployVO> Notebook 结果类集合
     */
    List<PtJupyterDeployVO> list(String namespace);

    /**
     * 查询Notebook
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtJupyterDeployVO Notebook 结果类
     */
    PtJupyterDeployVO get(String namespace, String resourceName);
}
