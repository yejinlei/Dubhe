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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

/**
 * @description 新增用户组实体DTO
 * @date 2021-05-08
 */
@Data
public class UserGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户组id")
    private Long id;

    @ApiModelProperty(value = "用户组名称")
    @NotBlank(message = "用户组名称不能为空")
    @Length(max = 32, message = "名称长度不能超过32")
    private String name;

    @ApiModelProperty("用户组描述")
    @Length(max = 255, message = "描述长度不能超过255")
    private String description;

    @ApiModelProperty(value = "用户id集合")
    private Set<Long> userIds;

}
