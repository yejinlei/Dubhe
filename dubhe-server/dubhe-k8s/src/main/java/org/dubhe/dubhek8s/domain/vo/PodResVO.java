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
 * @description Pod 资源占用信息展示 VO
 * @date 2021-7-19
 */
@Data
@Accessors(chain = true)
public class PodResVO {
    /**
     * pod的名称
     */
    private String podName;

    /**
     * pod的内存
     */
    private Integer podMemory;
    /**
     * pod的cpu
     */
    private Integer podCpu;
    /**
     * pod的显卡
     */
    private Integer podCard;
    /***
     * pod的状态
     */
    private String status;
}
