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
package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 操作权限DTO
 * @date 2021-04-29
 */
@Data
public class PermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "权限id")
    private Long id;

    @ApiModelProperty(value = "父权限id")
    private Long pid;

    @ApiModelProperty("权限标识")
    private String permission;

    @ApiModelProperty(value = "权限名称")
    private String name;
}
