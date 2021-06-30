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
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description 标准pod执行监听器
 * @date 2020-09-23
 */
@Slf4j
public class StdPodExecListener implements ExecListener {

    private ExecListener defaultExecListener;

    private PipedOutputStream stdoutOutput;

    private PipedOutputStream stderrOutput;

    private AtomicBoolean failed;

    public StdPodExecListener(ExecListener defaultExecListener, PipedOutputStream stdoutOutput, PipedOutputStream stderrOutput, AtomicBoolean failed) {
        this.defaultExecListener = defaultExecListener;
        this.stdoutOutput = stdoutOutput;
        this.stderrOutput = stderrOutput;
        this.failed = failed;
    }

    @Override
    public void onOpen(Response response) {
        log.info("onOpen=>response : 【{}】",response);
        defaultExecListener.onOpen(response);
    }

    @Override
    public void onFailure(Throwable t, Response response) {
        log.info("onFailure=> t :【{}】,response : 【{}】",t,response);
        try {
            failed.set(true);
            stdoutOutput.close();
            stderrOutput.close();
        } catch (IOException e) {
            log.error("Failed to close stdout and stderr pipes. 【{}】", e);
        } finally {
            defaultExecListener.onFailure(t, response);
        }
    }

    @Override
    public void onClose(int code, String reason) {
        log.info("onClose=>code : 【{}】,reason : 【{}】",code,reason);
        try {
            stdoutOutput.close();
            stderrOutput.close();
        } catch (IOException e) {
            log.error("Failed to close stdout and stderr pipes. 【{}】", e);
        } finally {
            defaultExecListener.onClose(code, reason);
        }
    }

}
