/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.domain.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description model 查询dto
 * @date 2020-10-09
 */
@Data
@ApiModel("模型查询")
public class ModelQueryDTO {
    @ApiModelProperty(value = "模型类型")
    private Integer modelResource;
    @ApiModelProperty(value = "模型名称")
    private String  name;
    @ApiModelProperty(value = "模型版本")
    private String version;
    @ApiModelProperty(value = "模型id")
    private Integer id;
    @ApiModelProperty(value = "模型路径")
    private String modelPath;
}
