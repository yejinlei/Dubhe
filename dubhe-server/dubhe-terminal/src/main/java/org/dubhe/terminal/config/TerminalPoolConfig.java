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

package org.dubhe.terminal.config;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.decorator.AuthenticationThreadLocalTaskDecorator;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 线程池配置类
 * @date 2021-09-23
 */
@Configuration
public class TerminalPoolConfig implements AsyncConfigurer {
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
    @Bean("terminalExecutor")
    @Override
    public Executor getAsyncExecutor() {
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
        taskExecutor.setThreadNamePrefix("async-terminal-");
        //拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //为线程设置用户信息
        taskExecutor.setTaskDecorator(new AuthenticationThreadLocalTaskDecorator());
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            LogUtil.error(LogEnum.BIZ_TRAIN, "Async terminal task failed,the name of terminal is {}, params are {}, exception is {}", method.getName(), params, ex);
        };
    }
}
