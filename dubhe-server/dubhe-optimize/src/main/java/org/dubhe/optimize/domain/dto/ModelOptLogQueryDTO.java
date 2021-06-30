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

package org.dubhe.optimize.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @description 模型优化日志查询参数
 * @date 2020-05-29
 */
@Data
public class ModelOptLogQueryDTO {

    @ApiModelProperty("模型优化实例ID")
    @NotNull(message = "模型优化实例ID")
    private Long instId;

    @ApiModelProperty(value = "起始行")
    @Min(value = 0, message = "startLine不能小于0")
    private Integer startLine;

    @ApiModelProperty(value = "行数")
    @Min(value = 1, message = "lines不能小于1")
    @Max(value = 1000, message = "lines不能大于1000")
    private Integer lines;

}
