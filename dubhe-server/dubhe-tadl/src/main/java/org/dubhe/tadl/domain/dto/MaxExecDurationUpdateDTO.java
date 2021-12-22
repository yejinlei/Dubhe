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
package org.dubhe.tadl.domain.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 运行时参数修改 DTO
 *
 * @date 2021-03-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel
public class MaxExecDurationUpdateDTO {

    @ApiModelProperty("实验ID")
    @NotNull(message = "实验ID不能为空")
    private Long experimentId;

    @ApiModelProperty("实验阶段")
    @NotNull(message = "实验阶段不能为空")
    private Integer stageOrder;

    @ApiModelProperty("最大执行时间")
    @NotNull(message = "最大执行时间不能为空")
    private Double maxExecDuration;

    @ApiModelProperty("最大执行时间单位")
    @NotNull(message = "最大执行时间单位不能为空")
    private String maxExecDurationUnit;

}
