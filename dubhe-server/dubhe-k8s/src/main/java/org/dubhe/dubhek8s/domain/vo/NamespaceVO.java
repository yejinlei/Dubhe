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
import java.util.List;

/**
 * @description
 * @date 2021-7-14
 */
@Data
@Accessors(chain = true)
public class NamespaceVO {

    /**
     * CPU 资源总量 单位：核
     */
    private Integer hardCpu;
    /**
     * 内存资源总量 单位：Gi
     */
    private Integer hardMemory;
    /**
     * GPU 资源总量 单位：块
     */
    private Integer hardGpu;
    /**
     * CPU 资源已使用量 单位：核
     */
    private Integer usedCpu;
    /**
     * 内存资源已使用量 单位：Gi
     */
    private Integer usedMemory;
    /**
     * GPU 资源已使用量 单位：块
     */
    private Integer usedGpu;

    /**
     * 任务 资源占用信息
     */
    private List<TaskResVO> tasks;



}
