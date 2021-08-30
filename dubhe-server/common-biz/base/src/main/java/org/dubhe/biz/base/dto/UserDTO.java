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
package org.dubhe.biz.base.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @description 用户信息
 * @date 2020-06-29
 */
@Data
public class UserDTO implements Serializable {

    private Long id;

    private String username;

    private String nickName;

    private String sex;

    private String email;

    private String phone;

    private Boolean enabled;

    private String remark;

    private Date lastPasswordResetTime;

    private Timestamp createTime;

    /**
     * 头像路径
     */
    private String userAvatarPath;

    /**
     * 角色
     */
    private List<SysRoleDTO> roles;

    /**
     * 用户配置
     */
    private SysUserConfigDTO userConfig;


}
