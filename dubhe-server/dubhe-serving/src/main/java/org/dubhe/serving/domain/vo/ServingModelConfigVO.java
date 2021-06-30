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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 在线服务模型配置返回
 * @date 2020-08-27
 */
@Data
public class ServingModelConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "在线服务模型配置id")
    private Long id;

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

    @ApiModelProperty(value = "模型来源")
    private Integer modelResource;

    @ApiModelProperty(value = "灰度发布分流（%）")
    private Integer releaseRate;

    @ApiModelProperty(value = "节点类型(0为CPU，1为GPU)")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "节点规格")
    private String resourcesPoolSpecs;

    @ApiModelProperty(value = "节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty(value = "资源信息")
    private String resourceInfo;

    @ApiModelProperty(value = "框架类型")
    private Integer frameType;

    @ApiModelProperty(value = "部署参数")
    private JSONObject deployParams;

    @ApiModelProperty(value = "部署ID")
    private String deployId;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "deployment已 Running的pod数")
    private Integer readyReplicas;

    @ApiModelProperty(value = "规格信息")
    private String poolSpecsInfo;

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

    @ApiModelProperty(value = "创建人id")
    private Long createUserId;
}
