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
package org.dubhe.dcm.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @description 病灶层面信息UpdateDTO
 * @date 2020-12-23
 */
@Data
public class DataLesionSliceUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "lesionOrder", required = true)
    @NotNull(message = "lesionOrder不能为空")
    private Integer lesionOrder;

    @ApiModelProperty(value = "sliceDesc", required = true)
    @NotNull(message = "sliceDesc不能为空")
    private String sliceDesc;

    @ApiModelProperty("标注信息")
    private List<DataLesionDrawInfoDTO> list;
}
