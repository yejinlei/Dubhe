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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.base.DataResponseBody;
import org.dubhe.domain.dto.*;
import org.dubhe.domain.vo.PtModelInfoCreateVO;
import org.dubhe.service.PtModelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
@Api(tags = "模型管理：模型管理")
@RestController
@RequestMapping("/api/ptModelInfo")
public class PtModelInfoController {

    @Autowired
    private PtModelInfoService ptModelInfoService;

    @GetMapping
    @ApiOperation("查询模型")
    public DataResponseBody getPtModelInfos(PtModelInfoQueryDTO ptModelInfoQueryDTO) {
        return new DataResponseBody(ptModelInfoService.queryAll(ptModelInfoQueryDTO));
    }

    @GetMapping("/byResource")
    @ApiOperation("根据类型来获取模型")
    public DataResponseBody getPtModelInfosByResource(PtModelInfoByResourceDTO ptModelInfoByResourceDTO) {
        return new DataResponseBody(ptModelInfoService.getModelByResource(ptModelInfoByResourceDTO));
    }

    @PostMapping
    @ApiOperation("新增模型")
    public DataResponseBody create(@Validated @RequestBody PtModelInfoCreateDTO ptModelInfoCreateDTO) {
        PtModelInfoCreateVO ptModelInfoCreateVO = ptModelInfoService.create(ptModelInfoCreateDTO);
        return new DataResponseBody(ptModelInfoCreateVO);
    }

    @PutMapping
    @ApiOperation("修改模型")
    public DataResponseBody update(@Validated @RequestBody PtModelInfoUpdateDTO ptModelInfoUpdateDTO) {
        return new DataResponseBody(ptModelInfoService.update(ptModelInfoUpdateDTO));
    }

    @ApiOperation("删除模型")
    @DeleteMapping
    public DataResponseBody deleteAll(@Validated @RequestBody PtModelInfoDeleteDTO ptModelInfoDeleteDTO) {
        return new DataResponseBody(ptModelInfoService.deleteAll(ptModelInfoDeleteDTO));
    }

    @PostMapping("/uploadModel")
    @ApiOperation("模型优化上传模型")
    public DataResponseBody modelOptimizationUploadModel(@Validated @RequestBody PtModelOptimizationCreateDTO ptModelOptimizationCreateDTO) {
        return new DataResponseBody(ptModelInfoService.modelOptimizationUploadModel(ptModelOptimizationCreateDTO));
    }

}
