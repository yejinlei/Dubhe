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
import org.dubhe.k8s.domain.bo.PtLimitRangeBO;
import org.dubhe.k8s.domain.resource.BizLimitRange;

import java.util.List;

/**
 * @description 限制命名空间下Pod的资源配额
 * @date 2020-07-03
 */
public interface LimitRangeApi {
    /**
     * 创建LimitRange
     *
     * @param bo LimitRange BO
     * @return BizLimitRange LimitRange 业务类
     */
    BizLimitRange create(PtLimitRangeBO bo);

    /**
     * 查询命名空间下所有LimitRange
     *
     * @param namespace 命名空间
     * @return List<BizLimitRange> LimitRange 业务类集合
     */
    List<BizLimitRange> list(String namespace);

    /**
     * 删除LimitRange
     *
     * @param namespace 命名空间
     * @param name LimitRange 名称
     * @return PtBaseResult 基本结果类
     */
    PtBaseResult delete(String namespace, String name);

}
