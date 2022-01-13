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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
/**
 * @author
 * @description
 * @date 2021-04-08
 */
@Data
public class ExperimentStageDetailUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("当前阶段最大trial数量")
    @NotNull(message = "当前阶段最大trial数量不能为空")
    @Min(value = 1,message = "当前阶段最大trial数量必须大于 0")
    private Integer maxTrialNum;

    @ApiModelProperty("trail并发数量")
    @NotNull(message = "trail并发数量不能为空")
    @Min(value = 1,message = "trail并发数量必须大于 0")
    private Integer trialConcurrentNum;

    @ApiModelProperty("当前阶段最长持续时间")
    @NotNull(message ="当前阶段最长持续时间不能为空" )
    @DecimalMin(value = "0",inclusive = false,message = "当前阶段最长持续时间必须大于0")
    private Double maxExecDuration;

    @ApiModelProperty("当前阶段最长持续时间单位 日（day），小时（hour），分钟（min）" )
    @NotBlank(message = "当前阶段最长持续时间单位不能为空")
    private String maxExecDurationUnit;
}
