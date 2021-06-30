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

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * @description 用户实体
 * @date 2020-11-29
 */
@Data
@TableName("user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -3836401769559845765L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "username")
    private String username;

    /**
     * 用户昵称
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 性别
     */
    @TableField(value = "sex")
    private String sex;

    @TableField(value = "email")
    private String email;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "enabled")
    private Boolean enabled;

    @TableField(value = "password")
    private String password;

    @TableField(value = "last_password_reset_time")
    private Date lastPasswordResetTime;

    @TableField(value = "remark")
    private String remark;

    @TableField(value = "avatar_id")
    private Long avatarId;

    @TableField(value = "deleted",fill = FieldFill.INSERT)
    private Boolean deleted = false;

    @TableField(exist = false)
    private UserAvatar userAvatar;


    @NotEmpty
    @TableField(exist = false)
    private List<Role> roles;

}
