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
package org.dubhe.biz.base.context;

import lombok.Data;
import org.dubhe.biz.base.dto.SysRoleDTO;
import org.dubhe.biz.base.dto.SysUserConfigDTO;

import java.io.Serializable;
import java.util.List;

/**
 * @description 用户上下文（当前登录用户）
 *  可根据需要自定义改造
 * @date 2020-12-07
 */
@Data
public class UserContext  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 性别
     */
    private String sex;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 角色
     */
    private List<SysRoleDTO> roles;
    /**
     * 头像路径
     */
    private String userAvatarPath;
    /**
     * 用户配置
     */
    private SysUserConfigDTO userConfig;

}
