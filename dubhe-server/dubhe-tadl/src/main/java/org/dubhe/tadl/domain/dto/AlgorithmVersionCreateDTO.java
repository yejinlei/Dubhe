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
package org.dubhe.tadl.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 算法版本
 * @date 2020-05-14
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AlgorithmVersionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 算法ID
     */
    @ApiModelProperty(value = "算法ID")
    @NotNull(message = "算法ID不能为空")
    private Long id;

    /**
     * 算法名称
     */
    @ApiModelProperty("算法名称")
    @NotNull(message = "算法名称不能为空")
    private String name;

    /**
     * 当前版本
     */
    @ApiModelProperty(value = "当前版本")
    private String currentVersion;

    /**
     * 下一版本名称
     */
    @ApiModelProperty(value = "下一版本名称")
    @NotBlank(message = "下一版本名称不能为空")
    private String nextVersion;

    /**
     * 版本描述
     */
    @ApiModelProperty(value = "版本说明")
    @Size(max = 50, message = "版本描述长度应小于50字符")
    private String description;

}
