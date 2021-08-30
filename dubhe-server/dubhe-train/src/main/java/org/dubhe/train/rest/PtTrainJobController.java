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

package org.dubhe.train.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.dto.PtTrainDataSourceStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.train.domain.dto.PtTrainJobCreateDTO;
import org.dubhe.train.domain.dto.PtTrainJobDeleteDTO;
import org.dubhe.train.domain.dto.PtTrainJobDetailQueryDTO;
import org.dubhe.train.domain.dto.PtTrainJobResumeDTO;
import org.dubhe.train.domain.dto.PtTrainJobStopDTO;
import org.dubhe.train.domain.dto.PtTrainJobUpdateDTO;
import org.dubhe.train.domain.dto.PtTrainJobVersionQueryDTO;
import org.dubhe.train.domain.dto.PtTrainModelDTO;
import org.dubhe.train.domain.dto.PtTrainQueryDTO;
import org.dubhe.train.domain.dto.VisualTrainQueryDTO;
import org.dubhe.train.enums.TrainTypeEnum;
import org.dubhe.train.service.PtTrainJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * @description 训练作业job
 * @date 2020-04-27
 */
@Api(tags = "训练：任务管理")
@RestController
@RequestMapping("/trainJob")
public class PtTrainJobController {

    @Autowired
    private PtTrainJobService ptTrainJobService;

    @GetMapping
    @ApiOperation("作业列表展示")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJob(@Validated PtTrainQueryDTO ptTrainQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJob(ptTrainQueryDTO));
    }

    @GetMapping("/jobDetail")
    @ApiOperation("根据jobId查询训练任务详情")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobDetail(@Validated PtTrainJobDetailQueryDTO ptTrainJobDetailQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobDetail(ptTrainJobDetailQueryDTO));
    }

    @GetMapping("/mine")
    @ApiOperation(value = "我的训练任务统计", notes = "运行中的任务:PENDDING,RUNNING；完成的任务:其他状态")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody statisticsMine() {
        return new DataResponseBody(ptTrainJobService.statisticsMine());
    }

    @GetMapping("/trainJobVersionDetail")
    @ApiOperation("作业不同版本任务列表展示")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobVersion(@Validated PtTrainJobVersionQueryDTO ptTrainJobVersionQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobVersion(ptTrainJobVersionQueryDTO));
    }

    @GetMapping("/dataSourceStatus")
    @ApiOperation("数据集状态展示")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody<Map<String, Boolean>> getTrainDataSourceStatus(@Validated PtTrainDataSourceStatusQueryDTO ptTrainDataSourceStatusQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainDataSourceStatus(ptTrainDataSourceStatusQueryDTO));
    }

    @GetMapping("/trainModelStatus")
    @ApiOperation("查询该模型是否在训练中（模型管理模块远程调用）")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody<Boolean> getTrainModelStatus(@Validated PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainModelStatus(ptModelStatusQueryDTO));
    }

    @GetMapping("/visualTrain")
    @ApiOperation("可视化训练列表")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getVisualTrainList(@Validated VisualTrainQueryDTO visualTrainQueryDTO) {
        return new DataResponseBody(ptTrainJobService.getVisualTrainList(visualTrainQueryDTO));
    }

    @PostMapping
    @ApiOperation("创建训练任务")
    @PreAuthorize(Permissions.TRAINING_JOB_CREATE)
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
    @PreAuthorize(Permissions.TRAINING_JOB_UPDATE)
    public DataResponseBody updateTrainJob(@Validated @RequestBody PtTrainJobUpdateDTO ptTrainJobUpdateDTO) {
        return new DataResponseBody(ptTrainJobService.updateTrainJob(ptTrainJobUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除训练任务")
    @PreAuthorize(Permissions.TRAINING_JOB_DELETE)
    public DataResponseBody deleteTrainJob(@Validated @RequestBody PtTrainJobDeleteDTO ptTrainJobDeleteDTO) {
        return new DataResponseBody(ptTrainJobService.deleteTrainJob(ptTrainJobDeleteDTO));
    }

    @PostMapping("/stop")
    @ApiOperation("停止训练任务")
    @PreAuthorize(Permissions.TRAINING_JOB_UPDATE)
    public DataResponseBody stopTrainJob(@Validated @RequestBody PtTrainJobStopDTO ptTrainJobStopDTO) {
        return new DataResponseBody(ptTrainJobService.stopTrainJob(ptTrainJobStopDTO));
    }

    @PostMapping("/batchStop")
    @ApiOperation("一键停止所有训练任务")
    @PreAuthorize(Permissions.TRAINING_JOB_UPDATE)
    public DataResponseBody batchStopTrainJob() {
        ptTrainJobService.batchStopTrainJob();
        return new DataResponseBody();
    }

    @PostMapping("/resume")
    @ApiOperation("恢复训练任务")
    @PreAuthorize(Permissions.TRAINING_JOB_UPDATE)
    public DataResponseBody resumeTrainJob(@Validated @RequestBody PtTrainJobResumeDTO ptTrainJobResumeDTO) {
        ptTrainJobService.resumeTrainJob(ptTrainJobResumeDTO);
        return new DataResponseBody();
    }

    @GetMapping("/grafanaUrl/{jobId}")
    @ApiOperation("获取job在grafana监控的地址")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getGrafanaUrl(@PathVariable Long jobId) {
        return new DataResponseBody(ptTrainJobService.getGrafanaUrl(jobId));
    }

    @GetMapping("/model")
    @ApiOperation("获取job使用的模型")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainJobModel(@Validated PtTrainModelDTO ptTrainModelDTO) {
        return new DataResponseBody(ptTrainJobService.getTrainJobModel(ptTrainModelDTO));
    }
}
