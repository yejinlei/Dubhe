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

import javax.annotation.Resource;

import org.dubhe.base.DataResponseBody;
import org.dubhe.factory.DataResponseFactory;
import org.dubhe.dto.callback.AlgorithmK8sPodCallbackCreateDTO;
import org.dubhe.dto.callback.NotebookK8sPodCallbackCreateDTO;
import org.dubhe.service.PodCallbackAsyncService;
import org.dubhe.utils.K8sCallBackTool;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @description k8s Pod 异步回调处理类
 *
 * @date 2020-05-28
 */
@Api(tags = "k8s回调：Pod")
@RestController
@RequestMapping("/api/k8s/callback/pod")
public class K8sCallbackPodController {

    @Resource(name = "noteBookAsyncServiceImpl")
    private PodCallbackAsyncService noteBookAsyncService;
    @Resource(name = "algorithmAsyncServiceImpl")
    private PodCallbackAsyncService algorithmAsyncServiceImpl;

    /**
     * notebook异步回调
     *
     * @param k8sToken
     * @param k8sPodCallbackReq
     * @return
     */
    @PostMapping(value = "/notebook")
    @ApiOperation("模型管理 pod 回调")
    public DataResponseBody notebookPodCallBack(@ApiParam(type = "head") @RequestHeader(name= K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken
            ,@Validated @RequestBody NotebookK8sPodCallbackCreateDTO k8sPodCallbackReq) {
        noteBookAsyncService.podCallBack(k8sPodCallbackReq);
        return DataResponseFactory.success("notebook正在异步处理pod中。");
    }


    /**
     * algorithm异步回调
     *
     * @param k8sToken
     * @param k8sPodCallbackReq
     * @return
     */
    @PostMapping(value = "/algorithm")
    @ApiOperation("算法管理 pod 回调")
    public DataResponseBody notebookPodCallBack(@ApiParam(type = "head") @RequestHeader(name= K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken
            ,@Validated @RequestBody AlgorithmK8sPodCallbackCreateDTO k8sPodCallbackReq) {
        algorithmAsyncServiceImpl.podCallBack(k8sPodCallbackReq);
        return DataResponseFactory.success("算法管理异步回调处理方法中");
    }


}
