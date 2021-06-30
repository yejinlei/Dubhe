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
import org.dubhe.biz.base.annotation.ApiVersion;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.train.domain.dto.PtTrainLogQueryDTO;
import org.dubhe.train.domain.vo.PtTrainLogQueryVO;
import org.dubhe.train.service.PtTrainLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 训练日志 控制器入口
 * @date 2020-05-08
 */
@Api(tags = "训练：训练日志")
@RestController
@ApiVersion(1)
@RequestMapping("/trainLog")
public class PtTrainLogController {

    @Autowired
    private PtTrainLogService ptTrainLogService;

    @GetMapping
    @ApiOperation("训练日志查询")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getTrainLog(@Validated PtTrainLogQueryDTO ptTrainLogQueryDTO) {
        return new DataResponseBody(ptTrainLogService.queryTrainLog(ptTrainLogQueryDTO));
    }

    @GetMapping("/download")
    @ApiOperation("训练日志下载")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody downLoadTrainLog(@Validated PtTrainLogQueryDTO ptTrainLogQueryDTO) {
        ptTrainLogQueryDTO.setStartLine(MagicNumConstant.ONE).setLines(MagicNumConstant.MILLION);
        PtTrainLogQueryVO ptTrainLogQueryVO = ptTrainLogService.queryTrainLog(ptTrainLogQueryDTO);
        return new DataResponseBody(ptTrainLogService.getTrainLogString(ptTrainLogQueryVO.getContent()));
    }

    @GetMapping("/pod/{id}")
    @ApiOperation("获取pod节点")
    @PreAuthorize(Permissions.TRAINING_JOB)
    public DataResponseBody getPods(@PathVariable Long id) {
        return DataResponseFactory.success(ptTrainLogService.getPods(id));
    }


}
