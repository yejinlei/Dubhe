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

package org.dubhe.data.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * @description 线程池
 * @date 2020-04-10
 */
@Component
public class BasePool {

    private ThreadPoolExecutor executorService;
    @Value("${basepool.corePoolSize:40}")
    private Integer corePoolSize;
    @Value("${basepool.maximumPoolSize:60}")
    private Integer maximumPoolSize;
    @Value("${basepool.keepAliveTime:120}")
    private Integer keepAliveTime;
    @Value("${basepool.blockQueueSize:10}")
    private Integer blockQueueSize;

    @PostConstruct
    public void init() {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(blockQueueSize);
        ThreadFactory threadFactory = new DefaultThreadFactoryImpl();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                workQueue, threadFactory, handler);
    }

    public ThreadPoolExecutor getExecutor() {
        return executorService;
    }

}
