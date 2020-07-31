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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.base.BaseImageDTO;
import org.dubhe.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description 训练任务修改
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainJobUpdateDTO extends BaseImageDTO {

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为null")
    @Min(value = TrainUtil.NUMBER_ONE, message = "id必须大于1")
    private Long id;

    @ApiModelProperty("描述, 长度不能超过255个字符")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "描述长度不能超过255个字符")
    private String description;

    @ApiModelProperty("算法id")
    @NotNull(message = "algorithmId不能为空")
    @Min(value = TrainUtil.NUMBER_ONE, message = "algorithmId必须大于1")
    private Long algorithmId;

    @ApiModelProperty(value = "运行命令,输入长度不能超过128个字符", required = true)
    @NotBlank(message = "运行命令不能为空")
    @Length(max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_EIGHT, message = "运行命令-输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty(value = "数据集来源路径,输入长度不能超过127个字符", required = true)
    @NotBlank(message = "数据集来源路径不能为空")
    @Length(max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_SEVEN, message = "数据集来源路径-输入长度不能超过127个字符")
    private String dataSourcePath;

    @ApiModelProperty(value = "数据集来源名称,输入长度不能超过127个字符", required = true)
    @NotBlank(message = "数据集来源名称不能为空")
    @Length(max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_SEVEN, message = "数据集来源名称-输入长度不能超过127个字符")
    private String dataSourceName;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)")
    private JSONObject runParams;

    @ApiModelProperty(value = "类型(0为CPU，1为GPU)", required = true)
    @Min(value = TrainUtil.NUMBER_ZERO, message = "类型错误")
    @Max(value = TrainUtil.NUMBER_ONE, message = "类型错误")
    @NotNull(message = "类型(0为CPU，1为GPU)不能为空")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "规格类型Id", required = true)
    @NotNull(message = "规格类型id不能为空")
    @Min(value = TrainUtil.NUMBER_ONE, message = "规格类型id必须不小于1")
    private Integer trainJobSpecsId;

}
