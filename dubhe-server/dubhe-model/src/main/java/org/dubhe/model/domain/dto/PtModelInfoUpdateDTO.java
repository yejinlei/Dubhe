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
 * @description 更新模型管理
 * @date 2020-03-24
 */
@Data
@Accessors(chain = true)
public class PtModelInfoUpdateDTO implements Serializable {

    @ApiModelProperty("模型ID")
    @NotNull(message = "模型ID不能为空")
    @Min(value = PtModelUtil.NUMBER_ONE, message = "模型ID不能小于1")
    private Long id;

    @ApiModelProperty("模型名称")
    @NotBlank(message = "模型名称不能为空")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型名称-输入长度不能超过128个字符")
    private String name;

    @ApiModelProperty("框架类型")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "框架类型错误")
    @Max(value = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "框架类型错误")
    @NotNull(message = "框架类型不能为空")
    private Integer frameType;

    @ApiModelProperty("模型类型")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型类型错误")
    @Max(value = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型类型错误")
    @NotNull(message = "模型类型不能为空")
    private Integer modelType;

    @ApiModelProperty("模型描述")
    @Length(max = PtModelUtil.NUMBER_TWO_HUNDRED_FIFTY_FIVE, message = "模型描述-输入长度不能超过256个字符")
    private String modelDescription;

    @ApiModelProperty("模型分类")
    private String modelClassName;
}
