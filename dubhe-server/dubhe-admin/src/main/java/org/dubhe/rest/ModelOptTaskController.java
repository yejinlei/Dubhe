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
import org.dubhe.domain.dto.ModelOptDatasetCreateDTO;
import org.dubhe.domain.dto.ModelOptTaskCreateDTO;
import org.dubhe.domain.dto.ModelOptTaskDeleteDTO;
import org.dubhe.domain.dto.ModelOptTaskQueryDTO;
import org.dubhe.domain.dto.ModelOptTaskSubmitDTO;
import org.dubhe.domain.dto.ModelOptTaskUpdateDTO;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.service.ModelOptTaskService;
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
 * @description 模型优化任务
 * @date 2020-05-22
 */
@Api(tags = "模型优化：任务")
@RestController
@RequestMapping("/api/modelOpt/task")
public class ModelOptTaskController {

    @Resource
    private ModelOptTaskService modelOptTaskService;

    @GetMapping
    @ApiOperation("任务列表分页查询")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getModelOptList(ModelOptTaskQueryDTO modelOptTaskQueryDTO) {
        return DataResponseFactory.success(modelOptTaskService.queryAll(modelOptTaskQueryDTO));
    }

    @PostMapping
    @ApiOperation("创建任务")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody create(@Validated @RequestBody ModelOptTaskCreateDTO modelOptTaskCreateDTO) {
        return DataResponseFactory.success(modelOptTaskService.create(modelOptTaskCreateDTO));
    }

    @PostMapping("/submit")
    @ApiOperation("提交任务，创建实例")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody submit(@Validated @RequestBody ModelOptTaskSubmitDTO submitDTO) {
        modelOptTaskService.submit(submitDTO);
        return DataResponseFactory.success();
    }

    @PutMapping
    @ApiOperation("编辑模型优化任务")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody update(@Validated @RequestBody ModelOptTaskUpdateDTO modelOptTaskUpdateDTO) {
        return DataResponseFactory.success(modelOptTaskService.update(modelOptTaskUpdateDTO));
    }

    @DeleteMapping
    @ApiOperation("删除模型优化任务")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody delete(@Validated @RequestBody ModelOptTaskDeleteDTO modelOptTaskDeleteDTO) {
        modelOptTaskService.delete(modelOptTaskDeleteDTO);
        return DataResponseFactory.success();
    }

    @GetMapping(value = "/getBuiltInModel")
    @ApiOperation("获取全部内置模型")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getBuiltInModel(Integer type, String dataset, String algorithm) {
        return DataResponseFactory.success(modelOptTaskService.getBuiltInModel(type, dataset, algorithm));
    }

    @GetMapping(value = "/getAlgorithm")
    @ApiOperation("获取模型优化算法")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getAlgorithm(Integer type, String model, String dataset) {
        return DataResponseFactory.success(modelOptTaskService.getAlgorithm(type, model, dataset));
    }

    @GetMapping(value = "/getDataset")
    @ApiOperation("获取内置模型优化数据集")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getDataset(Integer type, String model, String algorithm) {
        return DataResponseFactory.success(modelOptTaskService.getDataset(type, model, algorithm));
    }

    @GetMapping(value = "/MyDataset")
    @ApiOperation("获取我的模型优化数据集")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody getMyDataset() {
        return DataResponseFactory.success(modelOptTaskService.getMyDataset());
    }

    @PostMapping(value = "/MyDataset")
    @ApiOperation("创建我的模型优化数据集")
    @RequiresPermissions(Permissions.MODEL_OPTIMIZE)
    public DataResponseBody createMyDataset(@Validated @RequestBody ModelOptDatasetCreateDTO modelOptDatasetCreateDTO) {
        return DataResponseFactory.success(modelOptTaskService.createMyDataset(modelOptDatasetCreateDTO));
    }
}
