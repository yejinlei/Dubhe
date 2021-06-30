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
package org.dubhe.serving.domain.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 批量服务创建
 * @date 2020-08-27
 */
@Data
@Accessors(chain = true)
public class BatchServingCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "批量服务名称", required = true)
    @NotNull(message = "批量服务名称不能为空")
    @Size(max = 50, message = "批量服务名称长度超过50")
    @Pattern(regexp = StringConstant.REGEXP_NAME, message = "批量服务名称仅支持字母、数字、汉字、英文横杠和下划线")
    private String name;

    @ApiModelProperty(value = "描述")
    @Size(max = 200, message = "描述长度超过200")
    private String description;

    @ApiModelProperty(value = "模型ID", required =true)
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    @ApiModelProperty(value = "模型版本id")
    private Long modelBranchId;

    @ApiModelProperty(value = "输入数据目录", required = true)
    @NotNull(message = "输入数据目录不能为空")
    private String inputPath;

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
    private JSONObject deployParams;

    @ApiModelProperty(value = "模型来源:0-我的模型，1-预置模型", required = true)
    private Integer modelResource;

    @ApiModelProperty(value = "镜像名称")
    @Size(max = 200, message = "镜像名称长度超过200")
    @NotNull(message = "请选择镜像")
    private String imageName;

    @ApiModelProperty(value = "镜像标签")
    @Size(max = 200, message = "镜像标签长度超过200")
    @NotNull(message = "请选择镜像版本")
    private String imageTag;

    @ApiModelProperty(value = "是否上传推理脚本", required = true)
    @NotNull(message = "请选择是否上传推理脚本")
    private Boolean useScript;

    @ApiModelProperty(value = "算法ID")
    private Long algorithmId;
}
