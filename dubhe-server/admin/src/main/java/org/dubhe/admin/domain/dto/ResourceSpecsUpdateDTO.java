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
package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description 资源规格修改
 * @date 2021-05-27
 */
@Data
@Accessors(chain = true)
public class ResourceSpecsUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为null")
    @Min(value = MagicNumConstant.ONE, message = "id必须大于1")
    private Long id;

    @ApiModelProperty(value = "规格名称")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "规格名称错误-输入长度不能超过32个字符")
    @Pattern(regexp = StringConstant.REGEXP_SPECS, message = "规格名称支持字母、数字、汉字、英文横杠、下划线和空白字符")
    private String specsName;

    @ApiModelProperty(value = "所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving, 4：dubhe-tadl)", required = true)
    @NotNull(message = "所属业务场景不能为空")
    @Min(value = MagicNumConstant.ZERO, message = "所属业务场景错误")
    @Max(value = MagicNumConstant.FOUR, message = "所属业务场景错误")
    private Integer module;

    @ApiModelProperty(value = "CPU数量,单位：核")
    @Min(value = MagicNumConstant.ZERO, message = "CPU数量不能小于0")
    @Max(value = MagicNumConstant.TWO_BILLION, message = "CPU数量超限")
    private Integer cpuNum;

    @ApiModelProperty(value = "GPU数量，单位：核")
    @Min(value = MagicNumConstant.ZERO, message = "GPU数量不能小于0")
    @Max(value = MagicNumConstant.TWO_BILLION, message = "GPU数量超限")
    private Integer gpuNum;

    @ApiModelProperty(value = "内存大小，单位：M")
    @Min(value = MagicNumConstant.ZERO, message = "内存不能小于0")
    @Max(value = MagicNumConstant.TWO_BILLION, message = "内存数值超限")
    private Integer memNum;

    @ApiModelProperty(value = "工作空间的存储配额，单位：M")
    @Min(value = MagicNumConstant.ZERO, message = "工作空间的存储配额不能小于0")
    @Max(value = MagicNumConstant.TWO_BILLION, message = "工作空间的存储配额超限")
    private Integer workspaceRequest;
}