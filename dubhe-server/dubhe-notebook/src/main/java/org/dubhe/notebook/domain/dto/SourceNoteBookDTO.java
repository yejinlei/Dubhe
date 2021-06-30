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

package org.dubhe.notebook.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 第三方创建NoteBook请求对象
 * @date 2020-05-15
 */
@Data
public class SourceNoteBookDTO implements Serializable {

    @ApiModelProperty("第三方源主键")
    @NotNull(message = "sourceId不能为空")
    private Long sourceId;

    @ApiModelProperty("第三方源资源路径")
    @NotBlank(message = "sourceFilePath不能为空")
    private String sourceFilePath;

}
