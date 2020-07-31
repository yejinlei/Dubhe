/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.domain.dto;

import lombok.Data;
import org.dubhe.domain.entity.Role;
import org.dubhe.domain.entity.User;
import org.dubhe.domain.entity.UserAvatar;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description 用户修改DTO
 * @date 2020-06-29
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = -6196691710092809498L;
    @NotNull(groups = User.Update.class)
    private Long id;

    @NotBlank
    @Length(max = 255, message = "名称长度不能超过255")
    private String username;

    /**
     * 用户昵称
     */
    @NotBlank
    @Length(max = 255, message = "昵称长度不能超过255")
    private String nickName;

    /**
     * 性别
     */
    private String sex;

    @NotBlank
    @Pattern(regexp = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", message = "邮箱地址格式有误")
    private String email;

    @NotBlank
    @Pattern(regexp = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$", message = "手机号格式有误")
    private String phone;

    @NotNull
    private Boolean enabled;

    private String password;

    private Date lastPasswordResetTime;

    @Length(max = 255, message = "昵称长度不能超过255")
    private String remark;

    private Long avatarId;


    private UserAvatar userAvatar;


    @NotEmpty
    private List<Role> roles;

    private Boolean deleted;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserUpdateDTO user = (UserUpdateDTO) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    public @interface Update {
    }
}
