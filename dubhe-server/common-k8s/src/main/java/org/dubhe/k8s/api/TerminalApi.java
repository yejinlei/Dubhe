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
import org.dubhe.k8s.domain.bo.TerminalBO;
import org.dubhe.k8s.domain.vo.TerminalResourceVO;

/**
 * @description 专业版终端接口
 * @date 2021-06-29
 */
public interface TerminalApi {
    /**
     * 创建
     *
     * @param bo
     * @return BizDeployment
     */
    TerminalResourceVO create(TerminalBO bo);

    /**
     * 删除
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    PtBaseResult delete(String namespace, String resourceName);

    /**
     * 查询
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return
     */
    TerminalResourceVO get(String namespace, String resourceName);
}
