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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description GPU资源修改
 * @date 2021-08-20
 */
@Data
@Accessors(chain = true)
public class GpuResourceUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为null")
    @Min(value = MagicNumConstant.ONE, message = "id必须大于1")
    private Long id;

    @ApiModelProperty(value = "GPU类型(例如：NVIDIA)", required = true)
    @NotBlank(message = "GPU类型")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU类型错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_GPU_TYPE, message = "支持字母、数字、汉字、英文横杠、英文.号、空白字符和英文斜杠")
    private String gpuType;

    @ApiModelProperty(value = "GPU型号(例如：v100)", required = true)
    @NotBlank(message = "GPU型号")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU型号错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_GPU_MODEL, message = "支持小写字母、数字、英文横杠、英文.号、空白字符和英文斜杠")
    private String gpuModel;

    @ApiModelProperty(value = "k8s GPU资源标签key值(例如：nvidia.com/gpu)", required = true)
    @NotBlank(message = "k8s GPU资源标签key值")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU型号错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_K8S, message = "支持小写字母、数字、英文横杠、英文.号和英文斜杠")
    private String k8sLabelKey;
}