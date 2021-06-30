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
package org.dubhe.cloud.remotecall.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.dubhe.cloud.remotecall.interceptor.RestTemplateTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @description RestTemplate配置类
 * @date 2020-11-26
 */
@Configuration
@ConditionalOnMissingBean(RestTemplate.class)
public class RestTemplateConfig {

    @Autowired
    private CloseableHttpClient httpClient;

    /**
     * 负载均衡
     * @return RestTemplate
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(httpClient)
        );
        //拦截器统一添加token
        restTemplate.setInterceptors(
                Collections.singletonList(
                        new RestTemplateTokenInterceptor()
                )
        );
        return restTemplate;
    }

}
