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
import org.dubhe.biz.base.vo.GpuAllotVO;

import java.util.List;

/**
 * @description
 * @date 2021-11-12
 */
@Data
public class K8sAllResourceVO {


    /**
     * 已分配给用户的GPU核数
     */
    private Integer gpuAllotTotal;

    /**
     * 已占用的GPU核数
     */
    private Integer gpuUsedTotal;

    /**
     * 集群GPU的总数
     */
    private Integer gpuTotal;

    /**
     * 用户gpu资源配额
     */
    private List<GpuAllotVO> gpuResourceList;

    /**
     * 已分配给用户的CPU核数
     */
    private Integer cpuAllotTotal;

    /**
     * 已占用的CPU核数
     */

    private Integer cpuUsedTotal;

    /**
     * 集群CPU的总数
     */
    private Integer cpuTotal;

    /**
     * 已分配给用户的内存
     */
    private Integer memoryAllotTotal;

    /**
     * 已占用的内存
     */
    private Integer memoryUsedTotal;

    /**
     * 集群CPU的内存
     */
    private Integer memoryTotal;

    /**
     * 集群节点总数
     */
    private Integer nodeTotal;

    /**
     * gpu平均使用率
     */
    private float gpuAverageUsage;

}
    
