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
package org.dubhe.train.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @description 镜像基础类DTO
 * @date 2020-07-14
 */
@Data
@Accessors(chain = true)
public class BaseImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "镜像版本", required = true)
    @NotBlank(message = "镜像版本不能为空")
    private String imageTag;

    @ApiModelProperty(value = "镜像名称", required = true)
    @NotBlank(message = "镜像名称不能为空")
    private String imageName;

}