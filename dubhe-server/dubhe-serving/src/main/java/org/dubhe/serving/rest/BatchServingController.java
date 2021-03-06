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
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.serving.domain.dto.BatchServingCreateDTO;
import org.dubhe.serving.domain.dto.BatchServingDeleteDTO;
import org.dubhe.serving.domain.dto.BatchServingDetailDTO;
import org.dubhe.serving.domain.dto.BatchServingQueryDTO;
import org.dubhe.serving.domain.dto.BatchServingStartDTO;
import org.dubhe.serving.domain.dto.BatchServingStopDTO;
import org.dubhe.serving.domain.dto.BatchServingUpdateDTO;
import org.dubhe.serving.service.BatchServingService;
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

import javax.annotation.Resource;

/**
 * @description ??????????????????
 * @date 2020-08-27
 */
@Api(tags = "??????Serving?????????????????????")
@RestController
@RequestMapping("/batchServices")
public class BatchServingController {

    @Resource
    private BatchServingService batchServingService;

    @ApiOperation("??????????????????")
    @GetMapping
    @PreAuthorize(Permissions.SERVING_BATCH)
    public DataResponseBody query(BatchServingQueryDTO batchServingQueryDTO) {
        return new DataResponseBody(batchServingService.query(batchServingQueryDTO));
    }

    @ApiOperation("??????????????????")
    @PostMapping
    @PreAuthorize(Permissions.SERVING_BATCH_CREATE)
    public DataResponseBody create(@Validated @RequestBody BatchServingCreateDTO batchServingCreateDTO) {
        return new DataResponseBody(batchServingService.create(batchServingCreateDTO));
    }

    @ApiOperation("??????????????????")
    @PutMapping
    @PreAuthorize(Permissions.SERVING_BATCH_EDIT)
    public DataResponseBody update(@Validated @RequestBody BatchServingUpdateDTO batchServingUpdateDTO) {
        return new DataResponseBody(batchServingService.update(batchServingUpdateDTO));
    }

    @ApiOperation("??????????????????")
    @DeleteMapping
    @PreAuthorize(Permissions.SERVING_BATCH_DELETE)
    public DataResponseBody delete(@Validated @RequestBody BatchServingDeleteDTO batchServingDeleteDTO) {
        return new DataResponseBody(batchServingService.delete(batchServingDeleteDTO));
    }

    @ApiOperation("??????????????????")
    @PostMapping("/start")
    @PreAuthorize(Permissions.SERVING_BATCH_START)
    public DataResponseBody start(@Validated @RequestBody BatchServingStartDTO batchServingStartDTO) {
        return new DataResponseBody(batchServingService.start(batchServingStartDTO));
    }

    @ApiOperation("??????????????????")
    @PostMapping("/stop")
    @PreAuthorize(Permissions.SERVING_BATCH_STOP)
    public DataResponseBody stop(@Validated @RequestBody BatchServingStopDTO batchServingStopDTO) {
        return new DataResponseBody(batchServingService.stop(batchServingStopDTO));
    }

    @ApiOperation("????????????????????????")
    @GetMapping("/detail")
    @PreAuthorize(Permissions.SERVING_BATCH)
    public DataResponseBody getDetail(@Validated BatchServingDetailDTO batchServingDetailDTO) {
        return new DataResponseBody(batchServingService.getDetail(batchServingDetailDTO));
    }

    @GetMapping("/pod/{id}")
    @ApiOperation("?????????????????????pod??????")
    @PreAuthorize(Permissions.SERVING_BATCH)
    public DataResponseBody getServingLog(@PathVariable Long id) {
        return new DataResponseBody(batchServingService.getPods(id));
    }


    @GetMapping("/queryById/{id}")
    @ApiOperation("?????????????????????????????????")
    @PreAuthorize(Permissions.SERVING_BATCH)
    public DataResponseBody queryStatusAndProgress(@PathVariable Long id) {
        return new DataResponseBody(batchServingService.queryStatusAndProgress(id));
    }

    @GetMapping("/getModelStatus")
    @ApiOperation("????????????????????????????????????????????????????????????")
    @PreAuthorize(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody<Boolean> getServingModelStatus(@Validated PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        return new DataResponseBody(batchServingService.getServingModelStatus(ptModelStatusQueryDTO));
    }
}
