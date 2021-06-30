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

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 批量服务详情返回对象
 * @date 2020-08-31
 */
@Data
public class BatchServingDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "批量服务id")
    private Long id;

    @ApiModelProperty(value = "批量服务名称")
    private String name;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "状态对应的详情信息")
    private String statusDetail;

    @ApiModelProperty(value = "进度")
    private String progress;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "模型ID")
    private Long modelId;

    @ApiModelProperty(value = "模型版本ID")
    private Long modelBranchId;

    @ApiModelProperty(value = "模型路径")
    private String modelAddress;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "模型版本")
    private String modelVersion;

    @ApiModelProperty(value = "开始时间")
    private Timestamp startTime;

    @ApiModelProperty(value = "结束时间")
    private Timestamp endTime;

    @ApiModelProperty(value = "输入数据目录")
    private String inputPath;

    @ApiModelProperty(value = "输出数据目录")
    private String outputPath;

    @ApiModelProperty(value = "节点类型(0为CPU，1为GPU)")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "节点规格")
    private String resourcesPoolSpecs;

    @ApiModelProperty(value = "节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty(value = "模型来源")
    private Integer modelResource;

    @ApiModelProperty(value = "框架类型")
    private Integer frameType;

    @ApiModelProperty(value = "部署参数")
    private JSONObject deployParams;

    @ApiModelProperty(value = "镜像名称")
    private String imageName;

    @ApiModelProperty(value = "镜像标签")
    private String imageTag;

    @ApiModelProperty(value = "是否上传推理脚本")
    private Boolean useScript;

    @ApiModelProperty(value = "算法id")
    private Long algorithmId;

    @ApiModelProperty(value = "算法名称")
    private String algorithmName;
}
