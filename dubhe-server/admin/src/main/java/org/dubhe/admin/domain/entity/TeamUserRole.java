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

package org.dubhe.admin.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 团队用户关系实体
 * @date 2020-06-29
 */
@Data
@TableName("teams_users_roles")
public class TeamUserRole implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @NotNull()
    private Long id;

    /**
     * 团队
     */
    @TableField(exist = false)
    private Team team;

    /**
     * 用户
     */
    @TableField(exist = false)
    private User user;

    /**
     * 角色
     */
    @TableField(exist = false)
    private Role role;

}
