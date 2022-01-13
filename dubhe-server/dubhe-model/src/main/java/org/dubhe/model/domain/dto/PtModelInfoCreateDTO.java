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

package org.dubhe.model.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 创建模型管理
 * @date 2020-03-24
 */
@Data
@Accessors(chain = true)
public class PtModelInfoCreateDTO implements Serializable {

    @ApiModelProperty("模型名称")
    @NotBlank(message = "模型名称不能为空")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型名称-输入长度不能超过128个字符")
    private String name;

    @ApiModelProperty("框架类型")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "框架类型错误")
    @Max(value = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "框架类型错误")
    @NotNull(message = "框架类型不能为空")
    private Integer frameType;

    @ApiModelProperty("模型格式")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型格式错误")
    @Max(value = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型格式错误")
    @NotNull(message = "模型格式不能为空")
    private Integer modelType;

    @ApiModelProperty("模型描述")
    @Length(max = PtModelUtil.NUMBER_TWO_HUNDRED_FIFTY_FIVE, message = "模型描述-输入长度不能超过256个字符")
    private String modelDescription;

    @ApiModelProperty("模型分类")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型分类-输入长度不能超过128个字符")
    private String modelClassName;

    @ApiModelProperty("模型地址")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型地址-输入长度不能超过128个字符")
    private String modelAddress;

    @ApiModelProperty("模型来源(0用户上传，1训练输出，2模型优化，3模型转换，4自动机器学习)")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型来源错误")
    @Max(value = PtModelUtil.NUMBER_FOUR, message = "模型来源错误")
    private Integer modelSource;

    @ApiModelProperty("算法ID")
    private Long algorithmId;

    @ApiModelProperty("算法名称")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "算法名称-输入长度不能超过128个字符")
    private String algorithmName;

    @ApiModelProperty("算法来源(1为我的算法，2为预置算法)")
    @Min(value = PtModelUtil.NUMBER_ONE, message = "算法来源错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "算法来源错误")
    private Integer algorithmSource;

    @ApiModelProperty("模型类型")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型类型错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "模型类型错误")
    private Integer modelResource;

}
