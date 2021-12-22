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
package org.dubhe.k8s.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: k8s事件VO
 * @date: 2021/11/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class K8sEventVO {
    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 事件类型
     */
    private String type;

    /**
     * 事件信息
     */
    private String message;

    /**
     * 事件发生事件
     */
    private String startTime;
}
