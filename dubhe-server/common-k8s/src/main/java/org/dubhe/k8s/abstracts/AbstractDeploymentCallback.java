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

package org.dubhe.k8s.abstracts;

import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.domain.dto.BaseK8sDeploymentCallbackCreateDTO;
import org.dubhe.k8s.service.DeploymentCallbackAsyncService;
import org.dubhe.k8s.utils.K8sCallBackTool;

import javax.annotation.Resource;

/**
 * @description 公共 deployment回调
 * @date 2020-11-27
 */
public abstract class AbstractDeploymentCallback implements DeploymentCallbackAsyncService {

    @Resource
    private K8sCallBackTool k8sCallBackTool;

    /**
     * 公共 失败重试策略
     *
     * @param k8sDeploymentCallbackCreateDTO
     * @param <R>
     */
    @Override
    public <R extends BaseK8sDeploymentCallbackCreateDTO> void deploymentCallBack(R k8sDeploymentCallbackCreateDTO) {
        int tryTime = 1;
        while (!doCallback(tryTime, k8sDeploymentCallbackCreateDTO)) {
            if (k8sCallBackTool.continueRetry(++tryTime)) {
                // 继续重试 tryTime重试次数+1
                try {
                    Thread.sleep(tryTime * NumberConstant.NUMBER_1000);
                } catch (InterruptedException e) {
                    LogUtil.error(LogEnum.NOTE_BOOK, "AbstractDeploymentCallback deploymentCallBack InterruptedException : {}", e);
                    // Restore interrupted state...      
                    Thread.currentThread().interrupt();
                }
            } else {
                // 重试超限 tryTime重试次数+1未尝试，因此需要tryTime重试次数-1
                callbackFailed(--tryTime, k8sDeploymentCallbackCreateDTO);
                break;
            }
        }
    }


    /**
     * deployment 异步回调具体实现处理类
     *
     * @param times                          第n次处理
     * @param k8sDeploymentCallbackCreateDTO k8s回调实体类
     * @param <R>                            BaseK8sDeploymentCallbackReq     k8s回调基类
     * @return true：处理成功    false：处理失败
     */
    public abstract <R extends BaseK8sDeploymentCallbackCreateDTO> boolean doCallback(int times, R k8sDeploymentCallbackCreateDTO);


    /**
     * deployment 异步回调具体实现处理类
     *
     * @param retryTimes                     总处理次数
     * @param k8sDeploymentCallbackCreateDTO k8s回调实体类
     * @param <R>                            BaseK8sDeploymentCallbackReq     k8s回调基类
     */
    public abstract <R extends BaseK8sDeploymentCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sDeploymentCallbackCreateDTO);
}
