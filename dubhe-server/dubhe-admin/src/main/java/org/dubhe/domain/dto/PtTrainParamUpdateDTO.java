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

import javax.validation.constraints.*;

/**
 * @description 任务参数修改条件
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainParamUpdateDTO extends BaseImageDTO {

    @ApiModelProperty(value = "任务参数id,任务参数id不能小于1", required = true)
    @NotNull(message = "任务参数id不能为空")
    @Min(value = TrainUtil.NUMBER_ONE, message = "任务参数id不能小于1")
    private Long id;

    @ApiModelProperty(value = "任务参数名称,输入长度不能超过32个字符", required = true)
    @Length(max = TrainUtil.NUMBER_THIRTY_TWO, message = "任务参数名称-输入长度不能超过32个字符")
    @NotBlank(message = "任务参数名称不能为空")
    @Pattern(regexp = TrainUtil.REGEXP, message = "任务参数名称支持字母、数字、汉字、英文横杠和下划线")
    private String paramName;

    @ApiModelProperty("描述,输入长度不能超过256个字符")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "任务参数描述-输入长度不能超过256个字符")
    private String description;

    @ApiModelProperty(value = "算法id,算法id不能小于1", required = true)
    @NotNull(message = "算法id不能为空")
    @Min(value = TrainUtil.NUMBER_ONE, message = "算法id不能小于1")
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
