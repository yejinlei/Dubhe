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
import org.dubhe.biz.base.dto.PtModelBranchConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelBranchQueryByIdDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.PtModelBranchQueryVO;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.service.PtModelBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
@Api(tags = "模型管理：版本管理")
@RestController
@RequestMapping("/ptModelBranch")
public class PtModelBranchController {

    @Autowired
    private PtModelBranchService ptModelBranchService;

    @GetMapping
    @ApiOperation("查询版本")
    @PreAuthorize(Permissions.MODEL_BRANCH)
    public DataResponseBody getPtModelBranches(@Validated PtModelBranchQueryDTO ptModelBranchQueryDTO) {
        return new DataResponseBody(ptModelBranchService.queryAll(ptModelBranchQueryDTO));
    }

    @GetMapping("/byBranchId")
    @ApiOperation("根据模型版本id查询模型版本详情")
    @PreAuthorize(Permissions.MODEL_BRANCH)
    public DataResponseBody<PtModelBranchQueryVO> getByBranchId(@Validated PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO) {
        return new DataResponseBody(ptModelBranchService.queryByBranchId(ptModelBranchQueryByIdDTO));
    }

    @PostMapping
    @ApiOperation("新增版本")
    @PreAuthorize(Permissions.MODEL_BRANCH_CREATE)
    public DataResponseBody create(@Validated @RequestBody PtModelBranchCreateDTO ptModelBranchCreateDTO) {
        return new DataResponseBody(ptModelBranchService.create(ptModelBranchCreateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除版本")
    @PreAuthorize(Permissions.MODEL_BRANCH_DELETE)
    public DataResponseBody deleteAll(@Validated @RequestBody PtModelBranchDeleteDTO ptModelBranchDeleteDTO) {
        return new DataResponseBody(ptModelBranchService.deleteAll(ptModelBranchDeleteDTO));
    }

    @GetMapping("/conditionQuery")
    @ApiOperation("条件查询")
    @PreAuthorize(Permissions.MODEL_BRANCH)
    public DataResponseBody getConditionQuery(@Validated PtModelBranchConditionQueryDTO ptModelBranchConditionQueryDTO) {
        return new DataResponseBody(ptModelBranchService.getConditionQuery(ptModelBranchConditionQueryDTO));
    }

    @ApiOperation(value = "我的模型转预置模型")
    @PostMapping(value = "/convertPreset")
    @PreAuthorize(Permissions.MODEL_BRANCH_CONVERT_PRESET)
    public DataResponseBody convertPreset(@Validated @RequestBody ModelConvertPresetDTO modelConvertPresetDTO) {
        ptModelBranchService.convertPreset(modelConvertPresetDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "Tensorflow SavedModel 模型转换为 ONNX 模型")
    @PostMapping(value = "/convertOnnx")
    @PreAuthorize(Permissions.MODEL_BRANCH_CONVERT_ONNX)
    public DataResponseBody convertOnnx(@Validated @RequestBody PtModelConvertOnnxDTO ptModelConvertOnnxDTO) {
        return new DataResponseBody(ptModelBranchService.convertToOnnx(ptModelConvertOnnxDTO));
    }
}
