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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.domain.dto.DictCreateDTO;
import org.dubhe.admin.domain.dto.DictDeleteDTO;
import org.dubhe.admin.domain.dto.DictQueryDTO;
import org.dubhe.admin.domain.dto.DictUpdateDTO;
import org.dubhe.admin.service.DictService;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * @description 字典管理 控制器
 * @date 2020-06-01
 */
@Api(tags = "系统：字典管理")
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;


    @ApiOperation("导出字典数据")
    @GetMapping(value = "/download")
    @PreAuthorize(Permissions.DICT_DOWNLOAD)
    public void download(HttpServletResponse response, DictQueryDTO criteria) throws IOException {
        dictService.download(dictService.queryAll(criteria), response);
    }

    @ApiOperation("查询字典")
    @GetMapping(value = "/all")
    @PreAuthorize(Permissions.DICT)
    public DataResponseBody all() {
        return new DataResponseBody(dictService.queryAll(new DictQueryDTO()));
    }

    @ApiOperation("查询字典")
    @GetMapping
    @PreAuthorize(Permissions.DICT)
    public DataResponseBody getDicts(DictQueryDTO resources, Page page) {
        return new DataResponseBody(dictService.queryAll(resources, page));
    }

    @ApiOperation("新增字典")
    @PostMapping
    @PreAuthorize(Permissions.DICT_CREATE)
    public DataResponseBody create(@Valid @RequestBody DictCreateDTO resources) {
        return new DataResponseBody(dictService.create(resources));
    }

    @ApiOperation("修改字典")
    @PutMapping
    @PreAuthorize(Permissions.DICT_EDIT)
    public DataResponseBody update(@Valid @RequestBody DictUpdateDTO resources) {
        dictService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("批量删除字典")
    @DeleteMapping
    @PreAuthorize(Permissions.DICT_DELETE)
    public DataResponseBody delete(@RequestBody DictDeleteDTO dto) {
        dictService.deleteAll(dto.getIds());
        return new DataResponseBody();
    }

    @ApiOperation("根据名称查询字典详情")
    @GetMapping(value = "/{name}")
    public DataResponseBody getDict(@PathVariable String name) {
        return DataResponseFactory.success(dictService.findByName(name));
    }
}
