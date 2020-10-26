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
package org.dubhe.k8s.domain.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @description Pod基础信息查询入参
 * @date 2020-08-14
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
@Api("Pod基础信息查询入参")
public class PodQueryDTO {

    @ApiModelProperty(value ="命名空间",required = true)
    @NotNull(message = "命名空间不能为空")
    private String namespace;

    @ApiModelProperty(value ="资源名称",required = true)
    @NotNull(message = "资源名称不能为空")
    private String resourceName;

}
