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
import java.io.Serializable;

/**
 * @description 删除终端服务DTO
 * @date 2021-07-13
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TerminalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @Min(value = MagicNumConstant.ONE, message = "id数值不合法")
    private Long id;

    public TerminalDTO(Long id){
        this.id = id;
    }
}
