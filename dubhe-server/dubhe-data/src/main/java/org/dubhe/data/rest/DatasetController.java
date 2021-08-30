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
import org.dubhe.data.domain.vo.DatasetQueryDTO;
import org.dubhe.data.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;



/**
 * @description 数据集管理
 * @date 2020-04-10
 */
@Api(tags = "数据处理：数据集管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @ApiOperation(value = "数据集创建")
    @PostMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody createDataset(@Validated(DatasetCreateDTO.Create.class) @RequestBody DatasetCreateDTO datasetCreateDTO) {
        return new DataResponseBody(datasetService.create(datasetCreateDTO));
    }

    @ApiOperation(value = "数据集查询")
    @GetMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody query(Page page, DatasetQueryDTO datasetQueryDTO) {
        return new DataResponseBody(datasetService.listVO(page, datasetQueryDTO));
    }

    @ApiOperation(value = "数据集详情")
    @GetMapping(value = "/{datasetId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody get(@PathVariable(name = "datasetId") Long datasetId) {
        return new DataResponseBody(datasetService.get(datasetId));
    }

    @ApiOperation(value = "数据集进度")
    @GetMapping(value = "/progress")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody progress(@RequestParam List<Long> datasetIds) {
        return new DataResponseBody(datasetService.progress(datasetIds));
    }

    @ApiOperation(value = "数据集修改")
    @PutMapping(value = "/{datasetId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody update(@PathVariable(name = "datasetId") Long datasetId,
                                   @Validated @RequestBody DatasetCreateDTO datasetCreateDTO) {
        return new DataResponseBody(datasetService.update(datasetCreateDTO, datasetId));
    }

    @ApiOperation(value = "数据集删除", notes = "数据集下的文件会同时被删除")
    @DeleteMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody delete(@Validated @RequestBody DatasetDeleteDTO datasetDeleteDTO) {
        datasetService.delete(datasetDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "数据集下载", notes = "压缩数据集并下载")
    @GetMapping(value = "/{datasetId}/download")
    @PreAuthorize(Permissions.DATA)
    public void download(@PathVariable(name = "datasetId") Long datasetId, HttpServletResponse httpServletResponse) {
        datasetService.download(datasetId, httpServletResponse);
    }

    @ApiOperation(value = "数据集查询(有版本)")
    @GetMapping(value = "/versions/filter")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody queryConfirmDatasetVersion(Page page, DatasetIsVersionDTO datasetIsVersionDTO) {
        return new DataResponseBody(datasetService.dataVersionListVO(page, datasetIsVersionDTO));
    }

    @ApiOperation(value = "数据集增强")
    @PostMapping(value = "/enhance")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody enhance(@Validated @RequestBody DatasetEnhanceRequestDTO datasetEnhanceRequestDTO) {
        datasetService.enhance(datasetEnhanceRequestDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "查询公共和个人数据集的数量")
    @GetMapping(value = "/count")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody queryDatasetsCount() {
        return new DataResponseBody(datasetService.queryDatasetsCount());
    }

    @ApiOperation(value = "查询数据集状态")
    @GetMapping(value = "/status")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody determineIfTheDatasetIsAnImport(@RequestParam List<Long> datasetIds) {
        return new DataResponseBody(datasetService.determineIfTheDatasetIsAnImport(datasetIds));
    }

    @ApiOperation(value = "导入用户自定义数据集")
    @PostMapping(value = "/custom")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody importDataset(@RequestBody DatasetCustomCreateDTO datasetCustomCreateDTO) {
        return new DataResponseBody(datasetService.importDataset(datasetCustomCreateDTO));
    }

    @ApiOperation(value = "数据集置顶")
    @GetMapping(value = "/{datasetId}/top")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody topDataset(@PathVariable(name = "datasetId") Long datasetId) {
        datasetService.topDataset(datasetId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "普通数据集转预置")
    @PostMapping(value = "/convertPreset")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody convertPreset(@RequestBody DatasetConvertPresetDTO datasetConvertPresetDTO) {
        datasetService.convertPreset(datasetConvertPresetDTO);
        return new DataResponseBody();
    }


    @ApiOperation(value = "根据数据集ID查询数据集是否转换信息")
    @GetMapping(value = "/getConvertInfoByDatasetId")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody getConvertInfoByDatasetId(@RequestParam(value = "datasetId") Long datasetId) {
        return new DataResponseBody(datasetService.getConvertInfoByDatasetId(datasetId));
    }

    @ApiOperation("获取预置数据集列表")
    @GetMapping(value = "/getPresetDataset")
    public DataResponseBody getPresetDataset() {
        return new DataResponseBody(datasetService.getPresetDataset());
    }

}
