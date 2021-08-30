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

package org.dubhe.dubhek8s.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description k8s节点资源隔离DTO
 * @date 2021-07-21
 */
@Data
public class ResourceQuotaDTO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @NotNull(message = "CPU 资源限制配置不能为空")
    private Integer cpuLimit;

    @NotNull(message = "内存资源限制配置不能为空")
    private Integer memoryLimit;

    @NotNull(message = "GPU 资源限制配置不能为空")
    private Integer gpuLimit;
}
