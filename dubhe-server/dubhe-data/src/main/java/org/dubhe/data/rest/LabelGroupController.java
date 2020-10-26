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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.base.DataResponseBody;
import org.dubhe.data.constant.Constant;
import org.dubhe.data.domain.dto.LabelGroupCopyDTO;
import org.dubhe.data.domain.dto.LabelGroupCreateDTO;
import org.dubhe.data.domain.dto.LabelGroupDeleteDTO;
import org.dubhe.data.domain.dto.LabelGroupImportDTO;
import org.dubhe.data.domain.vo.LabelGroupQueryVO;
import org.dubhe.data.domain.vo.LabelGroupVO;
import org.dubhe.data.service.LabelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.dubhe.constant.Permissions.DATA;

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
    @RequiresPermissions(DATA)
    public DataResponseBody create(@Validated @RequestBody LabelGroupCreateDTO labelGroupCreateDTO) {
        labelGroupService.creatLabelGroup(labelGroupCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签组分页列表")
    @GetMapping(value = "/labelGroup/query")
    @RequiresPermissions(DATA)
    public DataResponseBody query(Page page, LabelGroupQueryVO labelGroupQueryVO) {
        return new DataResponseBody(labelGroupService.listVO(page, labelGroupQueryVO));
    }

    @ApiOperation(value = "标签组详情")
    @GetMapping(value = "/labelGroup/{labelGroupId}")
    @RequiresPermissions(DATA)
    public DataResponseBody get(@PathVariable(name = "labelGroupId") Long labelGroupId) {
        LabelGroupVO labelGroupVO = labelGroupService.get(labelGroupId);
        return new DataResponseBody(labelGroupVO);
    }

    @ApiOperation(value = "标签组列表")
    @GetMapping(value = "/labelGroup/getList/{type}")
    @RequiresPermissions(DATA)
    public DataResponseBody query(@PathVariable(name = "type") Integer type) {
        return new DataResponseBody(labelGroupService.getList(type));
    }

    @ApiOperation(value = "标签组编辑")
    @PutMapping(value = "/labelGroup/{labelGroupId}")
    @RequiresPermissions(DATA)
    public DataResponseBody update(@PathVariable(name = "labelGroupId") Long labelGroupId, @Validated @RequestBody LabelGroupCreateDTO labelGroupCreateDTO) {
        labelGroupService.update(labelGroupId, labelGroupCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签组删除", notes = "删除标签组及标签组下的标签")
    @DeleteMapping(value = "/labelGroup")
    @RequiresPermissions(DATA)
    public DataResponseBody delete(@Validated @RequestBody LabelGroupDeleteDTO labelGroupDeleteDTO) {
        labelGroupService.delete(labelGroupDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "标签组导入")
    @PostMapping(value = "/labelGroup/import")
    @RequiresPermissions(DATA)
    public DataResponseBody importLabelGroup(
            @RequestParam(value = "file", required = false) MultipartFile file,
            LabelGroupImportDTO labelGroupImportDTO) {
        labelGroupService.importLabelGroup(labelGroupImportDTO, file);
        return new DataResponseBody();
    }


    @ApiOperation(value = "标签组复制")
    @PostMapping(value = "/labelGroup/copy")
    @RequiresPermissions(DATA)
    public DataResponseBody copy(@Validated @RequestBody LabelGroupCopyDTO labelGroupCopyDTO) {
        labelGroupService.copy(labelGroupCopyDTO);
        return new DataResponseBody();
    }

}
