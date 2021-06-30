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
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.dubhe.serving.domain.dto.BatchServingK8sPodCallbackCreateDTO;
import org.dubhe.serving.domain.dto.ServingK8sPodCallbackCreateDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description k8s Pod 异步回调处理类
 * @date 2021-01-25
 */
@Api(tags = "k8s回调：Pod")
@RestController
@RequestMapping(StringConstant.K8S_CALLBACK_URI)
public class K8sCallbackPodController {

    @Resource(name = "batchServingAsyncService")
    private PodCallbackAsyncService batchServingAsyncService;

    @Resource(name = "servingAsyncService")
    private PodCallbackAsyncService servingAsyncService;

    /**
     * 云端serving批量服务异步回调
     *
     * @param k8sToken
     * @param k8sPodCallbackReq
     * @return
     */
    @PostMapping(value = "/batchserving")
    @ApiOperation("云端serving 批量服务 pod 回调")
    public DataResponseBody batchServingPodCallBack(@ApiParam(type = "head") @RequestHeader(name = K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken,
                                               @Validated @RequestBody BatchServingK8sPodCallbackCreateDTO k8sPodCallbackReq) {
        batchServingAsyncService.podCallBack(k8sPodCallbackReq);
        return DataResponseFactory.success("云端serving批量服务异步回调处理中");
    }
    /**
     * 云端serving 服务pod异步回调
     *
     * @param k8sToken
     * @param k8sPodCallbackReq
     * @return
     */
    @PostMapping(value = "/serving")
    @ApiOperation("云端serving pod 回调")
    public DataResponseBody servingPodCallBack(@ApiParam(type = "head") @RequestHeader(name = K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken,
                                               @Validated @RequestBody ServingK8sPodCallbackCreateDTO k8sPodCallbackReq) {
        servingAsyncService.podCallBack(k8sPodCallbackReq);
        return DataResponseFactory.success("云端serving服务异步回调处理中");
    }
}
