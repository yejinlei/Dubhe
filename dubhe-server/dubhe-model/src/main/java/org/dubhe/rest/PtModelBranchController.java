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

package org.dubhe.rest;

import org.dubhe.base.DataResponseBody;
import org.dubhe.domain.dto.PtModelBranchCreateDTO;
import org.dubhe.domain.dto.PtModelBranchDeleteDTO;
import org.dubhe.domain.dto.PtModelBranchQueryDTO;
import org.dubhe.service.PtModelBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
@Api(tags = "模型管理：版本管理")
@RestController
@RequestMapping("/api/ptModelBranch")
public class PtModelBranchController {

    @Autowired
    private PtModelBranchService ptModelBranchService;

    @GetMapping
    @ApiOperation("查询版本")
    public DataResponseBody getPtModelBranchs(@Validated PtModelBranchQueryDTO ptModelBranchQueryDTO) {
        return new DataResponseBody(ptModelBranchService.queryAll(ptModelBranchQueryDTO));
    }

    @PostMapping
    @ApiOperation("新增版本")
    public DataResponseBody create(@Validated @RequestBody PtModelBranchCreateDTO ptModelBranchCreateDTO) {
        return new DataResponseBody(ptModelBranchService.create(ptModelBranchCreateDTO));
    }

    @ApiOperation("删除版本")
    @DeleteMapping
    public DataResponseBody deleteAll(@Validated @RequestBody PtModelBranchDeleteDTO ptModelBranchDeleteDTO) {
        return new DataResponseBody(ptModelBranchService.deleteAll(ptModelBranchDeleteDTO));
    }



}
