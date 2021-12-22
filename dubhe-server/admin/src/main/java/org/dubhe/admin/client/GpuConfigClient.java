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

import org.dubhe.admin.client.fallback.GpuConfigClientFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.GpuConfigDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description 远程调用k8sGPU资源配额 Client
 * @date 2021-9-7
 */
@FeignClient(value = ApplicationNameConst.SERVER_K8S, contextId = "gpuConfigClient",fallback = GpuConfigClientFallback.class)
public interface GpuConfigClient {
    /**
     * 更新k8sGPU资源配额
     *
     * @param gpuConfigDTO k8sGPU资源配额
     * @return
     */
    @PostMapping(value = "/gpuConfig/update")
    DataResponseBody updateGpuConfig(@RequestBody GpuConfigDTO gpuConfigDTO);

}
