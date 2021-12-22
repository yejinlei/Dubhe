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

import java.io.Serializable;

/**
 * @description 系统用户GPU配置 DTO
 * @date 2021-09-03
 */
@Data
@Accessors(chain = true)
public class SysUserGpuConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * GPU类型(例如：NVIDIA)
     */
    private String gpuType;

    /**
     * GPU型号(例如：v100)
     */
    private String gpuModel;

    /**
     * k8s GPU资源标签key值(例如：nvidia.com/gpu)
     */
    private String k8sLabelKey;

    /**
     * GPU 资源限制配置不能为空
     */
    private Integer gpuLimit;
}