/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.domain.entity.PtTrainJobSpecs;

import java.io.Serializable;

/**
 * @description 创建训练任务的数据包
 * @date 2020-07-15
 */
@Data
@Accessors(chain = true)
public class BaseTrainJobDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private JSONObject runParams;
    private String jobName;
    private String dataSourcePath;
    private PtTrainJobSpecs ptTrainJobSpecs;
    private String outPath;
    private String logPath;
    private String visualizedLogPath;
    private Integer delayCreateTime;
    private Integer delayDeleteTime;

    /**
     * @return 每个节点的GPU数量
     */
    public Integer getGpuNumPerNode(){
        return getPtTrainJobSpecs().getSpecsInfo().getInteger("gpuNum");
    }

    /**
     * @return cpu数量
     */
    public Integer getCpuNum(){
        return getPtTrainJobSpecs().getSpecsInfo().getInteger("cpuNum");
    }

    /**
     * @return memNum
     */
    public Integer getMenNum(){
        return getPtTrainJobSpecs().getSpecsInfo().getInteger("memNum");
    }
    /**
     * "验证数据来源名称"
     */
    private String valDataSourceName;

    /**
     * 验证数据来源路径
     */
    private String valDataSourcePath;
    /**
     * 模型路径
     */
    private String modelLoadPathDir;
}
