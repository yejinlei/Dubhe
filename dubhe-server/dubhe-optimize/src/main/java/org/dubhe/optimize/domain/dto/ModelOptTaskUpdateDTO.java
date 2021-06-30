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

package org.dubhe.optimize.domain.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.StringConstant;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 修改模型优化任务
 * @date 2020-05-22
 */
@Data
@Accessors(chain = true)
public class ModelOptTaskUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotEmpty(message = "任务名称不能为空")
    @Size(max = 32, message = "任务名称长度不能超过32")
    @Pattern(regexp = StringConstant.REGEXP_ALGORITHM, message = "任务名称仅支持字母、数字、汉字、英文横杠和下划线")
    private String name;

    @ApiModelProperty(value = "任务描述")
    @Size(max = 500, message = "任务描述长度不能超过500")
    private String description;

    @ApiModelProperty(value = "是否内置", required = true)
    @NotNull(message = "请选择是否内置")
    private Boolean isBuiltIn;

    @ApiModelProperty(value = "数据集id")
    private Long datasetId;

    @ApiModelProperty(value = "数据集名称", required = true)
    @NotEmpty(message = "数据集不能为空")
    private String datasetName;

    @ApiModelProperty(value = "数据集路径", required = true)
    @NotEmpty(message = "数据集路径为空")
    private String datasetPath;

    @ApiModelProperty(value = "模型id")
    private Long modelId;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "模型路径")
    private String modelAddress;

    @ApiModelProperty(value = "优化算法类型")
    private Integer algorithmType;

    @ApiModelProperty(value = "优化算法id")
    private Long algorithmId;

    @ApiModelProperty(value = "优化算法", required = true)
    @NotEmpty(message = "请选择优化算法")
    private String algorithmName;

    @ApiModelProperty(value = "算法路径", required = true)
    @NotEmpty(message = "算法路径不能为空")
    private String algorithmPath;

    @ApiModelProperty(value = "是否编辑")
    private Boolean editAlgorithm ;

    @ApiModelProperty(value = "运行命令")
    private String command;

    @ApiModelProperty(value = "运行参数")
    private JSONObject params;
}
