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
import org.dubhe.terminal.domain.entity.TerminalInfo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    public TerminalInfo toTerminalInfo(Long terminalId,String k8sResourceName,Long originUserId,String sshUser,String sshPwd){
        return new TerminalInfo(id,terminalId,cpuNum,memNum,gpuNum,diskMemNum,k8sResourceName,originUserId,sshUser,sshPwd);
    }
}
