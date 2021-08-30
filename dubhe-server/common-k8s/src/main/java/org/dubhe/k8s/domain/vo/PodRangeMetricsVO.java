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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description Pod历史监控指标 VO
 * @date 2021-02-02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PodRangeMetricsVO {
    /**
     * pod名称
     **/
    private String podName;
    /**
     * cpu 监控指标 value为使用百分比
     */
    List<MetricsDataResultValueVO> cpuMetrics;
    /**
     * gpu 监控指标
     */
    List<GpuMetricsDataResultVO> gpuMetrics;
    /**
     * 内存 监控指标 value为占用内存 单位 Ki
     */
    List<MetricsDataResultValueVO> memoryMetrics;

    public PodRangeMetricsVO(String podName){
        this.podName = podName;
    }
}
