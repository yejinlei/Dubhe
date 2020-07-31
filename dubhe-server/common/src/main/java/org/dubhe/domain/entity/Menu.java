/**
 * Copyright 2019-2020 Zheng Jie
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
 */
 
package org.dubhe.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.base.BaseEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @description 菜单实体
 * @date 2020-06-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("menu")
public class Menu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 3100515433018008777L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank
    private String name;

    @TableField(value = "sort")
    private Long sort = 999L;

    @TableField(value = "path")
    private String path;

    @TableField(value = "component")
    private String component;

    /**
     * 类型，目录、菜单、按钮
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 权限
     */
    @TableField(value = "permission")
    private String permission;

    @TableField(value = "component_name")
    private String componentName;

    @TableField(value = "icon")
    private String icon;

    /**
     * 布局类型
     */
    @TableField(value = "layout")
    private String layout;

    @TableField(value = "cache")
    private Boolean cache;

    @TableField(value = "hidden")
    private Boolean hidden;

    /**
     * 上级菜单ID
     */
    @TableField(value = "pid")
    private Long pid;

    @TableField(value = "deleted",fill = FieldFill.INSERT)
    private Boolean deleted;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Menu menu = (Menu) o;
        return Objects.equals(id, menu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public @interface Update {
    }
}
