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
package org.dubhe.tadl.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.tadl.constant.TadlConstant;
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 实验管理
 * @date 2021-03-22
 */
@Api(tags = "TADL：实验管理")
@RestController
@RequestMapping(TadlConstant.MODULE_URL_PREFIX + "/experiment")
public class ExperimentController {

    /**
     * 实验服务实现类
     */
    @Autowired
    private ExperimentService experimentService;


    @ApiOperation(value = "实验列表查询")
    @GetMapping
    public DataResponseBody query(ExperimentQueryDTO experimentQueryDTO) {
        return new DataResponseBody(experimentService.query(experimentQueryDTO));
    }

    @ApiOperation(value = "创建实验")
    @PostMapping
    public DataResponseBody create(@Validated @RequestBody ExperimentCreateDTO experimentCreateDTO) {
        experimentService.create(experimentCreateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "实验详情")
    @GetMapping(value = "/{experimentId}/info")
    public DataResponseBody info(@PathVariable Long experimentId) {
        return new DataResponseBody(experimentService.info(experimentId));
    }

    @ApiOperation(value = "实验详情概览")
    @GetMapping(value = "/{experimentId}")
    public DataResponseBody getDetail(@PathVariable Long experimentId) {
        return new DataResponseBody(experimentService.getDetail(experimentId));
    }

    @ApiOperation(value = "实验编辑")
    @PutMapping
    public DataResponseBody update(@Validated @RequestBody ExperimentUpdateDTO experimentUpdateDTO) {
        experimentService.update(experimentUpdateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "获取 Experiment 下所有 yaml")
    @GetMapping(value = "/{experimentId}/configuration")
    public DataResponseBody getConfiguration(@PathVariable Long experimentId) {
        return new DataResponseBody(experimentService.getConfiguration(experimentId));
    }


    @ApiOperation(value = "暂停实验")
    @PutMapping(value = "/{experimentId}/pause")
    public DataResponseBody pauseExperiment(@PathVariable Long experimentId) {
        experimentService.pauseExperiment(experimentId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "重启实验")
    @PutMapping(value = "/{experimentId}/restart")
    public DataResponseBody restartExperiment(@PathVariable Long experimentId) {
        experimentService.restartExperiment(experimentId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "启动实验")
    @PutMapping(value = "/{experimentId}/start")
    public DataResponseBody startExperiment(@PathVariable Long experimentId) {
        experimentService.startExperiment(experimentId);
        return new DataResponseBody();
    }

    @ApiOperation(value = "删除实验")
    @DeleteMapping(value = "/{experimentId}")
    public DataResponseBody deleteExperiment(@PathVariable Long experimentId) {
        experimentService.deleteExperiment(experimentId);
        return new DataResponseBody();
    }


    @ApiOperation(value = "查询search_space内容")
    @GetMapping(value = "/{experimentId}/searchSpace")
    public DataResponseBody getSearchSpace(@PathVariable Long experimentId) {
        return new DataResponseBody(experimentService.getSearchSpace(experimentId));
    }

    @ApiOperation(value = "查询best_selected_space内容")
    @GetMapping(value = "/{experimentId}/bestSelectedSpace")
    public DataResponseBody getBestSelectedSpace(@PathVariable Long experimentId) {
        return new DataResponseBody(experimentService.getBestSelectedSpace(experimentId));
    }

    @ApiOperation(value = "查询experiment 当前阶段所有 trial 中间精度")
    @GetMapping(value = "/intermediate/accuracy")
    public DataResponseBody getIntermediateAccuracy(@Validated ExperimentIntermediateAccuracyDTO experimentIntermediateAccuracyDTO) {
        return new DataResponseBody(experimentService.getIntermediateAccuracy(experimentIntermediateAccuracyDTO));
    }

    @ApiOperation(value = "查询experiment 当前阶段所有 trial 最佳精度")
    @GetMapping(value = "/best/accuracy")
    public DataResponseBody getBestAccuracy(@Validated ExperimentBestAccuracyDTO experimentBestAccuracyDTO) {
        return new DataResponseBody(experimentService.getBestAccuracy(experimentBestAccuracyDTO));
    }

    @ApiOperation(value = "查询experiment 当前阶段所有 trial 运行时间")
    @GetMapping(value = "/runTime")
    public DataResponseBody getRunTime(@Validated ExperimentRunTimeDTO experimentRunTimeDTO) {
        return new DataResponseBody(experimentService.getRunTime(experimentRunTimeDTO));
    }

    @ApiOperation(value = "查询experiment 日志")
    @GetMapping(value = "/{experimentId}/logs")
    public DataResponseBody getExperimentLog(@Validated ExperimentLogQueryDTO experimentLogQueryDTO) {
        return new DataResponseBody(experimentService.queryExperimentLog(experimentLogQueryDTO));
    }

}