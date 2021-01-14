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
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.*;
import org.dubhe.enums.TrainTypeEnum;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.service.PtTrainJobService;
import org.dubhe.service.PtTrainJobSpecsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @description 训练作业job
 * @date 2020-04-27
 */
@Api(tags = "训练：任务管理")
@RestController
@ApiVersion(1)
@RequestMapping("/api/{version}/trainJob")
public class PtTrainJobController {

    @Autowired
    private PtTrainJobService ptTrainJobService;

    @Autowired
    private PtTrainJobSpecsService ptTrainJobSpecsService;

    @GetMapping
    @ApiOperation("作业列表展示")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJob(@Validated PtTrainQueryDTO ptTrainQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJob(ptTrainQueryDTO));
    }

    @GetMapping("/trainJobSpecs")
    @ApiOperation("规格展示")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobSpecs(@Validated PtTrainJobSpecsQueryDTO ptTrainJobSpecsQueryDTO) {
        return new DataResponseBody(ptTrainJobSpecsService.getTrainJobSpecs(ptTrainJobSpecsQueryDTO));
    }

    @GetMapping("/jobDetail")
    @ApiOperation("根据jobId查询训练任务详情")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobDetail(@Validated PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobDetail(ptTrainJobDetailQueryDTO));
    }

    @GetMapping("/mine")
    @ApiOperation(value = "我的训练任务统计", notes = "运行中的任务:PENDDING,RUNNING；完成的任务:其他状态")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody statisticsMine() {
        return new DataResponseBody(ptTrainJobService.statisticsMine());
    }

    @GetMapping("/trainJobVersionDetail")
    @ApiOperation("作业不同版本任务列表展示")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobVersion(@Validated PtTrainJobVersionQueryDTO ptTrainJobVersionQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobVersion(ptTrainJobVersionQueryDTO));
    }

    @GetMapping("/dataSourceStatus")
    @ApiOperation("数据集状态展示")
    @RequiresPermissions(value = {Permissions.TRAINING_JOB, Permissions.DATA}, logical = Logical.OR)
    public DataResponseBody getTrainDataSourceStatus(@Validated PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainDataSourceStatus(ptTrainDataSourceStatusQueryDTO));
    }

    @PostMapping
    @ApiOperation("创建训练任务")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody createTrainJob(@Validated @RequestBody PtTrainJobCreateDTO ptTrainJobCreateDTO) {
        if (TrainTypeEnum.isDistributeTrain(ptTrainJobCreateDTO.getTrainType())
            && ptTrainJobCreateDTO.getResourcesPoolNode() < 2) {
            // 分布式训练节点数校验补偿
            return DataResponseFactory.failed("分布式训练节点个数至少2个");
        }
        return new DataResponseBody(ptTrainJobService.createTrainJobVersion(ptTrainJobCreateDTO));
    }

    @PutMapping
    @ApiOperation("修改训练任务")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody updateTrainJob(@Validated @RequestBody PtTrainJobUpdateDTO ptTrainJobUpdateDTO) {
        return new DataResponseBody(ptTrainJobService.updateTrainJob(ptTrainJobUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除训练任务")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody deleteTrainJob(@Validated @RequestBody PtTrainJobDeleteDTO ptTrainJobDeleteDTO) {
        return new DataResponseBody(ptTrainJobService.deleteTrainJob(ptTrainJobDeleteDTO));
    }

    @PostMapping("/stop")
    @ApiOperation("停止训练任务")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody stopTrainJob(@Validated @RequestBody PtTrainJobStopDTO ptTrainJobStopDTO) {
        return new DataResponseBody(ptTrainJobService.stopTrainJob(ptTrainJobStopDTO));
    }

    @PostMapping("/resume")
    @ApiOperation("恢复训练任务")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody resumeTrainJob(@Validated @RequestBody PtTrainJobResumeDTO ptTrainJobResumeDTO) {
        ptTrainJobService.resumeTrainJob(ptTrainJobResumeDTO);
        return new DataResponseBody();
    }

    @GetMapping("/grafanaUrl/{jobId}")
    @ApiOperation("获取job在grafana监控的地址")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getGrafanaUrl( @PathVariable Long jobId) {
        return new DataResponseBody(ptTrainJobService.getGrafanaUrl(jobId));
    }

    @GetMapping("/model")
    @ApiOperation("获取job使用的模型")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobModel(@Validated PtTrainModelDTO ptTrainModelDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobModel(ptTrainModelDTO));
    }
}
