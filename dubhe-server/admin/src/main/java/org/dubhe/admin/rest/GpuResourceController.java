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
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.service.GpuResourceService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description GPU资源管理
 * @date 2021-08-20
 */
@Api(tags = "系统:GPU资源管理")
@RestController
@RequestMapping("/gpuResource")
public class GpuResourceController {

    @Autowired
    private GpuResourceService gpuResourceService;

    @ApiOperation("查询GPU资源")
    @GetMapping
    public DataResponseBody getGpuResource(GpuResourceQueryDTO gpuResourceQueryDTO) {
        return new DataResponseBody(gpuResourceService.getGpuResource(gpuResourceQueryDTO));
    }

    @ApiOperation("查询用户GPU类型")
    @GetMapping("/getUserGpuType")
    public DataResponseBody getUserGpuType() {
        return new DataResponseBody(gpuResourceService.getUserGpuType());
    }

    @ApiOperation("根据用户GPU类型查询用户GPU资源")
    @GetMapping("/getUserGpuModel")
    public DataResponseBody getUserGpuResource(UserGpuResourceQueryDTO userGpuResourceQueryDTO) {
        return new DataResponseBody(gpuResourceService.getUserGpuResource(userGpuResourceQueryDTO));
    }

    @ApiOperation("查询GPU类型")
    @GetMapping("/getGpuType")
    public DataResponseBody getGpuType() {
        return new DataResponseBody(gpuResourceService.getGpuType());
    }

    @ApiOperation("新增GPU资源")
    @PostMapping
    @PreAuthorize(Permissions.GPU_CREATE)
    public DataResponseBody create(@Valid @RequestBody GpuResourceCreateDTO gpuResourceCreateDTO) {
        return new DataResponseBody(gpuResourceService.create(gpuResourceCreateDTO));
    }

    @ApiOperation("修改GPU资源")
    @PutMapping
    @PreAuthorize(Permissions.GPU_EDIT)
    public DataResponseBody update(@Valid @RequestBody GpuResourceUpdateDTO gpuResourceUpdateDTO) {
        return new DataResponseBody(gpuResourceService.update(gpuResourceUpdateDTO));
    }

    @ApiOperation("删除GPU资源")
    @DeleteMapping
    @PreAuthorize(Permissions.GPU_DELETE)
    public DataResponseBody delete(@Valid @RequestBody GpuResourceDeleteDTO gpuResourceDeleteDTO) {
        gpuResourceService.delete(gpuResourceDeleteDTO);
        return new DataResponseBody();
    }

}