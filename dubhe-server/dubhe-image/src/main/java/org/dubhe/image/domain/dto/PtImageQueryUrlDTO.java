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
package org.dubhe.image.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @description 查询镜像路径
 * @date 2020-12-14
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class PtImageQueryUrlDTO {

    @ApiModelProperty(value = "镜像来源(0为我的镜像, 1为预置镜像)")
    private Integer imageResource;

    @ApiModelProperty(value = "镜像名称")
    private String imageName;

    @ApiModelProperty(value = "镜像标签")
    private String imageTag;

    @ApiModelProperty(value = "镜像项目类型(0:notebook , 1:train , 2:serving)")
    private Integer projectType;

}
