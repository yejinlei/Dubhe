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
import org.dubhe.domain.dto.DictDetailCreateDTO;
import org.dubhe.domain.dto.DictDetailDeleteDTO;
import org.dubhe.domain.dto.DictDetailQueryDTO;
import org.dubhe.domain.dto.DictDetailUpdateDTO;
import org.dubhe.service.DictDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Description 字典详情管理 控制器
 * @Date 2020-06-01
 */
@Api(tags = "系统：字典详情管理")
@RestController
@RequestMapping("/api/{version}/dictDetail")
public class DictDetailController {

    private static final String ENTITY_NAME = "dictDetail";
    @Autowired
    private DictDetailService dictDetailService;

    @ApiOperation("查询字典详情")
    @GetMapping
    @RequiresPermissions(Permissions.SYSTEM_DICT)
    public DataResponseBody getDictDetails(DictDetailQueryDTO resources, Page page) {
        return new DataResponseBody(dictDetailService.queryAll(resources, page));
    }


    @ApiOperation("新增字典详情")
    @PostMapping
    @RequiresPermissions(Permissions.SYSTEM_DICT)
    public DataResponseBody create(@Valid @RequestBody DictDetailCreateDTO resources) {
        return new DataResponseBody(dictDetailService.create(resources));
    }


    @ApiOperation("修改字典详情")
    @PutMapping
    @RequiresPermissions(Permissions.SYSTEM_DICT)
    public DataResponseBody update(@Valid @RequestBody DictDetailUpdateDTO resources) {
        dictDetailService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("删除字典详情")
    @DeleteMapping
    @RequiresPermissions(Permissions.SYSTEM_DICT)
    public DataResponseBody delete(@Valid @RequestBody DictDetailDeleteDTO dto) {
        dictDetailService.delete(dto.getIds());
        return new DataResponseBody();
    }
}
