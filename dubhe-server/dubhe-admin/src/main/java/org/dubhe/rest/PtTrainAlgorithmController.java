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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.*;
import org.dubhe.service.PtTrainAlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 训练算法
 * @date 2020-04-27
 */
@Api(tags = "训练：算法管理")
@RestController
@ApiVersion(1)
@RequestMapping("/api/{version}/algorithm")
public class PtTrainAlgorithmController {

    @Autowired
    private PtTrainAlgorithmService ptTrainAlgorithmService;

    @GetMapping
    @ApiOperation("查询算法")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody getAlgorithms(@Validated PtTrainAlgorithmQueryDTO ptTrainAlgorithmQueryDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.queryAll(ptTrainAlgorithmQueryDTO));
    }

    @GetMapping("/myAlgorithmCount")
    @ApiOperation("查询当前用户的算法个数")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody getAlgorithmCount() {
        return new DataResponseBody(ptTrainAlgorithmService.getAlgorithmCount());
    }

    @PostMapping
    @ApiOperation("新增算法")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody create(@Validated @RequestBody PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.create(ptTrainAlgorithmCreateDTO));
    }

    @PutMapping
    @ApiOperation("修改算法")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody update(@Validated @RequestBody PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.update(ptTrainAlgorithmUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除算法")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody deleteAll(@Validated @RequestBody PtTrainAlgorithmDeleteDTO ptTrainAlgorithmDeleteDTO) {
        ptTrainAlgorithmService.deleteAll(ptTrainAlgorithmDeleteDTO);
        return new DataResponseBody();
    }

    @PostMapping("/uploadAlgorithm")
    @ApiOperation("模型优化上传算法")
    @RequiresPermissions(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody modelOptimizationUploadAlgorithm(@Validated @RequestBody PtModelAlgorithmCreateDTO ptModelAlgorithmCreateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.modelOptimizationUploadAlgorithm(ptModelAlgorithmCreateDTO));
    }

}
