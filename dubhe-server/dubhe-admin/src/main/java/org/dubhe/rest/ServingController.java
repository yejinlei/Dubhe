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
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.PredictParamDTO;
import org.dubhe.domain.dto.ServingInfoCreateDTO;
import org.dubhe.domain.dto.ServingInfoDeleteDTO;
import org.dubhe.domain.dto.ServingInfoDetailDTO;
import org.dubhe.domain.dto.ServingInfoQueryDTO;
import org.dubhe.domain.dto.ServingInfoUpdateDTO;
import org.dubhe.domain.dto.ServingStartDTO;
import org.dubhe.domain.dto.ServingStopDTO;
import org.dubhe.service.ServingService;
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
@RequestMapping("/api/serving")
public class ServingController {

    @Resource
    private ServingService servingService;

    @ApiOperation("查询服务")
    @GetMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody query(ServingInfoQueryDTO servingInfoQueryDTO) {
        return new DataResponseBody(servingService.query(servingInfoQueryDTO));
    }

    @ApiOperation("创建服务")
    @PostMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody create(@Validated @RequestBody ServingInfoCreateDTO servingInfoCreateDTO) {
        return new DataResponseBody(servingService.create(servingInfoCreateDTO));
    }

    @ApiOperation("修改服务")
    @PutMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody update(@Validated @RequestBody ServingInfoUpdateDTO servingInfoUpdateDTO) {
        return new DataResponseBody(servingService.update(servingInfoUpdateDTO));
    }

    @ApiOperation("删除服务")
    @DeleteMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody delete(@Validated @RequestBody ServingInfoDeleteDTO servingInfoDeleteDTO) {
        return new DataResponseBody(servingService.delete(servingInfoDeleteDTO));
    }

    @ApiOperation("获取服务详情")
    @GetMapping("/detail")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getDetail(@Validated ServingInfoDetailDTO servingInfoDetailDTO) {
        return new DataResponseBody(servingService.getDetail(servingInfoDetailDTO));
    }

    @ApiOperation("启动服务")
    @PostMapping("/start")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody start(@Validated @RequestBody ServingStartDTO servingStartDTO) {
        return new DataResponseBody(servingService.start(servingStartDTO));
    }

    @ApiOperation("停止服务")
    @PostMapping("/stop")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody stop(@Validated @RequestBody ServingStopDTO servingStopDTO) {
        return new DataResponseBody(servingService.stop(servingStopDTO));
    }

    @ApiOperation("获取接口参数")
    @GetMapping("/predictParam")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getPredictParam(@Validated PredictParamDTO predictParamDTO) {
        return new DataResponseBody(servingService.getPredictParam(predictParamDTO));
    }

    @GetMapping("/servingConfig/pod/{modelConfigId}")
    @ApiOperation("获取modelConfigId下pod信息")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingLog(@PathVariable Long modelConfigId) {
        return new DataResponseBody(servingService.getPods(modelConfigId));
    }

    @PostMapping("/predict")
    @ApiOperation("预测")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody predict(MultipartFile[] files, Long id, String url) {
        return new DataResponseBody(servingService.predict(id, url, files));
    }

    @GetMapping("/metrics/{servingId}")
    @ApiOperation("获取服务的监控信息")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingMetrics(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getMetricsDetail(servingId));
    }

    @GetMapping("/rollback/{servingId}")
    @ApiOperation("获取回滚列表")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getRollbackList(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getRollbackList(servingId));
    }
}
