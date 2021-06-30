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

import lombok.Data;
import org.dubhe.admin.domain.entity.Menu;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @description 角色修改 DTO
 * @date 2020-06-29
 */
@Data
public class RoleUpdateDTO implements Serializable {

    private static final long serialVersionUID = -8685787591892312697L;

    private Long id;

    @Size(max = 255, message = "名称长度不能超过255")
    private String name;

    /**
     * 权限
     */
    @Size(max = 255, message = "默认权限长度不能超过255")
    private String permission;

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;

    private Set<Menu> menus;

    private Boolean deleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleUpdateDTO role = (RoleUpdateDTO) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public @interface Update {
    }

}
