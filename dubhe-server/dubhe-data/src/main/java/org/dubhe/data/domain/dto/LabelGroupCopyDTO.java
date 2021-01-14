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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 标签组复制DTO
 * @date 2020-10-16
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LabelGroupCopyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签组 Id
     */
    @NotNull(message = "标签组Id不能为空")
    private Long id;

    /**
     * 标签组名称
     */
    @NotNull(message = "标签组名称不能为空")
    @Size(min = 1, max = 50, message = "标签组长度范围只能是1~50", groups = DatasetCreateDTO.Create.class)
    private String name;

    /**
     * 描述
     */
    private String remark;




}
