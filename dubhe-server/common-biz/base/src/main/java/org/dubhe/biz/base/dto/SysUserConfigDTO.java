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
import java.util.List;

/**
 * @description 系统用户配置 DTO
 * @date 2021-7-5
 */
@Data
@Accessors(chain = true)
public class SysUserConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Notebook 延迟删除时间配置
     */
    private Integer notebookDelayDeleteTime;

    /**
     * CPU 资源限制配置
     */
    private Integer cpuLimit;

    /**
     * 内存资源限制配置
     */
    private Integer memoryLimit;

    /**
     * GPU 资源限制
     */
    private List<SysUserGpuConfigDTO> gpuResources;

    /**
     * 用户默认镜像
     */
    private Long defaultImageId;
}
