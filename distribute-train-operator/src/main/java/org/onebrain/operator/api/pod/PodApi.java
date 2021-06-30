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

import cn.hutool.core.util.StrUtil;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.onebrain.operator.context.KubeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @description PodApi 操作pod 里的容器用于上传文件等操作吧
 * @date 2020-09-23
 */
@Component
@Slf4j
public class PodApi {

    private static final Integer DEFAULT_LOG_LINES = 50;

    @Autowired
    private KubeContext kubeContext;

    @Autowired
    private KubernetesClient client;
    /**
     * 从Pod下载单个文件
     * @return File 临时文件，用后需要及时清理
     * **/
    public File copyFileFromPod(String namespace, String podName, String containerName, String filePath){
        try {
            File tmpFile = File.createTempFile("copy-from-pod-", "");
            client.pods().inNamespace(namespace).withName(podName)
                    .inContainer(containerName)
                    .file(filePath)
                    .copy(tmpFile.toPath());

            if(tmpFile.length() == 0){
                return null;
            }

            return tmpFile;
        } catch (IOException e) {

            log.error(" File copy error : 【{}】",e);
        }
        return null;
    }

    /**
     * 从Pod下载目录
     * @return File 临时文件，用后需要及时清理
     * **/
    public File copyFolderFromPod(String namespace, String podName, String containerName, String folderPath){
        final PipedInputStream stdoutInput = new PipedInputStream();
        final PipedOutputStream stdoutOutput = new PipedOutputStream();
        final PipedInputStream stderrInput = new PipedInputStream();
        final PipedOutputStream stderrOutput = new PipedOutputStream();
        final AtomicBoolean failed = new AtomicBoolean(false);
        try {
            stdoutInput.connect(stdoutOutput);
            stderrInput.connect(stderrOutput);

            //去除路径上的/前缀
            if(folderPath.startsWith(StrUtil.SLASH)){
                folderPath = StrUtil.removePrefix(folderPath, StrUtil.SLASH);
            }

            //监听器异步执行
            DefaultPodExecListener defaultPodExecListener = new DefaultPodExecListener(podName, namespace, containerName, null);

            StdPodExecListener stdPodExecListener = new StdPodExecListener(defaultPodExecListener, stdoutOutput, stderrOutput, failed);

            ExecWatch watch = client.pods().inNamespace(namespace)
                    .withName(podName).inContainer(containerName)
                    .writingOutput(stdoutOutput).writingError(stderrOutput)
                    .usingListener(stdPodExecListener)
                    .exec("tar", "cf", "-", "-C", folderPath, ".");
            // execLatch.await();

        } catch (IOException e) {
            log.error("copyFolderFromPod:【{}】",e);
        }

        File tmpFile = null;

        try {
            tmpFile = File.createTempFile("copy-from-pod-", ".tar");

            int length;
            byte[] buffer = new byte[1024];
            while (!Thread.currentThread().isInterrupted()
                    && (length = stdoutInput.read(buffer)) != -1) {

                byte[] content = new byte[length];
                System.arraycopy(buffer, 0, content, 0, length);

                FileUtils.writeByteArrayToFile(tmpFile, content, true);
            }

            while (!Thread.currentThread().isInterrupted()
                    && (length = stderrInput.read(buffer)) != -1) {
                log.error(new String(buffer, 0, length));
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                log.error("Error while pumping stream. 【{}】", e);
            } else {
                log.error("Interrupted while pumping stream. 【{}】", e);
            }
        }

        return tmpFile;
    }

    /**
     * 拷贝文件到pod
     * @param namespace 命名空间
     * @param podName pod名称
     * @param containerName 容器名称
     * @param file 文件
     * @param targetDir 目标路径
     */
    public void copyToPod(String namespace, String podName, String containerName, File file, String targetDir){
        client.pods().inNamespace(namespace).withName(podName)
                .inContainer(containerName)
                .file(targetDir)
                .upload(file.toPath());
    }

    /**
     * 同步执行
     * @param namespace 命名空间
     * @param podName pod名称
     * @param containerName 容器名称
     * @param cmd 命令
     */
    public void exec(String namespace, String podName, String containerName, String cmd){
        try {
            final CountDownLatch execLatch = new CountDownLatch(1);
            ExecWatch execWatch = client.pods().inNamespace(namespace).withName(podName).inContainer(containerName)
                    .redirectingOutput()
                    .withTTY() //不展示输出
                    .usingListener(new DefaultPodExecListener(namespace, podName, containerName, execLatch))
                    .exec("sh", "-c", cmd);
            execLatch.await();
        } catch (InterruptedException e) {
            log.error(" PodApi execute cmd error : 【{}】",e);
        }
    }
}
