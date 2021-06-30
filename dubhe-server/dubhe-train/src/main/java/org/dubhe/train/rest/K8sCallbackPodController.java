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

package org.dubhe.train.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.dubhe.train.domain.dto.callback.AlgorithmK8sPodCallbackCreateDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @description k8s Pod 异步回调处理类
 * @date 2020-05-28
 */
@Api(tags = "k8s回调：Pod")
@RestController
@RequestMapping(StringConstant.K8S_CALLBACK_URI)
public class K8sCallbackPodController {


    @Resource(name = "algorithmAsyncServiceImpl")
    private PodCallbackAsyncService algorithmAsyncServiceImpl;

    /**
     * train训练异步回调
     *
     * @param k8sToken
     * @param k8sPodCallbackReq
     * @return
     */
    @PostMapping(value = "/algorithm")
    @ApiOperation("train pod 回调")
    public DataResponseBody algorithmPodCallBack(@ApiParam(type = "head") @RequestHeader(name = K8sCallBackTool.K8S_CALLBACK_TOKEN) String k8sToken
            , @Validated @RequestBody AlgorithmK8sPodCallbackCreateDTO k8sPodCallbackReq) {
        algorithmAsyncServiceImpl.podCallBack(k8sPodCallbackReq);
        return DataResponseFactory.success("训练管理异步回调处理中");
    }
}
