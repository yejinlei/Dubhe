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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 线程工厂
 * @date 2020-04-10
 */
public class DefaultThreadFactoryImpl implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean isDaemon;
    private final long stackSize;
    private final Thread.UncaughtExceptionHandler exceptionHandler = new ThrealPoolExceptionHandler();

    /**
     * @param poolName  自定义的线程池名称
     * @param isDaemon  是否是守护线程
     * @param stackSize 新线程所需的堆栈大小，或者为零以指示要忽略此参数
     */
    public DefaultThreadFactoryImpl(String poolName, boolean isDaemon, long stackSize) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" + poolName + POOL_NUMBER.getAndIncrement() + "-thread-";
        this.isDaemon = isDaemon;
        this.stackSize = stackSize;

    }

    public DefaultThreadFactoryImpl() {
        this("default", false, 0);
    }

    /**
     *  创建新线程
     * @param runnable 线程接口实现类
     * @return
     */
    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), stackSize);
        t.setDaemon(isDaemon);
        t.setUncaughtExceptionHandler(exceptionHandler);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}
