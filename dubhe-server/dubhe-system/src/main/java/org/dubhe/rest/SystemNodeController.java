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
import org.dubhe.service.SystemNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @description 查询节点状态的controller层
 * @date 2020-06-03
 */
@Api(tags = "系统：节点状态管理")
@RestController
@RequestMapping("/api/{version}/node")
public class SystemNodeController {


    @Autowired
    private SystemNodeService systemNodeService;
    @ApiOperation("查询节点状态")
    @GetMapping(value = "findAllNode")
    @RequiresPermissions(Permissions.SYSTEM_NODE)
    public DataResponseBody findNodes(){
        return new DataResponseBody(systemNodeService.findNodes());
    }



}
