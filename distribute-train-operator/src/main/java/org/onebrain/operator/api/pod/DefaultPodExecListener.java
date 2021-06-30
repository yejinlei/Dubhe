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

package org.onebrain.operator.api.pod;

import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.concurrent.CountDownLatch;

/**
 * @description 默认命令执行监听器
 * @date 2020-09-23
 */
@Slf4j
@Getter
public class DefaultPodExecListener implements ExecListener {

    /**
     * pod名称
     */
    private String podName;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * 执行门栓 线程通信用
     */
    private CountDownLatch execLatch;

    public DefaultPodExecListener(String podName, String namespace, String containerName, CountDownLatch execLatch) {
        this.podName = podName;
        this.namespace = namespace;
        this.containerName = containerName;
        this.execLatch = execLatch;
    }

    @Override
    public void onOpen(Response response) {
        log.debug("shell environment in pod '{}', namespace '{}' is opened", podName, namespace);
        log.debug("onOpen: {}", response);
    }

    @Override
    public void onFailure(Throwable t, Response response) {
        log.error("shell environment in pod '{}', namespace '{}' barfed", podName, namespace);
        log.error("onFailure: {} {}", t.getMessage(), response);
        if (execLatch != null) {
            execLatch.countDown();
        }
    }

    @Override
    public void onClose(int code, String reason) {
        log.debug("shell environment in pod '{}', namespace '{}' closed", podName, namespace);
        log.debug("onClose: {} {}", code, reason);
        if (execLatch != null) {
            execLatch.countDown();
        }
    }
}
