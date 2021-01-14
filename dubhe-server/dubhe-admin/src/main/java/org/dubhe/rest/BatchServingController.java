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
import org.dubhe.domain.dto.BatchServingCreateDTO;
import org.dubhe.domain.dto.BatchServingDeleteDTO;
import org.dubhe.domain.dto.BatchServingDetailDTO;
import org.dubhe.domain.dto.BatchServingQueryDTO;
import org.dubhe.domain.dto.BatchServingStartDTO;
import org.dubhe.domain.dto.BatchServingStopDTO;
import org.dubhe.domain.dto.BatchServingUpdateDTO;
import org.dubhe.service.BatchServingService;
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
 * @description 批量服务管理
 * @date 2020-08-27
 */
@Api(tags = "云端Serving：批量服务管理")
@RestController
@RequestMapping("/api/batchServing")
public class BatchServingController {

    @Resource
    private BatchServingService batchServingService;

    @ApiOperation("查询批量服务")
    @GetMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody query(BatchServingQueryDTO batchServingQueryDTO) {
        return new DataResponseBody(batchServingService.query(batchServingQueryDTO));
    }

    @ApiOperation("创建批量服务")
    @PostMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody create(@Validated @RequestBody BatchServingCreateDTO batchServingCreateDTO) {
        return new DataResponseBody(batchServingService.create(batchServingCreateDTO));
    }

    @ApiOperation("修改批量服务")
    @PutMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody update(@Validated @RequestBody BatchServingUpdateDTO batchServingUpdateDTO) {
        return new DataResponseBody(batchServingService.update(batchServingUpdateDTO));
    }

    @ApiOperation("删除批量服务")
    @DeleteMapping
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody delete(@Validated @RequestBody BatchServingDeleteDTO batchServingDeleteDTO) {
        return new DataResponseBody(batchServingService.delete(batchServingDeleteDTO));
    }

    @ApiOperation("启动批量服务")
    @PostMapping("/start")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody start(@Validated @RequestBody BatchServingStartDTO batchServingStartDTO) {
        return new DataResponseBody(batchServingService.start(batchServingStartDTO));
    }

    @ApiOperation("停止批量服务")
    @PostMapping("/stop")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody stop(@Validated @RequestBody BatchServingStopDTO batchServingStopDTO) {
        return new DataResponseBody(batchServingService.stop(batchServingStopDTO));
    }

    @ApiOperation("获取批量服务详情")
    @GetMapping("/detail")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getDetail(@Validated BatchServingDetailDTO batchServingDetailDTO) {
        return new DataResponseBody(batchServingService.getDetail(batchServingDetailDTO));
    }

    @GetMapping("/pod/{id}")
    @ApiOperation("获取批量服务下pod信息")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody getServingLog(@PathVariable Long id) {
        return new DataResponseBody(batchServingService.getPods(id));
    }


    @GetMapping("/queryById/{id}")
    @ApiOperation("获取批量服务状态及进度")
    @RequiresPermissions(Permissions.SERVING_DEPLOYMENT)
    public DataResponseBody queryStatusAndProgress(@PathVariable Long id) {
        return new DataResponseBody(batchServingService.queryStatusAndProgress(id));
    }
}
