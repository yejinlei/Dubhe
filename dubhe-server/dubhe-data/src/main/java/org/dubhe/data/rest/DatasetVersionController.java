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
import org.dubhe.data.domain.dto.ConversionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;
import org.dubhe.data.domain.dto.DatasetVersionDeleteDTO;
import org.dubhe.data.domain.dto.DatasetVersionQueryCriteriaDTO;
import org.dubhe.data.service.DatasetVersionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static org.dubhe.constant.Permissions.DATA;

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
    @RequiresPermissions(DATA)
    public DataResponseBody publish(@Validated(DatasetVersionCreateDTO.Create.class)
                                    @RequestBody DatasetVersionCreateDTO datasetVersionCreateDTO) {
        return new DataResponseBody(datasetVersionService.publish(datasetVersionCreateDTO));
    }

    @ApiOperation("数据集版本url")
    @GetMapping(value = "/{datasetId}/list")
    @RequiresPermissions(DATA)
    public DataResponseBody versionList(@PathVariable(name = "datasetId") Long datasetId) {
        return new DataResponseBody(datasetVersionService.versionList(datasetId));
    }

    @ApiOperation("数据集版本列表")
    @GetMapping
    @RequiresPermissions(DATA)
    public DataResponseBody datasetVersionList(@Validated DatasetVersionQueryCriteriaDTO datasetVersionQueryCriteria, Page page) {
        return new DataResponseBody(datasetVersionService.getList(datasetVersionQueryCriteria, page));
    }

    @ApiOperation("数据集版本切换")
    @PutMapping("/{datasetId}")
    @RequiresPermissions(DATA)
    public DataResponseBody versionSwitch(@PathVariable(value = "datasetId", required = true) Long datasetId,
                                          @RequestParam(value = "versionName", required = true) String versionName) {
        datasetVersionService.versionSwitch(datasetId, versionName);
        return new DataResponseBody();
    }

    @ApiOperation("数据集版本删除")
    @DeleteMapping
    @RequiresPermissions(DATA)
    public DataResponseBody delete(@Validated @RequestBody DatasetVersionDeleteDTO datasetVersionDeleteDTO) {
        datasetVersionService.versionDelete(datasetVersionDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation("获取下一个版本号")
    @GetMapping("/{datasetId}/nextVersionName")
    @RequiresPermissions(DATA)
    public DataResponseBody getNextVersionName(@PathVariable(value = "datasetId", required = true) Long datasetId) {
        return new DataResponseBody(datasetVersionService.getNextVersionName(datasetId));
    }

    @ApiOperation("转换完成回调接口")
    @PostMapping(value = "/{datasetVersionId}/convert/finish")
    public DataResponseBody finishConvert(@PathVariable(value = "datasetVersionId") Long datasetVersionId, @Validated @RequestBody ConversionCreateDTO conversionCreateDTO) {
        return new DataResponseBody(datasetVersionService.finishConvert(datasetVersionId, conversionCreateDTO));
    }

    @ApiOperation("查询当前数据集版本的原始文件数量")
    @GetMapping("/{datasetId}/originFileCount")
    @RequiresPermissions(DATA)
    public DataResponseBody getFileCount(@PathVariable(value = "datasetId", required = true) Long datasetId) {
        return new DataResponseBody(datasetVersionService.getSourceFileCount(datasetId));
    }

}
