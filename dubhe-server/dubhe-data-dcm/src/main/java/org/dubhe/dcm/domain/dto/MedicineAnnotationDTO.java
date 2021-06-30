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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.base.constant.NumberConstant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @description 医学自动标注DTO
 * @date 2020-11-12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicineAnnotationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("医学数据集ID")
    @NotNull(message = "数据集ID不能为空")
    private Long medicalId;

    @ApiModelProperty("标注信息")
    @NotNull(message = "标注信息不能为空")
    private String annotations;

    @ApiModelProperty("操作类型 0:保存 1:完成")
    @NotNull(message = "操作类型不能为空")
    @Min(value = NumberConstant.NUMBER_0)
    @Max(value = NumberConstant.NUMBER_1)
    private Integer type;

    @ApiModelProperty("要保存的dcm标注文件id")
    private List<String> medicalFiles;
}
