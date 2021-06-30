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
import lombok.*;
import org.dubhe.biz.base.constant.NumberConstant;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;


/**
 * @description 数据增强请求 DTO
 * @date 2020-06-29
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DatasetEnhanceRequestDTO implements Serializable {

    @ApiModelProperty(value = "数据集ID")
    @NotNull(message = "数据集id不能为空")
    @Min(value = NumberConstant.NUMBER_0, message = "数据集id不能为负")
    private Long datasetId;

    @ApiModelProperty(value = "数据增强类型 1.去雾 2.增雾 3.对比度增强 4.直方图均衡化")
    @NotNull(message = "增强类型不能为空")
    private List<Integer> types;

}
