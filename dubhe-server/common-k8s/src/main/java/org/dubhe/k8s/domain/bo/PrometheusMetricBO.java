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

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.functional.StringFormat;
import org.dubhe.k8s.domain.vo.GpuTotalMemResultVO;
import org.dubhe.k8s.domain.vo.MetricsDataResultValueVO;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @return Map<String, Float> gpu使用率列表
     */
    public Map<String, Float> getGpuUsage() {
        Map<String, Float> gpuUsageMap = new HashMap<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return gpuUsageMap;
        }
        for (MetricResult result : data.getResult()) {
            gpuUsageMap.put(result.getMetric().getAcc_id(), Float.valueOf(result.getValue().get(1).toString()));
        }
        return gpuUsageMap;
    }

    /**
     * 获取GPU显存使用量
     * @return Map<String, String> gpu使用量列表
     */
    public Map<String, String> getGpuMemValue() {
        Map<String, String> gpuMemValueMap = new HashMap<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return gpuMemValueMap;
        }
        StringFormat memMetricsFormat = (value) -> {
            return NumberUtil.isNumber(String.valueOf(value)) ? String.valueOf(Long.valueOf(String.valueOf(value)) / MagicNumConstant.BINARY_TEN_EXP) : String.valueOf(MagicNumConstant.ZERO);
        };
        for (MetricResult result : data.getResult()) {
            gpuMemValueMap.put(result.getMetric().getAcc_id(), memMetricsFormat.format(result.getValue().get(1).toString()));
        }
        return gpuMemValueMap;
    }

    /**
     * 获取GPU显存总大小
     * @return List<GpuTotalMemResultVO> GPU显存总大小列表
     */
    public List<GpuTotalMemResultVO> getGpuTotalMemValue() {
        List<GpuTotalMemResultVO> gpuTotalMemValueVOList = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return gpuTotalMemValueVOList;
        }
        StringFormat memMetricsFormat = (value) -> {
            return NumberUtil.isNumber(String.valueOf(value)) ? String.valueOf(Long.valueOf(String.valueOf(value)) / MagicNumConstant.BINARY_TEN_EXP) : String.valueOf(MagicNumConstant.ZERO);
        };
        for (MetricResult result : data.getResult()) {
            gpuTotalMemValueVOList.add(new GpuTotalMemResultVO(result.getMetric().getAcc_id(), memMetricsFormat.format(result.getValue().get(1).toString())));
        }
        return gpuTotalMemValueVOList;
    }


    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public List<MetricsDataResultValueVO> getValues(StringFormat stringFormat) {
        List<MetricsDataResultValueVO> list = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return list;
        }
        for (MetricResult result : data.getResult()) {
            result.getValues().forEach(obj -> {
                list.add(new MetricsDataResultValueVO(obj.get(0).toString(), stringFormat.format(obj.get(1).toString())));
            });
        }
        return list;
    }

    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public List<MetricsDataResultValueVO> getValues(MetricResult metricResult) {
        List<MetricsDataResultValueVO> list = new ArrayList<>();
        if (metricResult == null || CollectionUtils.isEmpty(metricResult.getValues())) {
            return list;
        }
        metricResult.getValues().forEach(obj -> {
            list.add(new MetricsDataResultValueVO(obj.get(0).toString(), obj.get(1).toString()));
        });
        return list;
    }

    /**
     * 获取 GPU使用率result列表
     * @return List<MetricsDataResultVO> 监控指标列表
     */
    public Map<String, List<MetricsDataResultValueVO>> getGpuMetricsResults() {
        Map<String, List<MetricsDataResultValueVO>> map = new HashMap<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return map;
        }
        for (MetricResult result : data.getResult()) {
            map.put(result.getMetric().getAcc_id(), getValues(result));
        }
        return map;
    }

    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public List<MetricsDataResultValueVO> getFormatValues(MetricResult metricResult, StringFormat stringFormat) {
        List<MetricsDataResultValueVO> list = new ArrayList<>();
        if (metricResult == null || CollectionUtils.isEmpty(metricResult.getValues())) {
            return list;
        }
        metricResult.getValues().forEach(obj -> {
            list.add(new MetricsDataResultValueVO(obj.get(0).toString(), stringFormat.format(obj.get(1).toString())));
        });
        return list;
    }

    /**
     * 获取 GPU显存使用量result列表
     * @return Map<String, List < MetricsDataResultValueVO>> 监控指标列表
     */
    public Map<String, List<MetricsDataResultValueVO>> getGpuMemResults() {
        Map<String, List<MetricsDataResultValueVO>> map = new HashMap<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return map;
        }
        StringFormat memMetricsFormat = (value) -> {
            return NumberUtil.isNumber(String.valueOf(value)) ? String.valueOf(Long.valueOf(String.valueOf(value)) / MagicNumConstant.BINARY_TEN_EXP) : String.valueOf(MagicNumConstant.ZERO);
        };
        for (MetricResult result : data.getResult()) {
            map.put(result.getMetric().getAcc_id(), getFormatValues(result, memMetricsFormat));
        }
        return map;
    }

    /**
     * 获取value 列表
     * @return List<MetricsDataResultValueVO> 监控指标列表
     */
    public String getGpuTotalValues(MetricResult metricResult, StringFormat stringFormat) {
        List<String> strings = new ArrayList<>();
        if (metricResult == null || CollectionUtils.isEmpty(metricResult.getValues())) {
            return "";
        }
        metricResult.getValues().forEach(obj -> {
            strings.add(stringFormat.format(obj.get(1).toString()));
        });
        return strings.get(0);
    }

    /**
     * 获取 GPU显存总量result列表
     * @return List<MetricsDataResultVO> 监控指标列表
     */
    public List<GpuTotalMemResultVO> getGpuTotalMemResults() {
        List<GpuTotalMemResultVO> list = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())) {
            return list;
        }
        StringFormat memMetricsFormat = (value) -> {
            return NumberUtil.isNumber(String.valueOf(value)) ? String.valueOf(Long.valueOf(String.valueOf(value)) / MagicNumConstant.BINARY_TEN_EXP) : String.valueOf(MagicNumConstant.ZERO);
        };
        for (MetricResult result : data.getResult()) {
            list.add(new GpuTotalMemResultVO(result.getMetric().getAcc_id(), getGpuTotalValues(result, memMetricsFormat)));
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

