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
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 普通数据集转为预置数据集请求实体
 * @date 2021-03-10
 */
@Data
public class DatasetConvertPresetDTO implements Serializable {

    @ApiModelProperty(value = "datasetId", required = true)
    @NotNull(message = "数据集id不能为空")
    private Long datasetId;


    @ApiModelProperty(value = "versionName", required = true)
    @NotNull(message = "版本名称")
    private String versionName;


    @ApiModelProperty(value = "name", required = true)
    @NotNull(message = "数据集名称")
    private String name;

}
