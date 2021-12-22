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
import org.dubhe.biz.base.dto.NamespaceDeleteDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotVO;
import org.dubhe.dubhek8s.handler.WebSocketServer;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.dubhe.k8s.api.MetricsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private MetricsApi metricsApi;

    @ApiOperation("查询命名空间资源信息")
    @GetMapping(value = "findNamespace")
    public DataResponseBody findNamespace(@RequestParam(value = "userId") Long userId) {
        return new DataResponseBody(systemNamespaceService.findNamespace(userId));
    }

    @ApiOperation("删除用户namespace-admin远程调用")
    @DeleteMapping
    public DataResponseBody deleteNamespace(@RequestBody NamespaceDeleteDTO namespaceDeleteDTO){
        systemNamespaceService.deleteNamespace(namespaceDeleteDTO);
        return new DataResponseBody();
    }

    @ApiOperation("查看用户资源用量峰值")
    @GetMapping("ResourceUsage")
    public DataResponseBody<List<UserAllotVO>> getResourceNamespace(@RequestParam(value = "resourceType") Integer resourceType,
                                                                    @RequestParam(value = "sumDay") String sumDay) {
        return new DataResponseBody(metricsApi.getNamespaceUsageRate(resourceType, sumDay));
    }

    @ApiOperation("查询用户某段时间内的资源用量峰值")
    @GetMapping("ResourceByUser")
    public DataResponseBody<Map<Long, String>> getResourceUsageByUser(@RequestParam(value = "resourceType") Integer resourceType,
                                                                      @RequestParam(value = "sumDay") String sumDay,
                                                                      @RequestParam(value = "namespaces") String namespaces) {
        return new DataResponseBody(metricsApi.getResourceUsageByUser(resourceType, sumDay,namespaces));
    }
}
