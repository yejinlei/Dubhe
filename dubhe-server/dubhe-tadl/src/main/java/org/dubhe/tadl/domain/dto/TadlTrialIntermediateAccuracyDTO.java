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
import org.dubhe.tadl.constant.TadlConstant;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description trial中间精度DTO
 * @date 2020-12-28
 */
@Data
public class TadlTrialIntermediateAccuracyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("实验id")
    @NotNull(message = "实验id不能为空")
    @Min(value = TadlConstant.NUMBER_ONE, message = "实验id不能小于1")
    private Integer experimentId;

    @ApiModelProperty("trial中间精度的序号")
    @Min(value = TadlConstant.NUMBER_ONE, message = "trial中间精度的序号不能小于1")
    private Integer sequence;

    @ApiModelProperty("trial中间精度筛选的下限精度")
    @Min(value = TadlConstant.NUMBER_ZERO, message = "trial中间精度的下限精度不能小于0")
    private Double accuracyFrom;

    @ApiModelProperty("trial中间精度筛选的上限精度")
    @Min(value = TadlConstant.NUMBER_ZERO, message = "trial中间精度的上限精度不能小于0")
    private Double accuracyTo;

    @ApiModelProperty("实验阶段")
    @NotNull(message = "实验阶段不能为空")
    @Min(value = TadlConstant.NUMBER_ONE, message = "实验阶段不能小于1")
    private Integer experimentStage;

}
