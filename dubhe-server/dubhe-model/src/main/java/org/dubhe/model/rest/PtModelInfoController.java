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

package org.dubhe.model.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.PtModelInfoConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelInfoQueryByIdDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.domain.vo.PtModelInfoCreateVO;
import org.dubhe.model.service.PtModelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
@Api(tags = "模型管理：模型管理")
@RestController
@RequestMapping("/ptModelInfo")
public class PtModelInfoController {

    @Autowired
    private PtModelInfoService ptModelInfoService;

    @GetMapping
    @ApiOperation("查询模型")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody getPtModelInfos(@Validated PtModelInfoQueryDTO ptModelInfoQueryDTO) {
        return new DataResponseBody(ptModelInfoService.queryAll(ptModelInfoQueryDTO));
    }

    @GetMapping("/byResource")
    @ApiOperation("根据类型来获取模型")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody getPtModelInfosByResource(@Validated PtModelInfoByResourceDTO ptModelInfoByResourceDTO) {
        return new DataResponseBody(ptModelInfoService.getModelByResource(ptModelInfoByResourceDTO));
    }

    @GetMapping("/byModelId")
    @ApiOperation("根据模型id查询模型详情(训练远程调用)")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody<PtModelInfoQueryVO> getByModelId(@Validated PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO) {
        return new DataResponseBody(ptModelInfoService.queryByModelId(ptModelInfoQueryByIdDTO));
    }

    @GetMapping("/conditionQuery")
    @ApiOperation("根据模型条件查询模型详情列表(训练远程调用)")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody<List<PtModelInfoQueryVO>> getConditionQuery(@Validated PtModelInfoConditionQueryDTO ptModelInfoConditionQueryDTO) {
        return new DataResponseBody(ptModelInfoService.getConditionQuery(ptModelInfoConditionQueryDTO));
    }

    @PostMapping
    @ApiOperation("新增模型")
    @PreAuthorize(Permissions.MODEL_MODEL_CREATE)
    public DataResponseBody create(@Validated @RequestBody PtModelInfoCreateDTO ptModelInfoCreateDTO) {
        PtModelInfoCreateVO ptModelInfoCreateVO = ptModelInfoService.create(ptModelInfoCreateDTO);
        return new DataResponseBody(ptModelInfoCreateVO);
    }

    @PutMapping
    @ApiOperation("修改模型")
    @PreAuthorize(Permissions.MODEL_MODEL_EDIT)
    public DataResponseBody update(@Validated @RequestBody PtModelInfoUpdateDTO ptModelInfoUpdateDTO) {
        return new DataResponseBody(ptModelInfoService.update(ptModelInfoUpdateDTO));
    }

    @PostMapping("package")
    @ApiOperation("炼知模型打包")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody packageAtlasModel(@Validated @RequestBody PtModelInfoPackageDTO ptModelInfoPackageDTO) {
        return new DataResponseBody(ptModelInfoService.packageAtlasModel(ptModelInfoPackageDTO));
    }

    @DeleteMapping
    @ApiOperation("删除模型")
    @PreAuthorize(Permissions.MODEL_MODEL_DELETE)
    public DataResponseBody deleteAll(@Validated @RequestBody PtModelInfoDeleteDTO ptModelInfoDeleteDTO) {
        return new DataResponseBody(ptModelInfoService.deleteAll(ptModelInfoDeleteDTO));
    }

    @PostMapping("/uploadModel")
    @ApiOperation("模型优化上传模型")
    @PreAuthorize(Permissions.MODEL_MODEL_CREATE)
    public DataResponseBody modelOptimizationUploadModel(@Validated @RequestBody PtModelOptimizationCreateDTO ptModelOptimizationCreateDTO) {
        return new DataResponseBody(ptModelInfoService.modelOptimizationUploadModel(ptModelOptimizationCreateDTO));
    }

    @GetMapping("/servingModel")
    @ApiOperation("查询能提供服务的模型")
    @PreAuthorize(Permissions.MODEL_MODEL)
    public DataResponseBody getServingModel(@Validated ServingModelDTO servingModelDTO) {
        return new DataResponseBody(ptModelInfoService.getServingModel(servingModelDTO));
    }
}
