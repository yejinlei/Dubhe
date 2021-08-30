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
package org.dubhe.serving.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.k8s.domain.vo.GpuValueVO;

import java.io.Serializable;
import java.util.List;

/**
 * @description
 * @date 2020-10-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServingPodMetricsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * pod名称
     **/
    private String podName;
    /**
     * cpu 申请量
     */
    private String cpuRequestAmount;
    /**
     * cpu用量
     **/
    private String cpuUsageAmount;
    /**
     * cpu 申请量 单位 1核=1000m,1m=1000000n
     */
    private String cpuRequestFormat;
    /**
     * cpu用量 单位 1核=1000m,1m=1000000n
     **/
    private String cpuUsageFormat;
    /**
     * cpu 使用百分比
     */
    private Float cpuUsagePercent;
    /**
     * 内存申请量
     */
    private String memoryRequestAmount;
    /**
     * 内存用量
     **/
    private String memoryUsageAmount;
    /**
     * 内存申请量单位
     **/
    private String memoryRequestFormat;
    /**
     * 内存用量单位
     **/
    private String memoryUsageFormat;
    /**
     * 内存使用百分比
     */
    private Float memoryUsagePercent;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 设置status状态值
     **/
    private String status;
    /**
     * 设置gpu的使用情况
     **/
    private String gpuUsed;
    /**
     * gpu使用百分比
     */
    private List<GpuValueVO> gpuUsagePersent;
    /**
     * grafana地址
     */
    private String grafanaUrl;
}
