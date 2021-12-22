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
package org.dubhe.admin.client;

import org.dubhe.admin.client.fallback.SystemNamespaceClientFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.NamespaceDeleteDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @description 命名空间状态管理feign远程调用
 * @date 2021-11-26
 */
@FeignClient(value = ApplicationNameConst.SERVER_K8S, contextId = "systemNamespaceClient", fallback = SystemNamespaceClientFallback.class)
public interface SystemNamespaceClient {

    /**
     * 删除用户namespace
     * @param namespaceDeleteDTO 用户id
     * @return DataResponseBody
     */
    @DeleteMapping(value="/namespace")
    DataResponseBody deleteNamespace(@RequestBody NamespaceDeleteDTO namespaceDeleteDTO, @RequestHeader("Authorization") String accessToken);
}