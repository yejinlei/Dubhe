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
import org.dubhe.admin.domain.dto.ResourceSpecsCreateDTO;
import org.dubhe.admin.domain.dto.ResourceSpecsDeleteDTO;
import org.dubhe.admin.domain.dto.ResourceSpecsQueryDTO;
import org.dubhe.admin.domain.dto.ResourceSpecsUpdateDTO;
import org.dubhe.admin.service.ResourceSpecsService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.QueryResourceSpecsDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description CPU, GPU, 内存等资源规格管理
 * @date 2021-05-27
 */
@Api(tags = "系统:资源规格管理")
@RestController
@RequestMapping("/resourceSpecs")
public class ResourceSpecsController {

    @Autowired
    private ResourceSpecsService resourceSpecsService;

    @ApiOperation("查询资源规格")
    @GetMapping
    public DataResponseBody getResourceSpecs(@Validated ResourceSpecsQueryDTO resourceSpecsQueryDTO) {
        return new DataResponseBody(resourceSpecsService.getResourceSpecs(resourceSpecsQueryDTO));
    }

    @ApiOperation("查询资源规格(训练远程调用)")
    @GetMapping("/queryResourceSpecs")
    public DataResponseBody<QueryResourceSpecsVO> queryResourceSpecs(@Validated QueryResourceSpecsDTO queryResourceSpecsDTO) {
        return new DataResponseBody(resourceSpecsService.queryResourceSpecs(queryResourceSpecsDTO));
    }


    @ApiOperation("查询资源规格(tadl远程调用)")
    @GetMapping("/queryTadlResourceSpecs")
    public DataResponseBody<QueryResourceSpecsVO> queryTadlResourceSpecs(Long id) {
        return new DataResponseBody(resourceSpecsService.queryTadlResourceSpecs(id));
    }

    @ApiOperation("新增资源规格")
    @PostMapping
    @PreAuthorize(Permissions.SPECS_CREATE)
    public DataResponseBody create(@Valid @RequestBody ResourceSpecsCreateDTO resourceSpecsCreateDTO) {
        return new DataResponseBody(resourceSpecsService.create(resourceSpecsCreateDTO));
    }

    @ApiOperation("修改资源规格")
    @PutMapping
    @PreAuthorize(Permissions.SPECS_EDIT)
    public DataResponseBody update(@Valid @RequestBody ResourceSpecsUpdateDTO resourceSpecsUpdateDTO) {
        return new DataResponseBody(resourceSpecsService.update(resourceSpecsUpdateDTO));
    }

    @ApiOperation("删除资源规格")
    @DeleteMapping
    @PreAuthorize(Permissions.SPECS_DELETE)
    public DataResponseBody delete(@Valid @RequestBody ResourceSpecsDeleteDTO resourceSpecsDeleteDTO) {
        resourceSpecsService.delete(resourceSpecsDeleteDTO);
        return new DataResponseBody();
    }

}