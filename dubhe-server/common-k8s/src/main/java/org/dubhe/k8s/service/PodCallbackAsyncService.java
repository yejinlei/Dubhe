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

package org.dubhe.k8s.service;

import org.dubhe.k8s.domain.dto.BaseK8sPodCallbackCreateDTO;
import org.springframework.scheduling.annotation.Async;

/**
 * @description Pod 异步回调处理接口
 * @date 2020-05-28
 */
public interface PodCallbackAsyncService {

    /**
     * pod 异步回调
     * @param k8sPodCallbackCreateDTO
     */
    @Async
    <R extends BaseK8sPodCallbackCreateDTO> void  podCallBack (R k8sPodCallbackCreateDTO);
}
