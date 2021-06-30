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
package org.dubhe.admin.service;

import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.Group;
import org.dubhe.admin.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 用户组服务类
 * @date 2021-05-06
 */
public interface UserGroupService {


    /**
     * 分页查询用户组列表
     *
     * @param queryDTO 查询实体DTO
     * @return Map<String, Object>  用户组及分页信息
     */
    Map<String, Object> queryAll(UserGroupQueryDTO queryDTO);

    /**
     * 新增用户组
     *
     * @param groupCreateDTO 新增用户组实体DTO
     */
    Group create(UserGroupDTO groupCreateDTO);

    /**
     * 修改用户组信息
     *
     * @param groupUpdateDTO 修改用户组实体DTO
     */
    void update(UserGroupDTO groupUpdateDTO);

    /**
     * 删除用户组
     *
     * @param ids 用户组ids
     */
    void delete(Set<Long> ids);

    /**
     * 修改用户组成员
     *
     * @param userGroupUpdDTO 新增组用户DTO实体
     */
    void updUserWithGroup(UserGroupUpdDTO userGroupUpdDTO);

    /**
     * 删除用户组成员
     *
     * @param userGroupDelDTO 删除用户组成员
     */
    void delUserWithGroup(UserGroupUpdDTO userGroupDelDTO);

    /**
     * 获取没有归属组的用户
     *
     * @return List<User> 没有归属组的用户
     */
    List<User> findUserWithOutGroup();

    /**
     *  获取用户组成员信息
     *
     * @param groupId 用户组id
     * @return List<User> 用户列表
     */
    List<User> queryUserByGroupId(Long groupId);

    /**
     * 批量修改用户组成员的状态
     *
     * @param userStateUpdateDTO 实体DTO
     */
    void updateUserState(UserStateUpdateDTO userStateUpdateDTO);

    /**
     * 批量删除用户组用户
     *
     * @param userGroupUpdDTO 批量删除用户组用户DTO
     */
    void delUser(UserGroupUpdDTO userGroupUpdDTO);

    /**
     * 批量修改用户组用户的角色
     *
     * @param userRoleUpdateDTO
     */
    void updateUserRole(UserRoleUpdateDTO userRoleUpdateDTO);
}
