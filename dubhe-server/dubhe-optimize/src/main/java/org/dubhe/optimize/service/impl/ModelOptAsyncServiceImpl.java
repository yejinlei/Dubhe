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

package org.dubhe.optimize.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.abstracts.AbstractPodCallback;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.optimize.domain.dto.callback.ModelOptK8sPodCallbackCreateDTO;
import org.dubhe.optimize.service.ModelOptTaskInstanceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description 模型优化-k8s回调处理类
 * @date 2020-06-05
 */
@Service(value = "modelOptAsyncServiceImpl")
@Slf4j
public class ModelOptAsyncServiceImpl extends AbstractPodCallback implements PodCallbackAsyncService {

    @Resource
    private ModelOptTaskInstanceService modelOptTaskInstanceService;

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO) {
        try {
            // 强制转型
            ModelOptK8sPodCallbackCreateDTO req = (ModelOptK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
            LogUtil.info(LogEnum.MODEL_OPT, "Thread {} try {} time.Request: {}", Thread.currentThread(), times, req.toString());
            return modelOptTaskInstanceService.modelOptCallBack(req);
        } catch (Exception e) {
            LogUtil.error(LogEnum.MODEL_OPT, "ModelOpt doCallback error!", e);
            return false;
        }
    }

    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO) {
        ModelOptK8sPodCallbackCreateDTO req = (ModelOptK8sPodCallbackCreateDTO) k8sPodCallbackCreateDTO;
        LogUtil.info(LogEnum.MODEL_OPT, "Thread {} try {} times FAILED! if you want to storage or send failed msg,please impl this.. Request: {}", Thread.currentThread(), retryTimes, req.toString());
    }

}
