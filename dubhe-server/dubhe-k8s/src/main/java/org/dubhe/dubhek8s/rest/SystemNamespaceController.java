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

package org.dubhe.dubhek8s.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.constant.Permissions;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.dubhek8s.handler.WebSocketServer;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 查询命名空间状态的 controller 层
 * @date 2021-7-14
 */
@Api(tags = "系统：命名空间状态管理")
@RestController
@RequestMapping("/namespace")
public class SystemNamespaceController {
    @Autowired
    SystemNamespaceService systemNamespaceService;

    @Autowired
    WebSocketServer webSocketServer;

    @ApiOperation("查询命名空间资源信息")
    @GetMapping(value = "findNamespace")
    @PreAuthorize(Permissions.USER_RESOURCE_INFO)
    public DataResponseBody findNamespace(@RequestParam(value = "userId") Long userId){
        return new DataResponseBody(systemNamespaceService.findNamespace(userId));
    }
}
