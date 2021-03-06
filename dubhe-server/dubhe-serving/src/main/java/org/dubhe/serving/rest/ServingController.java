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
 * @description ??????????????????
 * @date 2020-08-24
 */
@Api(tags = "??????Serving?????????????????????")
@RestController
@ApiVersion(1)
@RequestMapping("/services")
public class ServingController {

    @Resource
    private ServingService servingService;

    @ApiOperation("????????????")
    @GetMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody query(ServingInfoQueryDTO servingInfoQueryDTO) {
        return new DataResponseBody(servingService.query(servingInfoQueryDTO));
    }

    @ApiOperation("????????????")
    @PostMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_CREATE)
    public DataResponseBody create(@Validated @RequestBody ServingInfoCreateDTO servingInfoCreateDTO) {
        return new DataResponseBody(servingService.create(servingInfoCreateDTO));
    }

    @ApiOperation("????????????")
    @PutMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_EDIT)
    public DataResponseBody update(@Validated @RequestBody ServingInfoUpdateDTO servingInfoUpdateDTO) {
        return new DataResponseBody(servingService.update(servingInfoUpdateDTO));
    }

    @ApiOperation("????????????")
    @DeleteMapping
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_DELETE)
    public DataResponseBody delete(@Validated @RequestBody ServingInfoDeleteDTO servingInfoDeleteDTO) {
        return new DataResponseBody(servingService.delete(servingInfoDeleteDTO));
    }

    @ApiOperation("??????????????????")
    @GetMapping("/detail")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getDetail(@Validated ServingInfoDetailDTO servingInfoDetailDTO) {
        return new DataResponseBody(servingService.getDetail(servingInfoDetailDTO));
    }

    @ApiOperation("????????????")
    @PostMapping("/start")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_START)
    public DataResponseBody start(@Validated @RequestBody ServingStartDTO servingStartDTO) {
        return new DataResponseBody(servingService.start(servingStartDTO));
    }

    @ApiOperation("????????????")
    @PostMapping("/stop")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT_STOP)
    public DataResponseBody stop(@Validated @RequestBody ServingStopDTO servingStopDTO) {
        return new DataResponseBody(servingService.stop(servingStopDTO));
    }

    @ApiOperation("??????????????????")
    @GetMapping("/predictParam")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getPredictParam(@Validated PredictParamDTO predictParamDTO) {
        return new DataResponseBody(servingService.getPredictParam(predictParamDTO));
    }

    @GetMapping("/servingConfig/pod/{modelConfigId}")
    @ApiOperation("??????modelConfigId???pod??????")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingLog(@PathVariable Long modelConfigId) {
        return new DataResponseBody(servingService.getPods(modelConfigId));
    }

    @PostMapping("/predict")
    @ApiOperation("??????")
    public DataResponseBody predict(MultipartFile[] files, Long id, String url) {
        return new DataResponseBody(servingService.predict(id, url, files));
    }

    @GetMapping("/metrics/{servingId}")
    @ApiOperation("???????????????????????????")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingMetrics(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getMetricsDetail(servingId));
    }

    @GetMapping("/rollback/{servingId}")
    @ApiOperation("??????????????????")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getRollbackList(@PathVariable Long servingId) {
        return new DataResponseBody(servingService.getRollbackList(servingId));
    }

    @GetMapping("/getModelStatus")
    @ApiOperation("????????????????????????????????????????????????????????????")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody<Boolean> getServingModelStatus(@Validated PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        return new DataResponseBody(servingService.getServingModelStatus(ptModelStatusQueryDTO));
    }
}
