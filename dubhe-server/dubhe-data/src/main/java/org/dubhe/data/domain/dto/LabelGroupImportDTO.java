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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 标签组导入DTO
 * @date 2020-10-16
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LabelGroupImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签组name
     */
    @ApiModelProperty(value = "标签组name")
    @NotNull(message = "名称不能为空")
    @Size(min = 1, max = 50, message = "name长度范围1~50")
    private String name;

    /**
     * 标签组类型
     */
    @ApiModelProperty(value = "标签组类型:0:视觉,1:文本")
    @NotNull(message = "标签组类型不能为空")
    private Integer labelGroupType;

    /**
     * 备注信息
     */
    @ApiModelProperty(notes = "备注信息")
    private String remark;


}
