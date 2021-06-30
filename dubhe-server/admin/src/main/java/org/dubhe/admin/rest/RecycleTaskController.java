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
package org.dubhe.admin.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.admin.service.RecycleTaskService;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.recycle.domain.dto.RecycleTaskDeleteDTO;
import org.dubhe.recycle.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.recycle.enums.RecycleModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 回收任务
 * @date 2020-09-23
 */
@Api(tags = "系统:回收任务")
@RestController
@RequestMapping("/recycleTask")
public class RecycleTaskController {

    @Autowired
    private RecycleTaskService recycleTaskService;

    @ApiOperation("查询回收任务列表")
    @GetMapping
    public DataResponseBody getRecycleTaskList(RecycleTaskQueryDTO recycleTaskQueryDTO) {
        return DataResponseFactory.success(recycleTaskService.getRecycleTasks(recycleTaskQueryDTO));
    }

    @ApiOperation("（批量）立即删除")
    @DeleteMapping
    public DataResponseBody recycleTaskResources(@Validated @RequestBody RecycleTaskDeleteDTO recycleTaskDeleteDTO) {
        for (long taskId:recycleTaskDeleteDTO.getRecycleTaskIdList()){
            recycleTaskService.recycleTaskResources(taskId);
        }
        return DataResponseFactory.successWithMsg("资源删除中");
    }

    @ApiOperation("获取模块代号，名称映射")
    @GetMapping("/recycleModuleMap")
    public DataResponseBody recycleModuleMap() {
        return DataResponseFactory.success(RecycleModuleEnum.RECYCLE_MODULE_MAP);
    }

    @ApiOperation("立即还原")
    @PutMapping
    public DataResponseBody restore(@RequestParam(required = true) long taskId) {
        recycleTaskService.restore(taskId);
        return DataResponseFactory.successWithMsg("还原成功");
    }


    @ApiOperation("实时删除完整路径无效文件")
    @DeleteMapping("/delTemp")
    public DataResponseBody delTempInvalidResources(@RequestParam(required = true) String sourcePath) {
        recycleTaskService.delTempInvalidResources(sourcePath);
        return DataResponseFactory.successWithMsg("删除临时目录文件成功");
    }

}
