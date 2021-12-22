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

import org.dubhe.admin.client.fallback.SystemNodeClientFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description 远程查询用户k8s资源是否可用
 * @date 2021-9-7
 */
@FeignClient(value = ApplicationNameConst.SERVER_K8S, contextId = "systemNodeClient", fallback = SystemNodeClientFallback.class)
public interface SystemNodeClient {
    /**
     * 查询用户k8s可用资源
     *
     * @param queryUserK8sResources 用户k8s可用资源查询条件
     * @return List<QueryUserResourceSpecsVO>  用户k8s可用资源列表
     */
    @PostMapping("/node/queryUserResource")
    DataResponseBody<List<QueryUserResourceSpecsVO>> queryUserK8sResource(@RequestBody List<QueryUserK8sResourceDTO> queryUserK8sResources);

}
