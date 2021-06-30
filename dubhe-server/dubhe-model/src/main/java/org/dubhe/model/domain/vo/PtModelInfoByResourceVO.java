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

package org.dubhe.model.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 根据模型类型查询返回的模型信息
 * @date 2020-11-23
 */
@Data
public class PtModelInfoByResourceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模型ID")
    private Long id;

    @ApiModelProperty("模型名称")
    private String name;

    @ApiModelProperty("模型类型")
    private Integer modelResource;

    @ApiModelProperty("模型地址")
    private String url;

    @ApiModelProperty("模型是否打包")
    private Integer packaged;

    @ApiModelProperty("框架类型")
    private Integer frameType;

}
