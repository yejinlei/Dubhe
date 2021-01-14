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
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.domain.dto.PtMeasureDTO;
import org.dubhe.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.domain.dto.PtMeasureQueryDTO;
import org.dubhe.domain.dto.PtMeasureUpdateDTO;
import org.dubhe.service.PtMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 度量管理
 * @date 2020-11-16
 */
@Api(tags = "度量：度量管理")
@RestController
@ApiVersion(1)
@RequestMapping("/api/{version}/ptMeasure")
public class PtMeasureController {

    @Autowired
    private PtMeasureService ptMeasureService;

    @GetMapping
    @ApiOperation("查询度量")
    public DataResponseBody getMeasure(PtMeasureQueryDTO ptMeasureQueryDTO) {
        return new DataResponseBody(ptMeasureService.getMeasure(ptMeasureQueryDTO));
    }

    @PostMapping
    @ApiOperation("新建度量")
    public DataResponseBody addMeasure(@Validated @RequestBody PtMeasureDTO ptMeasureCreateDTO) {
        ptMeasureService.createMeasure(ptMeasureCreateDTO);
        return new DataResponseBody();
    }

    @PutMapping
    @ApiOperation("修改度量")
    public DataResponseBody updateMeasure(@Validated @RequestBody PtMeasureUpdateDTO ptMeasureUpdateDTO) {
        ptMeasureService.updateMeasure(ptMeasureUpdateDTO);
        return new DataResponseBody();
    }

    @DeleteMapping
    @ApiOperation("删除度量")
    public DataResponseBody deleteMeasure(@Validated @RequestBody PtMeasureDeleteDTO PtMeasureUpdateDTO) {
        ptMeasureService.deleteMeasure(PtMeasureUpdateDTO);
        return new DataResponseBody();
    }

    @GetMapping("/byName")
    @ApiOperation("通过度量名称查询")
    public DataResponseBody getMeasureByName(@RequestParam String name) {
        return new DataResponseBody(ptMeasureService.getMeasureByName(name));
    }

}
