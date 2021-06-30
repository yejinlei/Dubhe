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
package org.dubhe.biz.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
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
public class ModelOptAlgorithmCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "算法名称不能为空")
    @Length(max = NumberConstant.NUMBER_32, message = "算法名称-输入长度不能超过32个字符")
    @Pattern(regexp = StringConstant.REGEXP_ALGORITHM, message = "算法名称支持字母、数字、汉字、英文横杠和下划线")
    private String name;

    @NotBlank(message = "代码目录不能为空")
    @Length(max = NumberConstant.NUMBER_64, message = "代码目录-输入长度不能超过128个字符")
    private String path;

}