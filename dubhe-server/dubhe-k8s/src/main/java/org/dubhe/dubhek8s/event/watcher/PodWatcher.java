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

package org.dubhe.dubhek8s.event.watcher;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.dubhek8s.event.callback.PodCallback;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @description Pod事件监听类
 * @date 2020-06-02
 */
@Component
public class PodWatcher implements CommandLineRunner, Watcher<Pod> {

    @Autowired
    private K8sUtils k8sUtils;

    /**
     * 重写SpringBoot事件监听类run方法
     *
     * @param args 可变参数
     * @return void
     */
    @Override
    public void run(String... args) {
        KubernetesClient client = k8sUtils.getClient();

        if (client == null) {
            LogUtil.info(LogEnum.BIZ_K8S, "can not get KubernetesClient,PodWatcher init failed");
            return;
        }
        try {
            client.pods().inAnyNamespace().watch(this);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodWatcher init failed, error {}", e.getMessage());
        }

    }

    /**
     * 事件监听回调
     *
     * @param action pod动作，参考PodPhaseEnum
     * @param pod pod对象
     */
    @Override
    public void eventReceived(Action action, Pod pod) {
        /**将监听到的Pod转换为BizPod**/
        BizPod bizPod = BizConvertUtils.toBizPod(pod);

        /**将Action 转换为atcherActionEnu**/
        WatcherActionEnum watcherActionEnum = WatcherActionEnum.get(action.name());

        LogUtil.info(LogEnum.BIZ_K8S, "received event pod {} action {}",pod.getMetadata().getName(),JSON.toJSONString(action));

        /**回调podCallback方法**/
        PodCallback podCallback = SpringContextHolder.getBean(PodCallback.class);
        podCallback.podCallback(watcherActionEnum, bizPod);
    }

    /**
     * 重写onClose()保证网络连接意外断开能够重连
     *
     * @param cause KubernetesClientException异常对象
     * @return void
     */
    @Override
    public void onClose(KubernetesClientException cause) {
        LogUtil.warn(LogEnum.BIZ_K8S," onClose=>cause : {}", cause.getMessage());
        k8sUtils.getClient().pods().inAnyNamespace().watch(this);
    }

}
