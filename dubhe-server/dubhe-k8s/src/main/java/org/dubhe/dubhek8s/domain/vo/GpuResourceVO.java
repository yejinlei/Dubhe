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
package org.dubhe.dubhek8s.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description
 * @date 2021-09-15
 */
@Data
@Accessors(chain = true)
public class GpuResourceVO {

    /**
     * GPU 资源总量 单位：块
     */
    private Integer hardGpu;


    /**
     * GPU 资源已使用量 单位：块
     */
    private Integer usedGpu;

    /**
     * gpu型号
     */
    private String gpuModel;

    /**
     * k8s GPU资源标签key值(例如：nvidia.com/gpu)
     */
    private String k8sLabelKey;
}
