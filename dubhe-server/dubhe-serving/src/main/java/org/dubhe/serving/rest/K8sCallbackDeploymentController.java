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

package org.dubhe.serving.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.k8s.service.DeploymentCallbackAsyncService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.dubhe.serving.domain.dto.ServingK8sDeploymentCallbackCreateDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static org.dubhe.biz.base.constant.StringConstant.K8S_CALLBACK_PATH_DEPLOYMENT;

/**
 * @description k8s deployment回调
 * @date 2020-11-27
 */
@Api(tags = "k8s回调：deployment")
@RestController
@RequestMapping(K8S_CALLBACK_PATH_DEPLOYMENT)
public class K8sCallbackDeploymentController {

    @Resource(name = "servingDeploymentAsyncService")
    private DeploymentCallbackAsyncService servingDeploymentAsyncService;

    /**
     * 云端serving在线服务异步回调
     *
     * @param k8sToken
     * @param k8sDeploymentCallbackReq
     * @return
     */
    @PostMapping(value = "/serving")
    @ApiOperation("云端serving deployment 回调")
    public DataResponseBody servingPodCallBack(@ApiParam(type = "head") @RequestHeader(name = K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken,
                                               @Validated @RequestBody ServingK8sDeploymentCallbackCreateDTO k8sDeploymentCallbackReq) {
        servingDeploymentAsyncService.deploymentCallBack(k8sDeploymentCallbackReq);
        return DataResponseFactory.success("云端serving在线服务异步回调中");
    }

}
