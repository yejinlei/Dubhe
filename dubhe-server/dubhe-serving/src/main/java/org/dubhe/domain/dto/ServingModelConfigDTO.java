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
package org.dubhe.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.constant.NumberConstant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 模型配置修改参数
 * @date 2020-08-24
 */
@Data
public class ServingModelConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模型配置ID", required =true)
    private Long id;

    @ApiModelProperty(value = "模型ID", required =true)
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    @ApiModelProperty(value = "模型路径")
    @NotNull(message = "模型路径不能为空")
    private String modelAddress;

    @ApiModelProperty(value = "模型来源")
    @NotNull(message = "模型来源不能为空")
    private Integer modelResource;

    @ApiModelProperty(value = "灰度发布分流（%）", required = true)
    @NotNull(message = "灰度发布分流不能为空")
    @Min(value = NumberConstant.NUMBER_0)
    @Max(value = NumberConstant.NUMBER_100)
    private Integer releaseRate;

    @ApiModelProperty(value = "节点类型(0为CPU，1为GPU)", required = true)
    @Min(value = NumberConstant.NUMBER_0, message = "节点类型错误")
    @Max(value = NumberConstant.NUMBER_1, message = "节点类型错误")
    @NotNull(message = "节点类型不能为空")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "节点规格", required = true)
    @NotNull(message = "节点规格不能为空")
    private String resourcesPoolSpecs;

    @ApiModelProperty(value = "规格信息", required = true)
    @NotNull(message = "规格信息不能为空")
    private String poolSpecsInfo;

    @ApiModelProperty(value = "节点个数", required = true)
    @NotNull(message = "节点个数不能为空")
    private Integer resourcesPoolNode;

    @ApiModelProperty(value = "部署参数")
    private String deployParams;
}
