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
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description GPU监控数据
 * @date 2021-07-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GpuMetricsDataResultVO {
    /**
     * 显卡编号
     */
    private String accId;

    /**
     * GPU显存总大小
     */
    private String totalMemValues;

    /**
     * GPU使用率监控指标值
     */
    List<MetricsDataResultValueVO> gpuMetricsValues;

    /**
     * GPU显存使用量监控指标值
     */
    List<MetricsDataResultValueVO> gpuMemValues;
}