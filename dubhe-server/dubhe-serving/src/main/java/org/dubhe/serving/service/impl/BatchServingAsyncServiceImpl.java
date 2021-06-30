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

package org.dubhe.serving.service.impl;


import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.abstracts.AbstractPodCallback;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.serving.domain.dto.BatchServingK8sPodCallbackCreateDTO;
import org.dubhe.serving.service.BatchServingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description 云端Serving批量服务回调
 * @date 2020-11-27
 */
@Service(value = "batchServingAsyncService")
public class BatchServingAsyncServiceImpl extends AbstractPodCallback implements PodCallbackAsyncService {

    @Resource
    private BatchServingService batchServingService;

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        // 强制转型
        BatchServingK8sPodCallbackCreateDTO req = (BatchServingK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.SERVING, "Thread {} try {} time.Request: {}", Thread.currentThread(), times, req.toString());
        //批量服务回调
        return batchServingService.batchServingCallback(times, req);
    }

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        BatchServingK8sPodCallbackCreateDTO req = (BatchServingK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.SERVING, "Thread {} try {} times FAILED! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
    }
}
