/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
import org.dubhe.domain.dto.ModelOptTaskInstanceDeleteDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceCancelDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceDetailDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceQueryDTO;
import org.dubhe.domain.dto.ModelOptTaskInstanceResubmitDTO;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.service.ModelOptTaskInstanceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description 模型优化任务实例
 * @date 2020-05-22
 */
@Api(tags = "模型优化：任务实例")
@RestController
@RequestMapping("/api/modelOpt/taskInstance")
public class ModelOptTaskInstanceController {

    @Resource
    private ModelOptTaskInstanceService modelOptTaskInstanceService;

    @ApiOperation("分页查询任务执行记录实例列表")
    @GetMapping
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody query(ModelOptTaskInstanceQueryDTO instanceQueryDTO) {
        return DataResponseFactory.success(modelOptTaskInstanceService.queryAll(instanceQueryDTO));
    }

    @ApiOperation("重新提交任务实例")
    @PostMapping(value = "/resubmit")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody resubmit(@Validated @RequestBody ModelOptTaskInstanceResubmitDTO resubmitDTO) {
        modelOptTaskInstanceService.resubmit(resubmitDTO);
        return DataResponseFactory.success();
    }

    @ApiOperation("取消模型优化任务实例")
    @PutMapping(value = "/cancel")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody cancel(@Validated @RequestBody ModelOptTaskInstanceCancelDTO cancelDTO) {
        modelOptTaskInstanceService.cancel(cancelDTO);
        return DataResponseFactory.success();
    }

    @ApiOperation("查看任务实例详情")
    @GetMapping(value = "/detail")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getInstDetail(@Validated ModelOptTaskInstanceDetailDTO detailDTO) {
        return DataResponseFactory.success(modelOptTaskInstanceService.getInstDetail(detailDTO));
    }

    @ApiOperation("删除任务实例")
    @DeleteMapping
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody delete(@Validated @RequestBody ModelOptTaskInstanceDeleteDTO modelOptTaskInstanceDeleteDTO) {
        modelOptTaskInstanceService.delete(modelOptTaskInstanceDeleteDTO);
        return DataResponseFactory.success();
    }

}
