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
package org.dubhe.biz.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 用户GPU配置DTO
 * @date 2021-7-1
 */
@Data
@Accessors(chain = true)
public class UserGpuConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "GPU类型")
    private String gpuType;

    @NotBlank(message = "GPU型号")
    private String gpuModel;

    @NotBlank(message = "k8s GPU资源标签key值")
    private String k8sLabelKey;

    @NotNull(message = "GPU 资源限制配置不能为空")
    private Integer gpuLimit;
}
