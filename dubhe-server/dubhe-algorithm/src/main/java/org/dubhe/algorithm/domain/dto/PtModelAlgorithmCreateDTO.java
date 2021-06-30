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
package org.dubhe.algorithm.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.algorithm.utils.TrainUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description 模型优化上传算法入参
 * @date 2021-01-06
 */
@Data
@Accessors(chain = true)
public class PtModelAlgorithmCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "算法名称，输入长度不能超过32个字符", required = true)
    @NotBlank(message = "算法名称不能为空")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "算法名称-输入长度不能超过32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "算法名称支持字母、数字、汉字、英文横杠和下划线")
    private String name;

    @ApiModelProperty(value = "代码目录（路径规则：/algorithm-manage/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/用户上传的算法具体文件(zip文件）名称或从notebook跳转时为/notebook/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/）", required = true)
    @NotBlank(message = "代码目录不能为空")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "代码目录-输入长度不能超过128个字符")
    private String path;

}