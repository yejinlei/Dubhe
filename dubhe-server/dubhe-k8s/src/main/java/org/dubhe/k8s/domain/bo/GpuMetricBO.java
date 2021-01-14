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
import org.dubhe.k8s.domain.vo.GpuUsageVO;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description Gpu 指标 BO
 * @date 2020-10-13
 */
@Data
public class GpuMetricBO {
    private String status;
    private GpuMetricData data;

    /**
     * 获取Gpu 使用率
     * @return
     */
    public List<GpuUsageVO> getValue(){
        List<GpuUsageVO> gpuUsageVOList = new ArrayList<>();
        if (data == null || CollectionUtils.isEmpty(data.getResult())){
            return gpuUsageVOList;
        }
        for (GpuMetricResult result : data.getResult()){
            gpuUsageVOList.add(new GpuUsageVO(result.getMetric().getAcc_id(),Float.valueOf(result.getValue().get(1).toString())));
        }
        return gpuUsageVOList;
    }
}

@Data
class GpuMetricData{
    private String resultType;
    private List<GpuMetricResult> result;
}

@Data
class GpuMetricResult{
    private GpuMetric metric;
    List<Object> value;
}

@Data
class GpuMetric{
    private String acc_id;
    private String pod;
}

