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

package org.dubhe.algorithm.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.algorithm.constant.UserAuxiliaryInfoConstant;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageUpdateDTO;
import org.dubhe.algorithm.service.PtTrainAlgorithmUsageService;
import org.dubhe.biz.base.annotation.ApiVersion;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 算法用途管理
 * @date 2020-06-19
 */
@Api(tags = "训练：算法用途管理")
@RestController
@RequestMapping("/algorithmUsage")
public class PtTrainAlgorithmUsageController {

    @Autowired
    private PtTrainAlgorithmUsageService ptTrainAlgorithmUsageService;

    @GetMapping
    @ApiOperation("算法用途列表展示")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody queryAll(@Validated PtTrainAlgorithmUsageQueryDTO ptTrainAlgorithmUsageQueryDTO) {
        ptTrainAlgorithmUsageQueryDTO.setType(UserAuxiliaryInfoConstant.ALGORITHM_USAGE);
        return DataResponseFactory
                .success(ptTrainAlgorithmUsageService.queryAll(ptTrainAlgorithmUsageQueryDTO));
    }

    @PostMapping
    @ApiOperation("新增算法用途")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_CREATE)
    public DataResponseBody create(
            @Validated @RequestBody PtTrainAlgorithmUsageCreateDTO ptTrainAlgorithmUsageCreateDTO) {
        ptTrainAlgorithmUsageCreateDTO.setType(UserAuxiliaryInfoConstant.ALGORITHM_USAGE);
        return DataResponseFactory.success(ptTrainAlgorithmUsageService.create(ptTrainAlgorithmUsageCreateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除算法用途")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_DELETE)
    public DataResponseBody deleteAll(@Validated @RequestBody PtTrainAlgorithmUsageDeleteDTO ptTrainAlgorithmUsageDeleteDTO) {
        ptTrainAlgorithmUsageService.deleteAll(ptTrainAlgorithmUsageDeleteDTO);
        return new DataResponseBody();
    }

    @PutMapping
    @ApiOperation("修改算法用途")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_EDIT)
    public DataResponseBody update(
            @Validated @RequestBody PtTrainAlgorithmUsageUpdateDTO ptTrainAlgorithmUsageUpdateDTO) {
        ptTrainAlgorithmUsageService.update(ptTrainAlgorithmUsageUpdateDTO);
        return new DataResponseBody();
    }

}
