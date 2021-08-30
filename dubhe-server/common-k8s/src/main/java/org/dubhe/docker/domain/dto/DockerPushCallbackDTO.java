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

package org.dubhe.docker.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @description docker 推送镜像回调
 * @date 2021-07-27
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DockerPushCallbackDTO {
    @ApiModelProperty(value = "terminalId", required = true)
    @Min(value = MagicNumConstant.ONE, message = "id数值不合法")
    private Long terminalId;

    @ApiModelProperty(value = "错误信息", required = false)
    private String errorMessage;

    @NotNull
    @ApiModelProperty(value = "是否错误 true:错误 false:成功")
    private boolean error;

    @ApiModelProperty(value = "用户id", required = false)
    private Long userId;

    public DockerPushCallbackDTO(Long terminalId,Long userId){
        this.terminalId = terminalId;
        this.userId = userId;
    }

    public DockerPushCallbackDTO(Long terminalId, String errorMessage, boolean error,Long userId){
        this.terminalId = terminalId;
        this.errorMessage = errorMessage;
        this.error = error;
        this.userId = userId;
    }
}
