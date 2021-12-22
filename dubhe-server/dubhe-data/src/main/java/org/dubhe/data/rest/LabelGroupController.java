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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.dto.*;
import org.dubhe.data.domain.entity.LabelGroup;
import org.dubhe.data.domain.vo.LabelGroupQueryVO;
import org.dubhe.data.domain.vo.LabelGroupVO;
import org.dubhe.data.service.LabelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @description 标签组管理
 * @date 2020-09-22
 */
@Api(tags = "数据处理：标签组管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX)
public class LabelGroupController {

    @Autowired
    private LabelGroupService labelGroupService;

    @ApiOperation(value = "标签组创建")
    @PostMapping(value = "/labelGroup")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody create(@Validated @RequestBody LabelGroupCreateDTO labelGroupCreateDTO) {
        return new DataResponseBody(labelGroupService.creatLabelGroup(labelGroupCreateDTO));
    }

    @ApiOperation(value = "标签组分页列表")
    @GetMapping(value = "/labelGroup/query")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody query(Page page, LabelGroupQueryVO labelGroupQueryVO) {
        return new DataResponseBody(labelGroupService.listVO(page, labelGroupQueryVO));
    }

    @ApiOperation(value = "标签组详情")
    @GetMapping(value = "/labelGroup/{labelGroupId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody get(@PathVariable(name = "labelGroupId") Long labelGroupId) {
        LabelGroupVO labelGroupVO = labelGroupService.get(labelGroupId);
        return new DataResponseBody(labelGroupVO);
    }

    @ApiOperation(value = "标签组列表")
    @GetMapping(value = "/labelGroup/getList")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody query(@Validated LabelGroupQueryDTO labelGroupQueryDTO) {
        List<LabelGroup> list = labelGroupService.getList(labelGroupQueryDTO);
        return new DataResponseBody(list);

    }

    @ApiOperation(value = "标签组编辑")
    @PutMapping(value = "/labelGroup/{labelGroupId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody update(@PathVariable(name = "labelGroupId") Long labelGroupId, @Validated @RequestBody LabelGroupCreateDTO labelGroupCreateDTO) {
        labelGroupService.update(labelGroupId, labelGroupCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签组删除", notes = "删除标签组及标签组下的标签")
    @DeleteMapping(value = "/labelGroup")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody delete(@Validated @RequestBody LabelGroupDeleteDTO labelGroupDeleteDTO) {
        labelGroupService.delete(labelGroupDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签组导入")
    @PostMapping(value = "/labelGroup/import")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody importLabelGroup(
            @RequestParam(value = "file", required = false) MultipartFile file,
            LabelGroupImportDTO labelGroupImportDTO) {
        return new DataResponseBody(labelGroupService.importLabelGroup(labelGroupImportDTO, file));
    }


    @ApiOperation(value = "标签组复制")
    @PostMapping(value = "/labelGroup/copy")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody copy(@Validated @RequestBody LabelGroupCopyDTO labelGroupCopyDTO) {
        labelGroupService.copy(labelGroupCopyDTO);
        return new DataResponseBody();
    }



    @ApiOperation(value = "普通标签组转预置")
    @PostMapping(value = "labelGroup/convertPreset")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody convertPreset(@RequestBody GroupConvertPresetDTO groupConvertPresetDTO) {
        labelGroupService.convertPreset(groupConvertPresetDTO);
        return new DataResponseBody();
    }

}
