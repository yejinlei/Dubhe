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

package org.onebrain.operator.watcher;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.context.KubeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description 监视器的管理器
 * @date 2020-09-24
 */
@Slf4j
@Component
public class KubeWatcherManager {

    /**
     * 监视队列
     */
    private static final LinkedBlockingQueue<JobWatcher> watchQueue = new LinkedBlockingQueue<>(1000);

    /**
     * 单例线程池
     */
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new ThreadFactory() {
        private final AtomicInteger mThreadNum = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "job-watcher-" + mThreadNum.getAndIncrement());
        }
    });

    @Autowired
    private KubeContext kubeContext;

    @Autowired
    private JobHandler jobHandler;

    /**
     * 第一次启动时
     */
    public void startWatching(){
        JobWatchHolder jobWatchHolder = new JobWatchHolder();
        pool.execute(jobWatchHolder);
        putNewWatcher();
    }

    /**
     * 监听指定job
     * @param jobWatcher
     */
    public void watch(JobWatcher jobWatcher){
        KubernetesClient client = kubeContext.getClient();
        //监听指定job
        client.batch().jobs()
                .inAnyNamespace().watch(jobWatcher);
    }

    /**
     * 加入新watcher
     */
    public void putNewWatcher(){
        try {
            JobWatcher jobWatcher = new JobWatcher(jobHandler, this);
            watchQueue.put(jobWatcher);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Job监视器持有者
     */
    class JobWatchHolder implements Runnable {

        @Override
        public void run() {
            while(true){
                try {
                    //无监视器时阻塞
                    JobWatcher jobWatcher = watchQueue.take();

                    //启动监视器
                    try{
                        watch(jobWatcher);
                    }catch (Exception e){
                        //出错不影响其他listener
                        log.error("JobWatchHolder watch error:【{}】",e);
                    }

                } catch (InterruptedException e) {
                    log.error("JobWatchHolder run error:【{}】",e);
                }
            }
        }
    }
}
