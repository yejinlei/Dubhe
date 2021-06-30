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
import org.dubhe.data.domain.dto.ConversionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionDeleteDTO;
import org.dubhe.data.domain.dto.DatasetVersionQueryCriteriaDTO;
import org.dubhe.data.service.DatasetVersionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description 数据集版本管理
 * @date 2020-05-14
 */
@Api(tags = "数据处理：数据集版本管理")
@RestController
@RequestMapping(Constant.MODULE_URL_PREFIX + "/datasets/versions")
public class DatasetVersionController {

    @Resource
    private DatasetVersionService datasetVersionService;

    @ApiOperation("数据集版本发布")
    @PostMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody publish(@Validated(DatasetVersionCreateDTO.Create.class)
                                    @RequestBody DatasetVersionCreateDTO datasetVersionCreateDTO) {
        return new DataResponseBody(datasetVersionService.publish(datasetVersionCreateDTO));
    }

    @ApiOperation("数据集版本url")
    @GetMapping(value = "/{datasetId}/list")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody versionList(@PathVariable(name = "datasetId") Long datasetId) {
        return new DataResponseBody(datasetVersionService.versionList(datasetId));
    }

    @ApiOperation("数据集版本列表")
    @GetMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody datasetVersionList(@Validated DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria) {
        return new DataResponseBody(datasetVersionService.getList(datasetVersionQueryCriteria));
    }

    @ApiOperation("数据集版本切换")
    @PutMapping("/{datasetId}")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody versionSwitch(@PathVariable(value = "datasetId", required = true) Long datasetId,
                                          @RequestParam(value = "versionName", required = true) String versionName) {
        datasetVersionService.versionSwitch(datasetId, versionName);
        return new DataResponseBody();
    }

    @ApiOperation("数据集版本删除")
    @DeleteMapping
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody delete(@Validated @RequestBody DatasetVersionDeleteDTO datasetVersionDeleteDTO) {
        datasetVersionService.versionDelete(datasetVersionDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation("获取下一个版本号")
    @GetMapping("/{datasetId}/nextVersionName")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody getNextVersionName(@PathVariable(value = "datasetId", required = true) Long datasetId) {
        return new DataResponseBody(datasetVersionService.getNextVersionName(datasetId));
    }

    @ApiOperation("转换完成回调接口")
    @PostMapping(value = "/{datasetVersionId}/convert/finish")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody finishConvert(@PathVariable(value = "datasetVersionId") Long datasetVersionId, @Validated @RequestBody ConversionCreateDTO conversionCreateDTO) {
        return new DataResponseBody(datasetVersionService.finishConvert(datasetVersionId, conversionCreateDTO));
    }

    @ApiOperation("查询当前数据集版本的原始文件数量")
    @GetMapping("/{datasetId}/originFileCount")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody getFileCount(@PathVariable(value = "datasetId", required = true) Long datasetId) {
        return new DataResponseBody(datasetVersionService.getSourceFileCount(datasetId));
    }

    @ApiOperation("生成ofRecord")
    @PutMapping(value = "/{datasetId}/ofRecord")
    @PreAuthorize(Permissions.DATA)
    public DataResponseBody createOfRecord(@PathVariable(value = "datasetId") Long datasetId,
                                           @RequestParam(value = "versionName", required = true) String versionName) {
        datasetVersionService.createOfRecord(datasetId, versionName);
        return new DataResponseBody();
    }
}
