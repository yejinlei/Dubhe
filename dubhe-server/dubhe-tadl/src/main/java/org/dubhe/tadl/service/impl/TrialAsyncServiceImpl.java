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
package org.dubhe.tadl.service.impl;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.abstracts.AbstractPodCallback;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.tadl.domain.dto.TrialK8sPodCallBackCreateDTO;
import org.dubhe.tadl.service.TadlRedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description
 * @date 2021-08-04
 */
@Service(value = "trialAsyncService")
public class TrialAsyncServiceImpl extends AbstractPodCallback implements PodCallbackAsyncService {

    @Resource
    private TadlRedisService tadlRedisService;

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        // 强制转型
        TrialK8sPodCallBackCreateDTO req = (TrialK8sPodCallBackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.TADL, "Thread {} try {} time.Request: {}", Thread.currentThread(), times, req.toString());
        // trial回调
        return tadlRedisService.trialCallback(times, req);
    }

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        TrialK8sPodCallBackCreateDTO req = (TrialK8sPodCallBackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.TADL, "Thread {} try {} times FAILED! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
    }
}
