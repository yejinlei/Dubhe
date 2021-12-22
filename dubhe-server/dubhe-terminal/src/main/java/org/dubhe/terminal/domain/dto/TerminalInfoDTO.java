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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.terminal.domain.entity.TerminalInfo;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description 节点详情DTO
 * @date 2021-07-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TerminalInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @Min(value = MagicNumConstant.ONE, message = "id数值不合法")
    private Long id;

    @NotNull
    @Min(value = MagicNumConstant.ONE, message = "最少需要一个CPU")
    @ApiModelProperty(value = "cpu数量")
    private Integer cpuNum;

    @Min(value = MagicNumConstant.ZERO, message = "GPU数量不能小于0")
    @ApiModelProperty(value = "gpu数量")
    private Integer gpuNum;

    @NotNull
    @Min(value = MagicNumConstant.ONE, message = "最少需要1G内存")
    @ApiModelProperty(value = "内存大小")
    private Integer memNum;

    @ApiModelProperty(value = "磁盘大小（M）")
    private Integer diskMemNum;

    @ApiModelProperty(value = "GPU类型(例如：NVIDIA)")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU类型错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_GPU_TYPE, message = "支持字母、数字、汉字、英文横杠、英文.号、空白字符和英文斜杠")
    private String gpuType;

    @ApiModelProperty(value = "GPU型号(例如：v100)")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU型号错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_GPU_MODEL, message = "支持小写字母、数字、英文横杠、英文.号和英文斜杠")
    private String gpuModel;

    @ApiModelProperty(value = "k8s GPU资源标签key值(例如：nvidia.com/gpu)")
    @Length(max = MagicNumConstant.SIXTY_FOUR, message = "GPU型号错误-输入长度不能超过64个字符")
    @Pattern(regexp = StringConstant.REGEXP_K8S, message = "支持小写字母、数字、英文横杠、英文.号和英文斜杠")
    private String k8sLabelKey;

    public TerminalInfo toTerminalInfo(Long terminalId,String k8sResourceName,Long originUserId,String sshUser,String sshPwd){
        return new TerminalInfo(id,terminalId,cpuNum,memNum,gpuNum,diskMemNum,gpuType,gpuModel,k8sLabelKey,k8sResourceName,originUserId,sshUser,sshPwd);
    }
}
