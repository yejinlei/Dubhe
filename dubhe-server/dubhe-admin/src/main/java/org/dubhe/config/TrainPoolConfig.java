/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 线程池配置类
 * @date 2020-07-17
 */
@Configuration
public class TrainPoolConfig {

    @Value("${basepool.corePoolSize:40}")
    private Integer corePoolSize;
    @Value("${basepool.maximumPoolSize:60}")
    private Integer maximumPoolSize;
    @Value("${basepool.keepAliveTime:120}")
    private Integer keepAliveTime;
    @Value("${basepool.blockQueueSize:20}")
    private Integer blockQueueSize;

    /**
     * 训练任务异步处理线程池
     * @return Executor 线程实例
     */
    @Bean
    public Executor trainJobAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        //最大线程数
        taskExecutor.setMaxPoolSize(maximumPoolSize);
        //超时时间
        taskExecutor.setKeepAliveSeconds(keepAliveTime);
        //配置队列大小
        taskExecutor.setQueueCapacity(blockQueueSize);
        //配置线程池前缀
        taskExecutor.setThreadNamePrefix("async-train-job-");
        //拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}
