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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @description 创建终端服务 DTO
 * @date 2021-07-12
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalCreateDTO extends TerminalDTO {
    @ApiModelProperty(value = "终端连接名称, 长度在1-32个字符", required = false)
    @Length(min = MagicNumConstant.ONE, max = MagicNumConstant.THIRTY_TWO, message = "训练作业名长度在1-32个字符")
    private String name;

    @ApiModelProperty(value = "数据来源名称, 长度在1-127个字符", required = false)
    @Length(min = MagicNumConstant.ONE, max = MagicNumConstant.ONE_HUNDRED_TWENTY_SEVEN, message = "数据来源名称长度在1-127个字符")
    private String dataSourceName;

    @ApiModelProperty(value = "数据来源路径, 长度在1-127个字符", required = false)
    @Length(min = MagicNumConstant.ONE, max = MagicNumConstant.ONE_HUNDRED_TWENTY_SEVEN, message = "数据来源路径长度在1-127个字符")
    private String dataSourcePath;

    @ApiModelProperty(value = "镜像版本", required = true)
    @NotBlank(message = "镜像版本不能为空")
    private String imageTag;

    @ApiModelProperty(value = "镜像名称", required = true)
    @NotBlank(message = "镜像名称不能为空")
    private String imageName;

    @ApiModelProperty(value = "镜像地址", required = true)
    @NotBlank(message = "镜像地址不能为空")
    private String imageUrl;

    @NotNull
    @Min(value = MagicNumConstant.ONE, message = "最少需要1节点")
    @ApiModelProperty(value = "总节点数")
    private Integer totalNode;

    @ApiModelProperty(value = "描述")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "描述长度不超过255个字符")
    private String description;

    @NotNull
    @ApiModelProperty(value = "是否相同规格")
    private boolean sameInfo;

    @ApiModelProperty(value = "端口")
    private Set<Integer> ports;

    @ApiModelProperty(value = "节点详情")
    private List<TerminalInfoDTO> info;

    @ApiModelProperty(value = "执行命令")
    private List<String> cmdLines;

    @ApiModelProperty(value = "镜像ssh密码")
    private String sshPwd;

    @ApiModelProperty(value = "镜像ssh用户")
    private String sshUser;

}
