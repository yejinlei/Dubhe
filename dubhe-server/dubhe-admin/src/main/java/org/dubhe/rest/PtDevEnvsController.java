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
import org.dubhe.domain.PtDevEnvs;
import org.dubhe.domain.dto.PtDevEnvsQueryCriteria;
import org.dubhe.service.PtDevEnvsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description  devEnvs管理
 * @date 2020-03-17
 */
@Api(tags = "devEnvs管理")
@ApiIgnore
@RestController
@RequestMapping("/api/{version}/pt_ev_envs")
public class PtDevEnvsController {

    private final PtDevEnvsService ptDevEnvsService;

    public PtDevEnvsController(PtDevEnvsService ptDevEnvsService) {
        this.ptDevEnvsService = ptDevEnvsService;
    }

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('ptDevEnvs:list')")
    public void download(HttpServletResponse response, PtDevEnvsQueryCriteria criteria) throws IOException {
        ptDevEnvsService.download(ptDevEnvsService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("查询devEnvs")
    @PreAuthorize("@el.check('ptDevEnvs:list')")
    public DataResponseBody getPtDevEnvss(PtDevEnvsQueryCriteria criteria, Page page) {
        return new DataResponseBody(ptDevEnvsService.queryAll(criteria, page));
    }

    @PostMapping
    @ApiOperation("新增devEnvs")
    @PreAuthorize("@el.check('ptDevEnvs:add')")
    public DataResponseBody create(@Validated @RequestBody PtDevEnvs resources) {
        return new DataResponseBody(ptDevEnvsService.create(resources));
    }

    @PutMapping
    @ApiOperation("修改devEnvs")
    @PreAuthorize("@el.check('ptDevEnvs:edit')")
    public DataResponseBody update(@Validated @RequestBody PtDevEnvs resources) {
        ptDevEnvsService.update(resources);
        return new DataResponseBody();
    }

    @ApiOperation("删除devEnvs")
    @PreAuthorize("@el.check('ptDevEnvs:del')")
    @DeleteMapping
    public DataResponseBody deleteAll(@RequestBody Long[] ids) {
        ptDevEnvsService.deleteAll(ids);
        return new DataResponseBody();
    }
}
