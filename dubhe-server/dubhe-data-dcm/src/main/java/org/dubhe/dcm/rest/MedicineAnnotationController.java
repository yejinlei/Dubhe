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
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.dcm.constant.DcmConstant;
import org.dubhe.dcm.domain.dto.MedicineAnnotationDTO;
import org.dubhe.dcm.domain.dto.MedicineAutoAnnotationDTO;
import org.dubhe.dcm.service.MedicineAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @description 医学标注管理
 * @date 2020-11-16
 */
@Api(tags = "医学数据处理：标注")
@RestController
@RequestMapping(DcmConstant.MODULE_URL_PREFIX + "/datasets/medical/annotation")
public class MedicineAnnotationController {

    @Autowired
    private MedicineAnnotationService medicalAnnotationService;

    @ApiOperation(value = "医学数据自动标注")
    @PostMapping("/auto")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody auto(@Validated @RequestBody MedicineAutoAnnotationDTO medicineAutoAnnotationDTO) {
        medicalAnnotationService.auto(medicineAutoAnnotationDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标注保存")
    @PostMapping(value = "/save")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody save(@Validated @RequestBody MedicineAnnotationDTO medicineAnnotationDTO) {
        return new DataResponseBody(medicalAnnotationService.save(medicineAnnotationDTO));
    }

    @ApiOperation(value = "标注进度")
    @GetMapping(value = "/schedule")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody schedule(@RequestParam(value = "ids") List<Long> ids) {
        return new DataResponseBody(medicalAnnotationService.schedule(ids));
    }

}
