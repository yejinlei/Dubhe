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
package org.dubhe.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.*;
import org.dubhe.service.RoleService;
import org.dubhe.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

/**
 * @Description 角色管理 控制器
 * @Date 2020-06-01
 */
@Api(tags = "系统：角色管理")
@RestController
@RequestMapping("/api/{version}/roles")
public class RoleController {

    private final RoleService roleService;

    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @ApiOperation("查询角色")
    @GetMapping
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody getRoles(RoleQueryDTO criteria, Page page) {
        return new DataResponseBody(roleService.queryAll(criteria, page));
    }

    @ApiOperation("返回全部的角色")
    @GetMapping(value = "/all")
    @RequiresPermissions(value = {Permissions.SYSTEM_ROLE, Permissions.SYSTEM_USER}, logical = Logical.OR)
    public DataResponseBody getAll(RoleQueryDTO criteria) {
        return new DataResponseBody(roleService.queryAllSmall(criteria));
    }

    @ApiOperation("获取单个role")
    @GetMapping(value = "/{id}")
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody getRoles(@PathVariable Long id) {
        return new DataResponseBody(roleService.findById(id));
    }

    @ApiOperation("新增角色")
    @PostMapping
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody create(@Valid @RequestBody RoleCreateDTO resources) {
        return new DataResponseBody(roleService.create(resources));
    }

    @ApiOperation("修改角色")
    @PutMapping
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody update(@Valid @RequestBody RoleUpdateDTO resources) {
        roleService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("修改角色菜单")
    @PutMapping(value = "/menu")
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody updateMenu(@Valid @RequestBody RoleUpdateDTO resources) {
        RoleDTO role = roleService.findById(resources.getId());
        if (Objects.isNull(role)) {
            throw new BadCredentialsException("请选择角色信息");
        }
        roleService.updateMenu(resources, role);
        return new DataResponseBody();
    }

    @ApiOperation("删除角色")
    @DeleteMapping
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public DataResponseBody delete(@RequestBody RoleDeleteDTO deleteDTO) {
        roleService.delete(deleteDTO.getIds());
        return new DataResponseBody();
    }

    @ApiOperation("导出角色数据")
    @GetMapping(value = "/download")
    @RequiresPermissions(Permissions.SYSTEM_ROLE)
    public void download(HttpServletResponse response, RoleQueryDTO criteria) throws IOException {
        roleService.download(roleService.queryAll(criteria), response);
    }

}
