/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.rest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.base.DataResponseBody;
import org.dubhe.domain.PtStorage;
import org.dubhe.domain.dto.PtStorageQueryCriteria;
import org.dubhe.service.PtStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description  storage管理
 * @date 2020-03-17
 */
@Api(tags = "storage管理")
@ApiIgnore
@RestController
@RequestMapping("/api/{version}/pt_storage")
public class PtStorageController {

    private final PtStorageService ptStorageService;

    public PtStorageController(PtStorageService ptStorageService) {
        this.ptStorageService = ptStorageService;
    }

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('ptStorage:list')")
    public void download(HttpServletResponse response, PtStorageQueryCriteria criteria) throws IOException {
        ptStorageService.download(ptStorageService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("查询storage")
    @PreAuthorize("@el.check('ptStorage:list')")
    public DataResponseBody getPtStorages(PtStorageQueryCriteria criteria, Page page) {
        return new DataResponseBody(ptStorageService.queryAll(criteria, page));
    }

    @PostMapping
    @ApiOperation("新增storage")
    @PreAuthorize("@el.check('ptStorage:add')")
    public DataResponseBody create(@Validated @RequestBody PtStorage resources) {
        return new DataResponseBody(ptStorageService.create(resources));
    }

    @PutMapping
    @ApiOperation("修改storage")
    @PreAuthorize("@el.check('ptStorage:edit')")
    public DataResponseBody update(@Validated @RequestBody PtStorage resources) {
        ptStorageService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("删除storage")
    @PreAuthorize("@el.check('ptStorage:del')")
    @DeleteMapping
    public DataResponseBody deleteAll(@RequestBody Long[] ids) {
        ptStorageService.deleteAll(ids);
        return new DataResponseBody();
    }
}
