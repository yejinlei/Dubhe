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
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.constant.Permissions;
import org.dubhe.domain.dto.PtTrainParamCreateDTO;
import org.dubhe.domain.dto.PtTrainParamDeleteDTO;
import org.dubhe.domain.dto.PtTrainParamQueryDTO;
import org.dubhe.domain.dto.PtTrainParamUpdateDTO;
import org.dubhe.service.PtTrainParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 任务参数
 * @date 2020-04-27
 */
@Api(tags = "训练：任务参数管理")
@RestController
@ApiVersion(1)
@RequestMapping("/api/{version}/trainParams")
public class PtTrainParamController {

    @Autowired
    private PtTrainParamService ptTrainParamService;

    @GetMapping
    @ApiOperation("任务参数列表展示")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainParam(@Validated PtTrainParamQueryDTO ptTrainParamQueryDTO) {
        return new DataResponseBody(ptTrainParamService.getTrainParam(ptTrainParamQueryDTO));
    }

    @PostMapping
    @ApiOperation("保存任务参数")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody createTrainParam(@Validated @RequestBody PtTrainParamCreateDTO ptTrainParamCreateDTO) {
        return new DataResponseBody(ptTrainParamService.createTrainParam(ptTrainParamCreateDTO));
    }

    @PutMapping
    @ApiOperation("修改任务参数")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody updateTrainParam(@Validated @RequestBody PtTrainParamUpdateDTO ptTrainParamUpdateDTO) {
        return new DataResponseBody(ptTrainParamService.updateTrainParam(ptTrainParamUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除任务参数")
    @RequiresPermissions(Permissions.TRAINING_JOB)
    public DataResponseBody deleteTrainParam(@Validated @RequestBody PtTrainParamDeleteDTO ptTrainParamDeleteDTO) {
        ptTrainParamService.deleteTrainParam(ptTrainParamDeleteDTO);
        return new DataResponseBody();
    }

}
