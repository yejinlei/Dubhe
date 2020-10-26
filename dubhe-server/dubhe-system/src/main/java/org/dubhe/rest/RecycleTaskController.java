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
import org.dubhe.annotation.ApiVersion;
import org.dubhe.base.DataResponseBody;
import org.dubhe.base.ResponseCode;
import org.dubhe.domain.dto.RecycleTaskQueryDTO;
import org.dubhe.service.RecycleTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description 回收任务
 * @date 2020-09-23
 */
@Api(tags = "回收任务")
@ApiVersion(1)
@RestController
@RequestMapping("/api/{version}/recycleTask")
public class RecycleTaskController {

    @Autowired
    private RecycleTaskService recycleTaskService;


    @ApiOperation("查询回收任务列表")
    @GetMapping
    public DataResponseBody getRecycleTaskList(RecycleTaskQueryDTO recycleTaskQueryDTO) {
        return new DataResponseBody(recycleTaskService.getRecycleTasks(recycleTaskQueryDTO));
    }


    @ApiOperation("实时执行回收任务")
    @DeleteMapping
    public DataResponseBody recycleTaskResources(@RequestParam(required = true) Long taskId) {
        recycleTaskService.recycleTaskResources(taskId);
        return new DataResponseBody(ResponseCode.SUCCESS, "回收任务执行成功");
    }


    @ApiOperation("实时删除完整路径无效文件")
    @DeleteMapping("/delTemp")
    public DataResponseBody delTempInvalidResources(@RequestParam(required = true) String sourcePath) {
        recycleTaskService.delTempInvalidResources(sourcePath);
        return new DataResponseBody(ResponseCode.SUCCESS, "删除临时目录文件成功");
    }

}
