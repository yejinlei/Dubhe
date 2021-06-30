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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 我的模型转预置模型
 * @date 2021-03-18
 */
@Data
@Accessors(chain = true)
public class ModelConvertPresetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模型版本ID")
    @Min(value = PtModelUtil.NUMBER_ONE, message = "模型版本ID不能小于1")
    @NotNull(message = "模型版本ID不能为空")
    private Long id;

    @ApiModelProperty("模型名称")
    @NotBlank(message = "模型名称不能为空")
    @Length(max = PtModelUtil.NUMBER_ONE_HUNDRED_TWENTY_EIGHT, message = "模型名称-输入长度不能超过128个字符")
    private String name;

    @ApiModelProperty("模型描述")
    @Length(max = PtModelUtil.NUMBER_TWO_HUNDRED_FIFTY_FIVE, message = "模型描述-输入长度不能超过256个字符")
    private String modelDescription;

}