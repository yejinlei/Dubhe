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

package org.dubhe.optimize.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.dto.PtModelStatusQueryDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.optimize.domain.dto.*;
import org.dubhe.optimize.service.ModelOptTaskInstanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @description 模型优化任务实例
 * @date 2020-05-22
 */
@Api(tags = "模型优化：任务实例")
@RestController
@RequestMapping("/taskInstance")
public class ModelOptTaskInstanceController {

    @Resource
    private ModelOptTaskInstanceService modelOptTaskInstanceService;

    @ApiOperation("分页查询任务执行记录实例列表")
    @GetMapping
    @PreAuthorize(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody query(ModelOptTaskInstanceQueryDTO instanceQueryDTO) {
        return DataResponseFactory.success(modelOptTaskInstanceService.queryAll(instanceQueryDTO));
    }

    @ApiOperation("重新提交任务实例")
    @PostMapping(value = "/resubmit")
    @PreAuthorize(Permissions.MODEL_OPTIMIZE_SUBMIT_TASK_INSTANCE)
    public DataResponseBody resubmit(@Validated @RequestBody ModelOptTaskInstanceResubmitDTO resubmitDTO) {
        modelOptTaskInstanceService.resubmit(resubmitDTO);
        return DataResponseFactory.success();
    }

    @ApiOperation("取消模型优化任务实例")
    @PutMapping(value = "/cancel")
    @PreAuthorize(Permissions.MODEL_OPTIMIZE_CANCEL_TASK_INSTANCE)
    public DataResponseBody cancel(@Validated @RequestBody ModelOptTaskInstanceCancelDTO cancelDTO) {
        modelOptTaskInstanceService.cancel(cancelDTO);
        return DataResponseFactory.success();
    }

    @ApiOperation("查看任务实例详情")
    @GetMapping(value = "/detail")
    @PreAuthorize(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getInstDetail(@Validated ModelOptTaskInstanceDetailDTO detailDTO) {
        return DataResponseFactory.success(modelOptTaskInstanceService.getInstDetail(detailDTO));
    }

    @ApiOperation("删除任务实例")
    @DeleteMapping
    @PreAuthorize(Permissions.MODEL_OPTIMIZE_DELETE_TASK_INSTANCE)
    public DataResponseBody delete(@Validated @RequestBody ModelOptTaskInstanceDeleteDTO modelOptTaskInstanceDeleteDTO) {
        modelOptTaskInstanceService.delete(modelOptTaskInstanceDeleteDTO);
        return DataResponseFactory.success();
    }

    @GetMapping("/getModelStatus")
    @ApiOperation("查询该模型是否正在使用（模型模块远程调用）")
    @PreAuthorize(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody<Boolean> getOptimizeModelStatus(@Validated PtModelStatusQueryDTO ptModelStatusQueryDTO) {
        return new DataResponseBody(modelOptTaskInstanceService.getOptimizeModelStatus(ptModelStatusQueryDTO));
    }

}
