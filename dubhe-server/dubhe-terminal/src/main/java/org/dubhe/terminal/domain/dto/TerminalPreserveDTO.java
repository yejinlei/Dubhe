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

package org.dubhe.terminal.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 保存终端 DTO
 * @date 2021-07-13
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalPreserveDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "terminalId")
    @NotNull(message = "terminalId不能为空")
    @Min(value = MagicNumConstant.ONE, message = "terminalId数值不合法")
    private Long id;

    @ApiModelProperty(value = "镜像名称", required = true)
    @NotBlank(message = "镜像名称不能为空")
    private String imageName;

    @ApiModelProperty(value = "镜像版本", required = true)
    @NotBlank(message = "镜像版本不能为空")
    private String imageTag;

    @ApiModelProperty(value = "镜像描述", required = false)
    private String imageRemark;

    @ApiModelProperty(value = "密码", required = false)
    private String password;
}
