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
import org.dubhe.admin.domain.dto.AuthCodeCreateDTO;
import org.dubhe.admin.domain.dto.AuthCodeDeleteDTO;
import org.dubhe.admin.domain.dto.AuthCodeQueryDTO;
import org.dubhe.admin.domain.dto.AuthCodeUpdateDTO;
import org.dubhe.admin.service.AuthCodeService;
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
 * @description
 * @date 2021-05-17
 */
@Api(tags = "系统：权限管理")
@RestController
@RequestMapping("/authCode")
public class AuthCodeController {

    @Autowired
    private AuthCodeService authCodeService;

    @GetMapping
    @ApiOperation("获取权限组列表")
    @PreAuthorize(Permissions.AUTH_CODE)
    public DataResponseBody queryAll(AuthCodeQueryDTO authCodeQueryDTO) {
        return new DataResponseBody(authCodeService.queryAll(authCodeQueryDTO));
    }

    @PostMapping
    @ApiOperation("创建权限组")
    @PreAuthorize(Permissions.AUTH_CODE_CREATE)
    public DataResponseBody create(@Validated @RequestBody AuthCodeCreateDTO authCodeCreateDTO) {
        authCodeService.create(authCodeCreateDTO);
        return new DataResponseBody();
    }

    @PutMapping()
    @ApiOperation("修改权限组")
    @PreAuthorize(Permissions.AUTH_CODE_EDIT)
    public DataResponseBody update(@Validated @RequestBody AuthCodeUpdateDTO authCodeUpdateDTO) {
        authCodeService.update(authCodeUpdateDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除权限组")
    @PreAuthorize(Permissions.AUTH_CODE_DELETE)
    public DataResponseBody delete(@RequestBody AuthCodeDeleteDTO authCodeDeleteDTO) {
        authCodeService.delete(authCodeDeleteDTO.getIds());
        return new DataResponseBody();
    }

    @GetMapping("list")
    @ApiOperation("获取权限组tree")
    @PreAuthorize(Permissions.AUTH_CODE)
    public DataResponseBody getAuthCodeList() {
        return new DataResponseBody(authCodeService.getAuthCodeList());
    }
}
