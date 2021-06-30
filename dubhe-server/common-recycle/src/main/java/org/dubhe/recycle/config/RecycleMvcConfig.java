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

package org.dubhe.recycle.config;


import org.dubhe.recycle.interceptor.RecycleCallInterceptor;
import org.dubhe.recycle.utils.RecycleTool;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @description 资源回收 Mvc Config
 * @date 2021-01-21
 */
@Configuration
public class RecycleMvcConfig implements WebMvcConfigurer {

    @Resource
    private RecycleCallInterceptor recycleCallInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(recycleCallInterceptor);
        // 拦截配置
        registration.addPathPatterns(RecycleTool.MATCH_RECYCLE_PATH);
    }

}
