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

package org.dubhe.data.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.constant.DatasetLabelEnum;
import org.dubhe.data.domain.dto.LabelDeleteDTO;
import org.dubhe.data.domain.dto.LabelUpdateDTO;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.DatasetService;
import org.dubhe.data.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody create(@RequestBody Label label, @PathVariable(name = "datasetId") Long datasetId) {
        datasetService.saveLabel(label, datasetId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签查询")
    @GetMapping(value = "/{datasetId}/labels")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody query(@PathVariable(name = "datasetId") Long datasetId) {
        return new DataResponseBody(labelService.list(datasetId));
    }

    @ApiOperation(value = "根据类型获取预置标签集合")
    @GetMapping(value = "/labels/auto/{labelGroupType}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody listSupportAutoByType(@PathVariable(value = "labelGroupType") Integer labelGroupType) {
        return new DataResponseBody(labelService.listSupportAutoByType(labelGroupType));
    }

    @ApiOperation(value = "获取预置标签类型")
    @GetMapping(value = "/presetLabels")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody getPresetLabels() {
        return new DataResponseBody(DatasetLabelEnum.getPresetLabels());
    }

    @ApiOperation(value = "获取coco预置标签")
    @GetMapping(value = "/pubLabels")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody getPubLabels(Integer labelGroupType) {
        return new DataResponseBody(labelService.getPubLabels(labelGroupType));
    }

    @ApiOperation(value = "根据标签组类型获取标签数据")
    @GetMapping(value = "/labels/{labelGroupType}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody query(@PathVariable(name = "labelGroupType") Integer labelGroupType) {
        return new DataResponseBody(labelService.findByLabelGroupType(labelGroupType));
    }

    @ApiOperation(value = "删除标签")
    @DeleteMapping(value = "/labels")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody delete(@Validated @RequestBody LabelDeleteDTO labelDeleteDTO) {
        labelService.delete(labelDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签修改")
    @PutMapping(value = "/labels")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody update(@Validated @RequestBody LabelUpdateDTO labelUpdateDTO) {
        return new DataResponseBody(labelService.update(labelUpdateDTO));
    }


}
