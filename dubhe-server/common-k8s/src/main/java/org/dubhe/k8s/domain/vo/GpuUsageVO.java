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

/**
 * @description Gpu usage percent
 * @date 2020-10-13
 */
@Data
@AllArgsConstructor
public class GpuUsageVO {
    /**
     * 显卡id
     */
    private String accId;
    /**
     * 使用率 百分比
     */
    Float usage;
}
