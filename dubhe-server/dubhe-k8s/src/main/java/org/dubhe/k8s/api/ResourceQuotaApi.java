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
import org.dubhe.k8s.domain.bo.PtResourceQuotaBO;
import org.dubhe.k8s.domain.resource.BizResourceQuota;

import java.util.List;

/**
 * @description 限制命名空间整体的资源配额
 * @date 2020-07-03
 */
public interface ResourceQuotaApi {
    /**
     * 创建 ResourceQuota
     *
     * @param bo ResourceQuota BO
     * @return BizResourceQuota ResourceQuota 业务类
     */
    BizResourceQuota create(PtResourceQuotaBO bo);

    /**
     * 根据命名空间查询ResourceQuota集合
     *
     * @param namespace 命名空间
     * @return List<BizResourceQuota> ResourceQuota 业务类集合
     */
    List<BizResourceQuota> list(String namespace);

    /**
     * 删除ResourceQuota
     *
     * @param namespace 命名空间
     * @param name ResourceQuota 名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult delete(String namespace, String name);
}
