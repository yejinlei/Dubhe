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

package org.dubhe.k8s.config;


import org.dubhe.k8s.interceptor.K8sCallBackPodInterceptor;
import org.dubhe.k8s.utils.K8sCallBackTool;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @description Web Mvc Config
 * @date 2020-05-28
 */
@Configuration
public class K8sCallbackMvcConfig implements WebMvcConfigurer {

    @Resource
    private K8sCallBackPodInterceptor k8sCallBackPodInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(k8sCallBackPodInterceptor);
        // 拦截配置
        registration.addPathPatterns(K8sCallBackTool.getK8sCallbackPaths());

    }



}
