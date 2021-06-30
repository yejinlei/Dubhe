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

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dubhe.biz.base.constant.MagicNumConstant.ONE_THOUSAND;

/**
 * @description HttpClient配置类
 * @date 2020-12-28
 */
@Configuration
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class HttpClientConfig {

    /**
     * 超时时间（秒）
     */
    private final static int TIMEOUT_SECOND = MagicNumConstant.FIVE;

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("RemoteCall HTTP连接回收管理器-%d").daemon(true).build());

    /**
     * 构建HttpClient
     * @return HttpClient
     */
    @Bean
    public CloseableHttpClient httpClient(){
        int processCount = Runtime.getRuntime().availableProcessors() * 2;
        return httpClientBuilder(processCount).build();
    }

    /**
     * 获取Http客户端生成器
     * @param processCount 当前硬件线程数*2
     * @return HttpClientBuilder
     */
    private HttpClientBuilder httpClientBuilder(int processCount){
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // Keep Alive 默认策略
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        // 连接管理器
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager(processCount));
        // Request请求配置
        httpClientBuilder.setDefaultRequestConfig(requestConfig());
        // 重试策略
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
        return httpClientBuilder;
    }

    /**
     * 获取Request请求配置
     * @return RequestConfig
     */
    private RequestConfig requestConfig(){
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // Socket超时时间（毫秒）
        requestConfigBuilder.setSocketTimeout(TIMEOUT_SECOND * ONE_THOUSAND);
        // 连接超时时间（毫秒）
        requestConfigBuilder.setConnectTimeout(TIMEOUT_SECOND * ONE_THOUSAND);
        // 从连接管理器中请求连接的超时时间（毫秒）
        requestConfigBuilder.setConnectionRequestTimeout(1 * ONE_THOUSAND);
        return requestConfigBuilder.build();
    }

    /**
     * 获取http连接管理器
     * @param processCount   当前硬件线程数*2
     * @return PoolingHttpClientConnectionManager
     */
    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager(int processCount){
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(1, TimeUnit.MINUTES);
        // 总连接数
        manager.setMaxTotal(processCount);
        // 同一个路由并发数控制
        manager.setDefaultMaxPerRoute(processCount/2);
        // 连接不活跃1秒后验证有效性
        manager.setValidateAfterInactivity(1 * ONE_THOUSAND);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LogUtil.debug(LogEnum.REMOTE_CALL,"PoolingHttpClientConnectionManager time to close connection.. ");
                // 关闭过期连接
                manager.closeExpiredConnections();
                // 关闭空闲5秒连接
                manager.closeIdleConnections(TIMEOUT_SECOND,TimeUnit.SECONDS);
            }
        },10,5, TimeUnit.SECONDS);
        return manager;
    }

}
