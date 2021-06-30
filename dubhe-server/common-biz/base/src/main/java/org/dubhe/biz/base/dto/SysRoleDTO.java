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
import java.util.List;


/**
 * @description 系统角色DTO
 * @date 2020-06-29
 */
@Data
public class SysRoleDTO  implements Serializable {

    private static final long serialVersionUID = -3836401769559845765L;

    /**
     * 权限列表
     */
    private List<SysPermissionDTO> permissions;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色ID
     */
    private Long id;

}
