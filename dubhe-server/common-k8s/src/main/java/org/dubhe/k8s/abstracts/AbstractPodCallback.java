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


import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.dubhe.k8s.service.PodCallbackAsyncService;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description pod 异步回调抽象处理类
 * @date 2020-05-29
 */
public abstract class AbstractPodCallback implements PodCallbackAsyncService {

    @Autowired
    private K8sCallBackTool k8sCallBackTool;

    /**
     * 公共 失败重试策略
     *
     * @param k8sPodCallbackCreateDTO
     * @param <R>
     */
    @Override
    public <R extends BaseK8sPodCallbackCreateDTO> void podCallBack(R k8sPodCallbackCreateDTO) {
        int tryTime = 1;
        while (!doCallback(tryTime, k8sPodCallbackCreateDTO)) {
            if (k8sCallBackTool.continueRetry(++tryTime)) {
                // 继续重试 tryTime重试次数+1
                try {
                    Thread.sleep(tryTime * 1000);
                    continue;
                } catch (InterruptedException e) {
                    LogUtil.error(LogEnum.NOTE_BOOK, "AbstractPodCallback podCallBack InterruptedException : {}", e);
                    // Restore interrupted state...      
                    Thread.currentThread().interrupt();
                }
            } else {
                // 重试超限 tryTime重试次数+1未尝试，因此需要tryTime重试次数-1
                callbackFailed(--tryTime, k8sPodCallbackCreateDTO);
                break;
            }
        }
    }

    /**
     * pod 异步回调具体实现处理类
     * @param times                    第n次处理
     * @param k8sPodCallbackCreateDTO             k8s回调实体类
     * @param <R> BaseK8sPodCallbackReq     k8s回调基类
     * @return true：处理成功    false：处理失败
     */
    public abstract <R extends BaseK8sPodCallbackCreateDTO> boolean doCallback(int times, R k8sPodCallbackCreateDTO);


    /**
     * pod 异步回调具体实现处理类
     * @param retryTimes                    总处理次数
     * @param k8sPodCallbackCreateDTO             k8s回调实体类
     * @param <R> BaseK8sPodCallbackReq     k8s回调基类
     */
    public abstract <R extends BaseK8sPodCallbackCreateDTO> void callbackFailed(int retryTimes, R k8sPodCallbackCreateDTO);

}
