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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.UserCreateDTO;
import org.dubhe.domain.dto.UserDeleteDTO;
import org.dubhe.domain.dto.UserQueryDTO;
import org.dubhe.domain.dto.UserUpdateDTO;
import org.dubhe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @Description :用户管理 控制器
 * @Date 2020-06-01
 */
@Api(tags = "系统：用户管理")
@RestController
@RequestMapping("/api/{version}/users")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("导出用户数据")
    @GetMapping(value = "/download")
    @RequiresPermissions(Permissions.SYSTEM_USER)
    public void download(HttpServletResponse response, UserQueryDTO criteria) throws IOException {
        userService.download(userService.queryAll(criteria), response);
    }

    @ApiOperation("查询用户")
    @GetMapping
    @RequiresPermissions(Permissions.SYSTEM_USER)
    public DataResponseBody getUsers(UserQueryDTO criteria, Page page) {
        return new DataResponseBody(userService.queryAll(criteria, page));
    }

    @ApiOperation("新增用户")
    @PostMapping
    @RequiresPermissions(Permissions.SYSTEM_USER)
    public DataResponseBody create(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return new DataResponseBody(userService.create(userCreateDTO));
    }

    @ApiOperation("修改用户")
    @PutMapping
    @RequiresPermissions(Permissions.SYSTEM_USER)
    public DataResponseBody update(@Valid @RequestBody UserUpdateDTO resources) {
        return new DataResponseBody(userService.update(resources));
    }

    @ApiOperation("删除用户")
    @DeleteMapping
    @RequiresPermissions(Permissions.SYSTEM_USER)
    public DataResponseBody delete(@Valid @RequestBody UserDeleteDTO userDeleteDTO) {
        userService.delete(userDeleteDTO.getIds());
        return new DataResponseBody();
    }




}
