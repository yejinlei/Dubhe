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
package org.dubhe.measure.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @description 创建度量文件
 * @date 2021-01-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtMeasureCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "度量名称", required = true)
    @NotBlank(message = "度量名称不能为空")
    private String name;

    @ApiModelProperty("度量描述")
    private String description;

    @ApiModelProperty(value = "模型地址", required = true)
    @NotNull(message = "模型地址不能为空")
    private List<String> modelUrls;

    @ApiModelProperty(value = "探针数据集url", required = true)
    @NotBlank(message = "探针数据集url不能为空")
    private String datasetUrl;

    @ApiModelProperty(value = "数据集id", required = true)
    @NotNull(message = "数据集id不能为空")
    private Long datasetId;
}
