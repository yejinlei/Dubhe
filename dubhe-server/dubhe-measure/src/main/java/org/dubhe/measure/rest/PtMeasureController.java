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
package org.dubhe.measure.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.measure.domain.dto.PtMeasureCreateDTO;
import org.dubhe.measure.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.measure.domain.dto.PtMeasureQueryDTO;
import org.dubhe.measure.domain.dto.PtMeasureUpdateDTO;
import org.dubhe.measure.service.PtMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 度量管理
 * @date 2020-11-16
 */
@Api(tags = "度量：度量管理")
@RestController
@RequestMapping("/ptMeasure")
public class PtMeasureController {

    @Autowired
    private PtMeasureService ptMeasureService;

    @GetMapping
    @ApiOperation("查询度量")
    @PreAuthorize(Permissions.MEASURE)
    public DataResponseBody getMeasure(PtMeasureQueryDTO ptMeasureQueryDTO) {
        return new DataResponseBody(ptMeasureService.getMeasure(ptMeasureQueryDTO));
    }

    @PostMapping
    @ApiOperation("新建度量")
    @PreAuthorize(Permissions.MEASURE_CREATE)
    public DataResponseBody addMeasure(@Validated @RequestBody PtMeasureCreateDTO ptMeasureCreateDTO) {
        ptMeasureService.createMeasure(ptMeasureCreateDTO);
        return new DataResponseBody();
    }

    @PutMapping
    @ApiOperation("修改度量")
    @PreAuthorize(Permissions.MEASURE_EDIT)
    public DataResponseBody updateMeasure(@Validated @RequestBody PtMeasureUpdateDTO ptMeasureUpdateDTO) {
        ptMeasureService.updateMeasure(ptMeasureUpdateDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除度量")
    @PreAuthorize(Permissions.MEASURE_DELETE)
    public DataResponseBody deleteMeasure(@Validated @RequestBody PtMeasureDeleteDTO ptMeasureDeleteDTO) {
        ptMeasureService.deleteMeasure(ptMeasureDeleteDTO);
        return new DataResponseBody();
    }

    @GetMapping("/byName")
    @ApiOperation("通过度量名称查询")
    public DataResponseBody getMeasureByName(@RequestParam String name) {
        return new DataResponseBody(ptMeasureService.getMeasureByName(name));
    }

}
