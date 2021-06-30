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

package org.dubhe.serving.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.annotation.ApiVersion;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.serving.domain.dto.PredictParamDTO;
import org.dubhe.serving.domain.dto.ServingInfoCreateDTO;
import org.dubhe.serving.domain.dto.ServingInfoDeleteDTO;
import org.dubhe.serving.domain.dto.ServingInfoDetailDTO;
import org.dubhe.serving.domain.dto.ServingInfoQueryDTO;
import org.dubhe.serving.domain.dto.ServingInfoUpdateDTO;
import org.dubhe.serving.domain.dto.ServingStartDTO;
import org.dubhe.serving.domain.dto.ServingStopDTO;
import org.dubhe.serving.service.ServingService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @description 在线服务管理
 * @date 2020-08-24
 */
@Api(tags = "云端Serving：在线服务管理")
@RestController
@ApiVersion(1)
@RequestMapping("/services")
public class ServingController {

    @Resource
    private ServingService servingService;

    @ApiOperation("查询服务")
    @GetMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody query(ServingInfoQueryDTO servingInfoQueryDTO) {
        return new DataResponseBody(servingService.query(servingInfoQueryDTO));
    }

    @ApiOperation("创建服务")
    @PostMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_CREATE)
    public DataResponseBody create(@Validated @RequestBody ServingInfoCreateDTO servingInfoCreateDTO) {
        return new DataResponseBody(servingService.create(servingInfoCreateDTO));
    }

    @ApiOperation("修改服务")
    @PutMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_EDIT)
    public DataResponseBody update(@Validated @RequestBody ServingInfoUpdateDTO servingInfoUpdateDTO) {
        return new DataResponseBody(servingService.update(servingInfoUpdateDTO));
    }

    @ApiOperation("删除服务")
    @DeleteMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_DELETE)
    public DataResponseBody delete(@Validated @RequestBody ServingInfoDeleteDTO servingInfoDeleteDTO) {
        return new DataResponseBody(servingService.delete(servingInfoDeleteDTO));
    }

    @ApiOperation("获取服务详情")
    @GetMapping("/detail")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getDetail(@Validated ServingInfoDetailDTO servingInfoDetailDTO) {
        return new DataResponseBody(servingService.getDetail(servingInfoDetailDTO));
    }

    @ApiOperation("启动服务")
    @PostMapping("/start")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_START)
    public DataResponseBody start(@Validated @RequestBody ServingStartDTO servingStartDTO) {
        return new DataResponseBody(servingService.start(servingStartDTO));
    }

    @ApiOperation("停止服务")
    @PostMapping("/stop")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_STOP)
    public DataResponseBody stop(@Validated @RequestBody ServingStopDTO servingStopDTO) {
        return new DataResponseBody(servingService.stop(servingStopDTO));
    }

    @ApiOperation("获取接口参数")
    @GetMapping("/predictParam")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getPredictParam(@Validated PredictParamDTO predictParamDTO) {
        return new DataResponseBody(servingService.getPredictParam(predictParamDTO));
    }

    @GetMapping("/servingConfig/pod/{modelConfigId}")
    @ApiOperation("获取modelConfigId下pod信息")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingLog(@PathVariable Long modelConfigId) {
        return new DataResponseBody(servingService.getPods(modelConfigId));
    }

    @PostMapping("/predict")
    @ApiOperation("预测")
    public DataResponseBody predict(MultipartFile[] files, Long id, String url) {
        return new DataResponseBody(servingService.predict(id, url, files));
    }

    @GetMapping("/metrics/{servingId}")
    @ApiOperation("获取服务的监控信息")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingMetrics(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getMetricsDetail(servingId));
    }

    @GetMapping("/rollback/{servingId}")
    @ApiOperation("获取回滚列表")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getRollbackList(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getRollbackList(servingId));
    }

    @GetMapping("/getModelStatus")
    @ApiOperation("查询模型是否在运行中（模型模块远程调用）")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody<Boolean> getServingModelStatus(@Validated PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        return new DataResponseBody(servingService.getServingModelStatus(ptModelStatusQueryDTO));
    }
}
