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
import org.dubhe.base.MagicNumConstant;
import org.dubhe.k8s.utils.UnitConvertUtils;
import org.dubhe.utils.MathUtils;
import org.dubhe.utils.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 封装pod信息
 * @date 2020-06-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PtPodsVO implements Serializable {
    private static final long serialVersionUID = 1L;
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
    /****/
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
    private List<GpuUsageVO> gpuUsagePersent;

    public PtPodsVO(String podName,String cpuRequestAmount,String cpuUsageAmount,String cpuRequestFormat,String cpuUsageFormat,String memoryRequestAmount,String memoryUsageAmount,String memoryRequestFormat,String memoryUsageFormat,String nodeName,String status,String gpuUsed){
        this.podName = podName;
        this.cpuRequestAmount = cpuRequestAmount;
        this.cpuUsageAmount = cpuUsageAmount;
        this.cpuRequestFormat = cpuRequestFormat;
        this.cpuUsageFormat = cpuUsageFormat;
        this.memoryRequestAmount = memoryRequestAmount;
        this.memoryUsageAmount = memoryUsageAmount;
        this.memoryRequestFormat = memoryRequestFormat;
        this.memoryUsageFormat = memoryUsageFormat;
        this.nodeName = nodeName;
        this.status = status;
        this.gpuUsed = gpuUsed;
    }

    public PtPodsVO(String podName,String cpuUsageAmount,String cpuUsageFormat,String memoryUsageAmount,String memoryUsageFormat,String nodeName,String status,String gpuUsed){
        this.podName = podName;
        this.cpuUsageAmount = cpuUsageAmount;
        this.cpuUsageFormat = cpuUsageFormat;
        this.memoryUsageAmount = memoryUsageAmount;
        this.memoryUsageFormat = memoryUsageFormat;
        this.nodeName = nodeName;
        this.status = status;
        this.gpuUsed = gpuUsed;
    }

    public void calculationPercent(){
        if (StringUtils.isNotEmpty(cpuRequestAmount) && StringUtils.isNotEmpty(cpuUsageAmount)){
            cpuUsagePercent =  MathUtils.floatDivision(UnitConvertUtils.cpuFormatToN(cpuUsageAmount,cpuUsageFormat).toString(),UnitConvertUtils.cpuFormatToN(cpuRequestAmount,cpuRequestFormat).toString(), MagicNumConstant.TWO) * MagicNumConstant.ONE_HUNDRED;
        }
        if (StringUtils.isNotEmpty(memoryRequestAmount) && StringUtils.isNotEmpty(memoryUsageAmount)){
            memoryUsagePercent = MathUtils.floatDivision(UnitConvertUtils.cpuFormatToN(memoryUsageAmount,memoryUsageFormat).toString(),UnitConvertUtils.cpuFormatToN(memoryRequestAmount,memoryRequestFormat).toString(), MagicNumConstant.TWO) * MagicNumConstant.ONE_HUNDRED;
        }
    }

    public void addGpuUsage(String accId,Float usage){
        if (CollectionUtils.isEmpty(gpuUsagePersent)){
            gpuUsagePersent = new ArrayList<>();
        }
        gpuUsagePersent.add(new GpuUsageVO(accId,usage));
    }
}
