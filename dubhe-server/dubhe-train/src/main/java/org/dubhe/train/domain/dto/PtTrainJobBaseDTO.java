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

package org.dubhe.train.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @description 创建训练或者修改训练的基础数据包
 * @date 2021-08-19
 */
@Data
@Accessors(chain = true)
public class PtTrainJobBaseDTO extends BaseImageDTO {

    @ApiModelProperty(value = "Notebook Id")
    private Long notebookId;

    @ApiModelProperty(value = "算法来源id")
    private Long algorithmId;

    @ApiModelProperty(value = "运行命令,输入长度不能超过128个字符", required = true)
    @NotBlank(message = "运行命令不能为空")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "运行命令-输入长度不能超过128个字符")
    private String runCommand;

}
