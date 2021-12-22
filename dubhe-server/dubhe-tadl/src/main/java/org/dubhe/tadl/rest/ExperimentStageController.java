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
import org.dubhe.tadl.domain.dto.MaxExecDurationUpdateDTO;
import org.dubhe.tadl.domain.dto.MaxTrialNumUpdateDTO;
import org.dubhe.tadl.domain.dto.TrialConcurrentNumUpdateDTO;
import org.dubhe.tadl.domain.dto.UpdateStageYamlDTO;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.enums.TimeUnitEnum;
import org.dubhe.tadl.service.ExperimentStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 实验阶段管理
 * @date 2021-03-22
 */
@Api(tags = "TADL：实验阶段管理")
@RestController
@RequestMapping(TadlConstant.MODULE_URL_PREFIX + "/experiment/stage")
public class ExperimentStageController {

    /**
     * 实验阶段服务实现类
     */
    @Autowired
    private ExperimentStageService experimentStageService;


    @ApiOperation(value = "实验阶段概览")
    @GetMapping(value = "/{experimentId}/{stageOrder}")
    public DataResponseBody query(@PathVariable("experimentId") Long experimentId,@PathVariable("stageOrder") Integer stageOrder) {
        return new DataResponseBody(experimentStageService.query(experimentId,stageOrder));
    }

    @ApiOperation(value = "实验阶段参数")
    @GetMapping(value = "/{experimentId}/{stageOrder}/param")
    public DataResponseBody queryStageParam(@PathVariable("experimentId") Long experimentId,@PathVariable("stageOrder") Integer stageOrder) {
        return new DataResponseBody(experimentStageService.queryStageParam(experimentId,stageOrder));
    }

    @ApiOperation(value = "实验阶段运行参数")
    @GetMapping(value = "/{experimentId}/{stageOrder}/runtime/param")
    public DataResponseBody queryRuntimeParam(@PathVariable("experimentId") Long experimentId,@PathVariable("stageOrder") Integer stageOrder) {
        return new DataResponseBody(experimentStageService.queryRuntimeParam(experimentId,stageOrder));
    }

    @ApiOperation(value = "trial 列表（展示最高精度五条）")
    @GetMapping(value = "/{experimentId}/{stageOrder}/trial/rep")
    public DataResponseBody queryTrialRep(@PathVariable("experimentId") Long experimentId,@PathVariable("stageOrder") Integer stageOrder) {
        return new DataResponseBody(experimentStageService.queryTrialRep(experimentId,stageOrder));
    }

    @ApiOperation(value = "获取 Experiment 下当前阶段 yaml")
    @GetMapping(value = "/{experimentId}/{stageOrder}/yaml")
    public DataResponseBody getConfiguration(@PathVariable("experimentId") Long experimentId,@PathVariable("stageOrder") Integer stageOrder) {
        return new DataResponseBody(experimentStageService.getConfiguration(experimentId,stageOrder));
    }

    @ApiOperation(value = "修改 Experiment 下当前阶段 yaml")
    @PutMapping(value = "/update/yaml")
    public DataResponseBody updateConfiguration(@Validated @RequestBody UpdateStageYamlDTO updateStageYamlDTO) {
        experimentStageService.updateConfiguration(updateStageYamlDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "修改 Experiment 下当前阶段 最大运行时间")
    @PutMapping(value = "/update/MaxExecDuration")
    public DataResponseBody updateMaxExecDuration(@Validated @RequestBody MaxExecDurationUpdateDTO maxExecDurationUpdateDTO) {
        experimentStageService.updateMaxExecDuration(maxExecDurationUpdateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "修改 Experiment 下当前阶段 最大trial数量")
    @PutMapping(value = "/update/MaxTrialNum")
    public DataResponseBody updateMaxTrialNum(@Validated @RequestBody MaxTrialNumUpdateDTO maxTrialNumUpdateDTO) {
        experimentStageService.updateMaxTrialNum(maxTrialNumUpdateDTO);
        return new DataResponseBody();
    }

    @ApiOperation(value = "修改 Experiment 下当前阶段 最大并发数")
    @PutMapping(value = "/update/ConcurrentNum")
    public DataResponseBody updateTrialConcurrentNum(@Validated @RequestBody TrialConcurrentNumUpdateDTO trialConcurrentNumUpdateDTO) {
        experimentStageService.updateTrialConcurrentNum(trialConcurrentNumUpdateDTO);
        return new DataResponseBody();
    }

}
