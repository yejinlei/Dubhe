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
package org.dubhe.dubhek8s.service;

import org.dubhe.biz.base.dto.NamespaceDeleteDTO;
import org.dubhe.dubhek8s.domain.vo.NamespaceVO;

import java.util.Set;

/**
 * @description 查询命名空间状态的 service 层接口
 * @date 2021-7-14
 */
public interface SystemNamespaceService {
    /**
     * 查询命名空间封装的数据
     *
     * @param userId 用户 ID
     * @return NamespaceVO
     */
    NamespaceVO findNamespace(Long userId);

    /**
     *  删除用户namespace
     * @param namespaceDeleteDTO 用户DTO
     */
    void deleteNamespace(NamespaceDeleteDTO namespaceDeleteDTO);
}
