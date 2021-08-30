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
package org.dubhe.train.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

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
    private String taskIdentify;
    private String dataSourcePath;
    private String trainModelPath;
    private String trainOutPath;
    private String visualizedLogPath;
    private Integer delayCreateTime;
    private Integer delayDeleteTime;
    private String modelPath;
    private List<String> teacherModelPathList;
    private List<String> studentModelPathList;
    private Long modelId;
    private Long modelBranchId;
    private Integer modelResource;
    private String teacherModelIds;
    private String studentModelIds;

    /**
     * 规格名称
     */
    private String trainJobSpecsName;

    /**
     * 类型(0为CPU，1为GPU)
     */
    private Integer resourcesPoolType;

    /**
     * CPU数量,单位：m(毫核)
     */
    private Integer cpuNum;

    /**
     * GPU数量，单位：核
     */
    private Integer gpuNum;

    /**
     * 内存大小，单位：Mi
     */
    private Integer memNum;

    /**
     * 工作空间的存储配额，单位：Mi
     */
    private Integer workspaceRequest;

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

    /**
     * pip包路径
     */
    private String pipSitePackagePath;
}
