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

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @description Job监视器
 * @date 2020-09-24
 */
@Data
@Slf4j
public class JobWatcher implements Watcher<Job> {

    private String namespace;

    private String jobName;

    private KubeWatcherManager manager;

    private JobHandler jobHandler;

    public JobWatcher(JobHandler jobHandler, KubeWatcherManager manager) {
        this.manager = manager;
        this.jobHandler = jobHandler;
    }

    /**
     * 接收事件进行处理
     * @param action 事件类型
     * @param job job信息
     */
    @Override
    public void eventReceived(Action action, Job job) {
        log.info("Job Event received: {} action {}", job.getMetadata().getName(), action.toString());
        jobHandler.handleJob(job);
    }

    /**
     * 关闭事件
     * @param e 客户端异常
     */
    @Override
    public void onClose(KubernetesClientException e) {
        log.debug("job watcher close");
        if (e != null) {
            log.error(e.getMessage());
            log.info("restart new job watcher thread");
            manager.putNewWatcher();
        }
    }
}
