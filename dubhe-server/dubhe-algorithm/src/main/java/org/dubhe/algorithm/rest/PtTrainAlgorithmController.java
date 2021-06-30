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
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUpdateDTO;
import org.dubhe.algorithm.service.PtTrainAlgorithmService;
import org.dubhe.biz.base.annotation.ApiVersion;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.ModelOptAlgorithmCreateDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllBatchIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllByIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectByIdDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 训练算法
 * @date 2020-04-27
 */
@Api(tags = "训练：算法管理")
@RestController
@RequestMapping("/algorithms")
public class PtTrainAlgorithmController {

    @Autowired
    private PtTrainAlgorithmService ptTrainAlgorithmService;

    @GetMapping
    @ApiOperation("查询算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody getAlgorithms(@Validated PtTrainAlgorithmQueryDTO ptTrainAlgorithmQueryDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.queryAll(ptTrainAlgorithmQueryDTO));
    }

    @GetMapping("/myAlgorithmCount")
    @ApiOperation("查询当前用户的算法个数")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody getAlgorithmCount() {
        return new DataResponseBody(ptTrainAlgorithmService.getAlgorithmCount());
    }

    @PostMapping
    @ApiOperation("新增算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_CREATE)
    public DataResponseBody create(@Validated @RequestBody PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.create(ptTrainAlgorithmCreateDTO));
    }

    @PutMapping
    @ApiOperation("修改算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_EDIT)
    public DataResponseBody update(@Validated @RequestBody PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.update(ptTrainAlgorithmUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_DELETE)
    public DataResponseBody deleteAll(@Validated @RequestBody PtTrainAlgorithmDeleteDTO ptTrainAlgorithmDeleteDTO) {
        ptTrainAlgorithmService.deleteAll(ptTrainAlgorithmDeleteDTO);
        return new DataResponseBody();
    }

    @GetMapping("/selectAllById")
    @ApiOperation("根据Id查询所有数据")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody<TrainAlgorithmQureyVO> selectAllById(@Validated TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.selectAllById(trainAlgorithmSelectAllByIdDTO));
    }

    @GetMapping("/selectById")
    @ApiOperation("根据Id查询")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody<TrainAlgorithmQureyVO> selectById(@Validated TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.selectById(trainAlgorithmSelectByIdDTO));
    }

    @GetMapping("/selectAllBatchIds")
    @ApiOperation("批量查询")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody<List<TrainAlgorithmQureyVO>> selectAllBatchIds(@Validated TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.selectAllBatchIds(trainAlgorithmSelectAllBatchIdDTO));
    }

    @PostMapping("/uploadAlgorithm")
    @ApiOperation("模型优化上传算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM_CREATE)
    public DataResponseBody modelOptimizationUploadAlgorithm(@Validated @RequestBody ModelOptAlgorithmCreateDTO modelOptAlgorithmCreateDTO) {
        return new DataResponseBody(ptTrainAlgorithmService.modelOptimizationUploadAlgorithm(modelOptAlgorithmCreateDTO));
    }

    @GetMapping("/getInferenceAlgorithm")
    @ApiOperation("查询可推理算法")
    @PreAuthorize(Permissions.DEVELOPMENT_ALGORITHM)
    public DataResponseBody getInferenceAlgorithm() {
        return new DataResponseBody(ptTrainAlgorithmService.getInferenceAlgorithm());
    }
}
