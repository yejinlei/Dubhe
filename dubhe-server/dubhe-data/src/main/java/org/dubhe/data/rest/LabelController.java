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

package org.dubhe.data.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.domain.dto.LabelCreateDTO;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.dubhe.constant.Permissions.DATA;

/**
 * @description 标签管理
 * @date 2020-04-10
 */
@Api(tags = "数据处理：标签管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets")
public class LabelController {

    @Autowired
    private LabelService labelService;
    @Autowired
    private DatasetService datasetService;

    @ApiOperation(value = "标签创建")
    @PostMapping(value = "/{datasetId}/labels")
    @RequiresPermissions(DATA)
    public DataResponseBody create(@RequestBody Label label, @PathVariable(name = "datasetId") Long datasetId) {
        datasetService.saveLabel(label, datasetId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签查询")
    @GetMapping(value = "/{datasetId}/labels")
    @RequiresPermissions(DATA)
    public DataResponseBody query(@PathVariable(name = "datasetId") Long datasetId) {
        return new DataResponseBody(labelService.list(datasetId));
    }

    @ApiOperation(value = "支持自动标注的标签查询")
    @GetMapping(value = "/labels/auto")
    @RequiresPermissions(DATA)
    public DataResponseBody query() {
        return new DataResponseBody(labelService.listSupportAuto());
    }

    @ApiOperation(value = "获取预置标签类型")
    @GetMapping(value = "/presetLabels")
    @RequiresPermissions(DATA)
    public DataResponseBody getPresetLabels() {
        return new DataResponseBody(DatasetLabelEnum.getPresetLabels());
    }

    @ApiOperation(value = "标签修改")
    @PutMapping(value = "/labels/{labelId}")
    @RequiresPermissions(DATA)
    public DataResponseBody update(@PathVariable(name = "labelId") Long labelId,
                                  @Validated @RequestBody LabelCreateDTO labelCreateDto) {
        return new DataResponseBody(labelService.update(labelCreateDto, labelId));
    }

    @ApiOperation(value = "获取coco预置标签")
    @GetMapping(value = "/pubLabels")
    @RequiresPermissions(DATA)
    public DataResponseBody getPubLabels() {
        return new DataResponseBody(labelService.getPubLabels());
    }


}
