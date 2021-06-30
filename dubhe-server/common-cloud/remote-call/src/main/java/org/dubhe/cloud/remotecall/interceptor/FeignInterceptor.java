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
package org.dubhe.cloud.remotecall.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.dubhe.cloud.remotecall.config.RemoteCallConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @description Feign请求拦截器
 * @date 2020-11-19
 */
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    /**
     * 拦截feign请求，传递token
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (RequestContextHolder.getRequestAttributes() == null){
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        RemoteCallConfig.TOKEN_LIST.forEach(token -> {
            requestTemplate.header(token, request.getHeader(token));
        });
    }
}
