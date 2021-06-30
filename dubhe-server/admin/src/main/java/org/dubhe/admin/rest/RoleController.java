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
package org.dubhe.admin.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.RoleAuthUpdateDTO;
import org.dubhe.admin.domain.dto.RoleCreateDTO;
import org.dubhe.admin.domain.dto.RoleDTO;
import org.dubhe.admin.domain.dto.RoleDeleteDTO;
import org.dubhe.admin.domain.dto.RoleQueryDTO;
import org.dubhe.admin.domain.dto.RoleUpdateDTO;
import org.dubhe.admin.service.AuthCodeService;
import org.dubhe.admin.service.RoleService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;


/**
 * @description 角色管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：角色管理")
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    private AuthCodeService authCodeService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @ApiOperation("查询角色")
    @GetMapping
    @PreAuthorize(Permissions.ROLE)
    public DataResponseBody getRoles(RoleQueryDTO criteria, Page page) {
        return new DataResponseBody(roleService.queryAll(criteria, page));
    }

    @ApiOperation("返回全部的角色")
    @GetMapping(value = "/all")
    @PreAuthorize(Permissions.ROLE)
    public DataResponseBody getAll(RoleQueryDTO criteria) {
        return new DataResponseBody(roleService.queryAllSmall(criteria));
    }

    @ApiOperation("获取单个role")
    @GetMapping(value = "/{id}")
    @PreAuthorize(Permissions.ROLE)
    public DataResponseBody getRoles(@PathVariable Long id) {
        return new DataResponseBody(roleService.findById(id));
    }

    @ApiOperation("新增角色")
    @PostMapping
    @PreAuthorize(Permissions.ROLE_CREATE)
    public DataResponseBody create(@Valid @RequestBody RoleCreateDTO resources) {
        return new DataResponseBody(roleService.create(resources));
    }

    @ApiOperation("修改角色")
    @PutMapping
    @PreAuthorize(Permissions.ROLE_EDIT)
    public DataResponseBody update(@Valid @RequestBody RoleUpdateDTO resources) {
        roleService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("修改角色菜单")
    @PutMapping(value = "/menu")
    @PreAuthorize(Permissions.ROLE_MENU)
    public DataResponseBody updateMenu(@Valid @RequestBody RoleUpdateDTO resources) {
        RoleDTO role = roleService.findById(resources.getId());
        if (Objects.isNull(role)) {
            throw new BadCredentialsException("请选择角色信息");
        }
        roleService.updateMenu(resources, role);
        return new DataResponseBody();
    }

    @ApiOperation("修改角色权限")
    @PutMapping(value = "/auth")
    @PreAuthorize(Permissions.ROLE_AUTH)
    public DataResponseBody updateAuth(@Valid @RequestBody RoleAuthUpdateDTO roleAuthUpdateDTO) {
        RoleDTO role = roleService.findById(roleAuthUpdateDTO.getRoleId());
        if (Objects.isNull(role)) {
            throw new BadCredentialsException("请选择角色信息");
        }
        authCodeService.updateRoleAuth(roleAuthUpdateDTO);
        return new DataResponseBody();
    }

    @ApiOperation("删除角色")
    @DeleteMapping
    @PreAuthorize(Permissions.ROLE_DELETE)
    public DataResponseBody delete(@RequestBody RoleDeleteDTO deleteDTO) {
        roleService.delete(deleteDTO.getIds());
        return new DataResponseBody();
    }

    @ApiOperation("导出角色数据")
    @GetMapping(value = "/download")
    @PreAuthorize(Permissions.ROLE_DWONLOAD)
    public void download(HttpServletResponse response, RoleQueryDTO criteria) throws IOException {
        roleService.download(roleService.queryAll(criteria), response);
    }

}
