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
 * @description 删除标签组
 * @date 2020-09-23
 */
@Data
public class LabelDeleteDTO implements Serializable {

    @ApiModelProperty(value = "datasetId", required = true)
    @NotNull(message = "数据集id不能为空")
    private Long datasetId;

    @ApiModelProperty(value = "labelId", required = true)
    @NotNull(message = "标签id不能为空")
    private Long labelId;

}
