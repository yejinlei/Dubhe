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

package org.dubhe.recycle.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;
import org.dubhe.recycle.service.CustomRecycleService;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @description 资源回收远程调用处理类
 * @date 2021-01-21
 */
@Api(tags = "通用:资源回收远程调用")
@RestController
@RequestMapping(RecycleTool.RECYCLE_CALL_PATH)
public class RecycleCallController {


    @PostMapping(value = RecycleTool.BIZ_RECYCLE)
    @ApiOperation("资源回收远程调用统一入口")
    public DataResponseBody recycle(@ApiParam(type = "head") @RequestHeader(name= RecycleTool.RECYCLE_TOKEN) String token
            , @Validated @RequestBody RecycleCreateDTO dto) {
        CustomRecycleService customRecycleService = SpringContextHolder.getBean(dto.getRecycleCustom());
        if (customRecycleService == null){
            return DataResponseFactory.failed("本服务未实现自定义资源删除！");
        }else {
            // 因作业量大，异步执行
            customRecycleService.recycle(dto);
            return DataResponseFactory.successWithMsg("资源删除正在异步处理中。");
        }
    }

    @PostMapping(value = RecycleTool.BIZ_RESTORE)
    @ApiOperation("资源还原远程调用统一入口")
    public DataResponseBody restore(@ApiParam(type = "head") @RequestHeader(name= RecycleTool.RECYCLE_TOKEN) String token
            , @Validated @RequestBody RecycleCreateDTO dto) {
        CustomRecycleService customRecycleService = SpringContextHolder.getBean(dto.getRestoreCustom());
        if (customRecycleService == null){
            return DataResponseFactory.failed("本服务未实现自定义资源还原！");
        }else {
            // 同步执行
            customRecycleService.restore(dto);
            return DataResponseFactory.successWithMsg("还原成功！");
        }
    }

}
