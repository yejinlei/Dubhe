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
package org.dubhe.admin.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.UserGroupDTO;
import org.dubhe.admin.domain.dto.UserGroupDeleteDTO;
import org.dubhe.admin.domain.dto.UserGroupQueryDTO;
import org.dubhe.admin.domain.dto.UserGroupUpdDTO;
import org.dubhe.admin.domain.dto.UserRoleUpdateDTO;
import org.dubhe.admin.domain.dto.UserStateUpdateDTO;
import org.dubhe.admin.service.UserGroupService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 用户组管理 控制器
 * @date 2021-05-10
 */
@Api(tags = "系统：用户组管理")
@RestController
@RequestMapping("/group")
public class UserGroupController {

    @Autowired
    private UserGroupService userGroupService;

    @GetMapping
    @ApiOperation("获取用户组列表")
    public DataResponseBody queryAll(UserGroupQueryDTO queryDTO) {
        return new DataResponseBody(userGroupService.queryAll(queryDTO));
    }

    @PostMapping
    @ApiOperation("创建用户组")
    @PreAuthorize(Permissions.USER_GROUP_CREATE)
    public DataResponseBody create(@Validated @RequestBody UserGroupDTO groupCreateDTO) {
        return new DataResponseBody(userGroupService.create(groupCreateDTO));
    }

    @PutMapping
    @ApiOperation("修改用户组信息")
    @PreAuthorize(Permissions.USER_GROUP_EDIT)
    public DataResponseBody update(@Validated @RequestBody UserGroupDTO groupUpdateDTO) {
        userGroupService.update(groupUpdateDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除用户组")
    @PreAuthorize(Permissions.USER_GROUP_DELETE)
    public DataResponseBody delete(@RequestBody UserGroupDeleteDTO groupDeleteDTO) {
        userGroupService.delete(groupDeleteDTO.getIds());
        return new DataResponseBody();
    }

    @PostMapping("/updateUser")
    @ApiOperation("修改组成员")
    @PreAuthorize(Permissions.USER_GROUP_EDIT_USER)
    public DataResponseBody updUserWithGroup(@Validated @RequestBody UserGroupUpdDTO userGroupUpdDTO) {
        userGroupService.updUserWithGroup(userGroupUpdDTO);
        return new DataResponseBody();
    }

    @DeleteMapping("/deleteUser")
    @ApiOperation("移除组成员")
    @PreAuthorize(Permissions.USER_GROUP_EDIT_USER)
    public DataResponseBody delUserWithGroup(@Validated @RequestBody UserGroupUpdDTO userGroupDelDTO) {
        userGroupService.delUserWithGroup(userGroupDelDTO);
        return new DataResponseBody();
    }

    @GetMapping("/findUser")
    @ApiOperation("获取没有归属组的用户")
    public DataResponseBody findUserWithOutGroup() {
        return new DataResponseBody(userGroupService.findUserWithOutGroup());
    }

    @GetMapping("/byGroupId")
    @ApiOperation("获取某个用户组的成员")
    public DataResponseBody queryUserByGroupId(Long groupId) {
        return new DataResponseBody(userGroupService.queryUserByGroupId(groupId));
    }

    @PutMapping("/userState")
    @ApiOperation("批量修改组用户的状态(激活/锁定)")
    @PreAuthorize(Permissions.USER_GROUP_EDIT_USER_STATE)
    public DataResponseBody updateUserState(@Validated @RequestBody UserStateUpdateDTO userStateUpdateDTO) {
        userGroupService.updateUserState(userStateUpdateDTO);
        return new DataResponseBody();
    }

    @DeleteMapping("/delete")
    @ApiOperation("批量删除组用户")
    @PreAuthorize(Permissions.USER_GROUP_DELETE_USER)
    public DataResponseBody delUser(@Validated @RequestBody UserGroupUpdDTO userGroupUpdDTO) {
        userGroupService.delUser(userGroupUpdDTO);
        return new DataResponseBody();
    }

    @PutMapping("/userRoles")
    @ApiOperation("批量修改组成员角色")
    @PreAuthorize(Permissions.USER_GROUP_EDIT_USER_ROLE)
    public DataResponseBody updateUserRole(@Validated @RequestBody UserRoleUpdateDTO userRoleUpdateDTO) {
        userGroupService.updateUserRole(userRoleUpdateDTO);
        return new DataResponseBody();
    }
}
