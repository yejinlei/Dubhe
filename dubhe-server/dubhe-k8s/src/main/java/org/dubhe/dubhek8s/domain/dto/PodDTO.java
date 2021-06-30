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

/**
 * @description pod的实体类
 * @date 2020-06-03
 */
@Data
public class PodDTO {

    /**
     * pod的name
     */
    private String podName;
    /**
     * pod的内存
     */
    private String podMemory;
    /**
     * pod的cpu
     */
    private String podCpu;
    /**
     * pod的显卡  
     */
    private String podCard;
    /***
     * pod的状态
     */
    private String status;
    /**
     * node的name
     */
    private String nodeName;
    /**
     * pod的创建时间
     */
    private String podCreateTime;
}
