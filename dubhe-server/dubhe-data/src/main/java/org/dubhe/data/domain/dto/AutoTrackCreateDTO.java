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

package org.dubhe.data.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @descritption 自动跟踪
 * @date 2020-04-17
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AutoTrackCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(notes = "code:199算法失败，200成功")
    @NotNull(message = "code不能为空")
    private Integer code;

    @ApiModelProperty(notes = "调用结果信息: success,  ret")
    private String msg;

    @ApiModelProperty(notes = "回调数据")
    private String data;

    @ApiModelProperty(notes = "追踪编号")
    private String traceId;

}
