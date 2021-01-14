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

import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;

import java.util.List;

/**
 * @description 训练任务操作接口
 * @date 2020-07-03
 */
public interface TrainJobApi {
    /**
     * 创建训练任务 Job
     *
     * @param bo 训练任务 Job BO
     * @return PtJupyterJobVO 训练任务 Job 结果类
     */
    PtJupyterJobVO create(PtJupyterJobBO bo);

    /**
     * 根据命名空间和资源名删除Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return Boolean true删除成功 false删除失败
     */
    Boolean delete(String namespace, String resourceName);

    /**
     * 根据命名空间查询Job
     *
     * @param namespace 命名空间
     * @return List<BizJob> Job业务类集合
     */
    List<BizJob> list(String namespace);

    /**
     * 根据命名空间和资源名查询Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizJob Job业务类
     */
    BizJob get(String namespace, String resourceName);
}
