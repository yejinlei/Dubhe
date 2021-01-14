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
import org.dubhe.dcm.domain.dto.*;
import org.dubhe.dcm.service.DataMedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.dubhe.constant.Permissions.DATA;

/**
 * @description 医学数据集管理
 * @date 2020-11-11
 */
@Api(tags = "医学数据处理：医学数据集管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets/medical")
public class DataMedicineController {

    @Autowired
    private DataMedicineService dataMedicineService;

    @ApiOperation(value = "导入医学数据集")
    @PostMapping(value = "/files")
    @RequiresPermissions(DATA)
    public DataResponseBody importDataMedicine(@Validated @RequestBody DataMedicineImportDTO dataMedicineImportDTO) {
        return new DataResponseBody(dataMedicineService.importDataMedicine(dataMedicineImportDTO));
    }

    @ApiOperation(value = "医学数据集创建")
    @PostMapping
    @RequiresPermissions(DATA)
    public DataResponseBody create(@Validated(DataMedicineCreateDTO.Create.class) @RequestBody DataMedicineCreateDTO dataMedicineCreateDTO) {
        return new DataResponseBody(dataMedicineService.create(dataMedicineCreateDTO));
    }

    @ApiOperation(value = "医学数据集查询")
    @GetMapping
    @RequiresPermissions(DATA)
    public DataResponseBody query(DataMedicineQueryDTO dataMedicineQueryDTO) {
        return new DataResponseBody(dataMedicineService.listVO(dataMedicineQueryDTO));
    }

    @ApiOperation(value = "医学数据集删除")
    @DeleteMapping
    @RequiresPermissions(DATA)
    public DataResponseBody delete(@Validated @RequestBody DataMedicineDeleteDTO dataMedicineDeleteDTO) {
        return new DataResponseBody(dataMedicineService.delete(dataMedicineDeleteDTO));
    }

    @ApiOperation(value = "数据集修改")
    @PutMapping(value = "/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody update(@PathVariable(name = "medicalId") Long medicineId,
                                   @Validated @RequestBody DataMedcineUpdateDTO dataMedcineUpdateDTO) {
        return new DataResponseBody(dataMedicineService.update(dataMedcineUpdateDTO, medicineId));
    }

    @ApiOperation(value = "医学数据集详情")
    @GetMapping("/detail/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody get(@PathVariable(name = "medicalId") Long medicalId) {
        return new DataResponseBody(dataMedicineService.get(medicalId));
    }

    @ApiOperation(value = "获取完成标注文件")
    @GetMapping("/getFinished/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody getFinished(@PathVariable(name = "medicalId") Long medicalId) {
        return new DataResponseBody(dataMedicineService.getFinished(medicalId));
    }

    @ApiOperation(value = "获取自动标注文件")
    @GetMapping("/getAuto/{medicalId}")
    @RequiresPermissions(DATA)
    public DataResponseBody getAuto(@PathVariable(name = "medicalId") Long medicalId) {
        return new DataResponseBody(dataMedicineService.getAuto(medicalId));
    }
}
