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
package org.dubhe.dcm.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.dcm.domain.dto.DataLesionSliceDeleteDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceUpdateDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceCreateDTO;
import org.dubhe.dcm.service.DataLesionSliceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.dubhe.constant.Permissions.DATA;

/**
 * @description 病灶信息管理
 * @date 2020-12-23
 */
@Api(tags = "医学数据处理：病灶信息管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets/medical/lesion")
public class DataLesionSliceController {

    @Autowired
    private DataLesionSliceService dataLesionSliceService;

    @ApiOperation(value = "病灶信息保存")
    @PostMapping("/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody save(@Validated @RequestBody List<DataLesionSliceCreateDTO> dataLesionSliceCreateDTOS
            , @PathVariable(name = "medicalId") Long medicineId) {
        return new DataResponseBody(dataLesionSliceService.save(dataLesionSliceCreateDTOS,medicineId));
    }

    @ApiOperation(value = "病灶信息查询")
    @GetMapping("/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody query(@PathVariable(name = "medicalId") Long medicineId) {
        return new DataResponseBody(dataLesionSliceService.get(medicineId));
    }

    @ApiOperation(value = "病灶信息删除")
    @DeleteMapping
    @RequiresPermissions(DATA)
    public DataResponseBody delete(@Validated @RequestBody DataLesionSliceDeleteDTO dataLesionSliceDeleteDTO) {
        return new DataResponseBody(dataLesionSliceService.delete(dataLesionSliceDeleteDTO));
    }

    @ApiOperation(value = "病灶信息修改")
    @PutMapping
    @RequiresPermissions(DATA)
    public DataResponseBody update(@Validated @RequestBody DataLesionSliceUpdateDTO dataLesionSliceUpdateDTO) {
        return new DataResponseBody(dataLesionSliceService.update(dataLesionSliceUpdateDTO));
    }
}
