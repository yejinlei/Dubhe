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

package org.dubhe.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.db.entity.BaseEntity;

import java.io.Serializable;
import java.util.Set;

/**
 * @description 角色实体
 * @date 2020-11-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("role")
public class Role extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -812009584744832371L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "name")
    private String name;

    /**
     * 权限
     */
    @TableField(value = "permission")
    private String permission;

    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private Set<Menu> menus;

    @TableField(exist = false)
    private Set<Auth> auths;

    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Boolean deleted = false;


    public @interface Update {
    }
}
