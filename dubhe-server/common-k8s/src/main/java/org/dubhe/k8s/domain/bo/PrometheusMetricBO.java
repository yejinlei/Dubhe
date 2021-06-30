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

package org.dubhe.k8s.domain.bo;

import lombok.Data;
import org.dubhe.biz.base.functional.StringFormat;
import org.dubhe.k8s.domain.vo.GpuUsageVO;
import org.dubhe.k8s.domain.vo.MetricsDataResultVO;
import org.dubhe.k8s.domain.vo.MetricsDataResultValueVO;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description Gpu 指标 BO
 * @date 2020-10-13
 */
@Data
public class PrometheusMetricBO {
    private String status;
    private MetricData data;

    /**
     * 获取Gpu 使用率
     * @return List<GpuUsageVO> gpu使用列表
     */
    public List<GpuUsageVO> getGpuUsage(){
        List<GpuUsageVO> gpuUsageVOList = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())){
            return gpuUsageVOList;
        }
        for (MetricResult result : data.getResult()){
            gpuUsageVOList.add(new GpuUsageVO(result.getMetric().getAcc_id(),Float.valueOf(result.getValue().get(1).toString())));
        }
        return gpuUsageVOList;
    }

    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public List<MetricsDataResultValueVO> getValues(StringFormat stringFormat){
        List<MetricsDataResultValueVO> list = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())){
            return list;
        }
        for (MetricResult result : data.getResult()){
            result.getValues().forEach(obj->{
                list.add(new MetricsDataResultValueVO(obj.get(0).toString(),stringFormat.format(obj.get(1).toString())));
            });
        }
        return list;
    }

    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public List<MetricsDataResultValueVO> getValues(MetricResult metricResult){
        List<MetricsDataResultValueVO> list = new ArrayList<>();
        if (metricResult == null || CollectionUtils.isEmpty(metricResult.getValues())){
            return list;
        }
        metricResult.getValues().forEach(obj->{
            list.add(new MetricsDataResultValueVO(obj.get(0).toString(),obj.get(1).toString()));
        });
        return list;
    }

    /**
     * 获取 result列表
     * @return List<MetricsDataResultVO> 监控指标列表
     */
    public List<MetricsDataResultVO> getResults(){
        List<MetricsDataResultVO> list = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())){
            return list;
        }
        for (MetricResult result : data.getResult()){
            list.add(new MetricsDataResultVO(result.getMetric().getAcc_id(),getValues(result)));
        }
        return list;
    }
}

@Data
class MetricData {
    private String resultType;
    private List<MetricResult> result;
}

@Data
class MetricResult {
    private Metric metric;
    List<Object> value;
    List<List<Object>> values;
}

@Data
class Metric {
    private String acc_id;
    private String pod;
}

