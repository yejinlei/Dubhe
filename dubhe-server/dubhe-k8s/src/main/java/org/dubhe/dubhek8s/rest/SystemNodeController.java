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
import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.dubhe.dubhek8s.service.SystemNodeService;
import org.dubhe.k8s.domain.dto.NodeInfoDTO;
import org.dubhe.k8s.domain.dto.NodeIsolationDTO;
import org.dubhe.k8s.domain.entity.K8sNode;
import org.dubhe.k8s.domain.resource.BizNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @description 查询节点状态的controller层
 * @date 2020-06-03
 */
@Api(tags = "系统：节点状态管理")
@RestController
@RequestMapping("/node")
public class SystemNodeController {

    @Autowired
    private SystemNodeService systemNodeService;

    @ApiOperation("查询节点状态")
    @GetMapping(value = "findAllNode")
    @PreAuthorize(Permissions.SYSTEM_NODE)
    public DataResponseBody findNodes() {
        return new DataResponseBody(systemNodeService.findNodesIsolation());
    }


    @ApiOperation("查询用户k8s可用资源(admin远程调用)")
    @PostMapping(value = "queryUserResource")
    public DataResponseBody<List<QueryUserResourceSpecsVO>> queryUserK8sResource(@Validated @RequestBody List<QueryUserK8sResourceDTO> queryUserK8sResources) {
        return new DataResponseBody(systemNodeService.queryUserK8sResource(queryUserK8sResources));
    }

    @ApiOperation("添加资源隔离")
    @PostMapping(value = "isolation")
    @PreAuthorize(Permissions.SYSTEM_NODE)
    public DataResponseBody addNodeIisolation(@Validated @RequestBody NodeIsolationDTO nodeIsolationDTO, HttpServletResponse response) {
        String res = "";
        List<BizNode> bizNodes = systemNodeService.addNodeIisolation(nodeIsolationDTO);
        for (BizNode bizNode : bizNodes) {
            if (!bizNode.isSuccess()) {
                res += bizNode.getMessage();
            }
        }
        return new DataResponseBody(res);
    }

    @ApiOperation("删除资源隔离")
    @DeleteMapping(value = "isolation")
    @PreAuthorize(Permissions.SYSTEM_NODE)
    public DataResponseBody delNodeIisolation(@Validated @RequestBody NodeIsolationDTO nodeIsolationDTO, HttpServletResponse response) {
        String res = "";
        List<BizNode> bizNodes = systemNodeService.delNodeIisolation(nodeIsolationDTO);
        for (BizNode bizNode : bizNodes) {
            if (!bizNode.isSuccess()) {
                res += bizNode.getMessage();
            }
        }
        return new DataResponseBody(res);
    }

    @ApiOperation("编辑")
    @PostMapping(value = "edit")
    @PreAuthorize(Permissions.SYSTEM_NODE)
    public DataResponseBody edit(@Validated @RequestBody NodeInfoDTO nodeInfoDTO, HttpServletResponse response) {
        K8sNode k8sNode = systemNodeService.editNodeInfo(nodeInfoDTO);
        return new DataResponseBody();
    }

    @ApiOperation("资源统计")
    @GetMapping("findAllResource")
    @PreAuthorize(Permissions.SYSTEM_NODE)
    public DataResponseBody findAllResource() {
        return new DataResponseBody(systemNodeService.findAllResource());
    }
}
