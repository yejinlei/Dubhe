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

package org.dubhe.k8s;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import org.dubhe.AppRun;
import org.dubhe.k8s.enums.WatcherActionEnum;
import org.dubhe.k8s.event.callback.WatcherCallback;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description WatcherCallbackTest测试类
 * @date 2020-6-3
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= AppRun.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WatcherCallbackTest {
    @Autowired
    private WatcherCallback watcherCallback;

    /**
     * 连接k8s master服务器
     *
     * @return
     */
    public static KubernetesClient connectK8s() {
        String namespace = "local";
        String master = "http://xxx.xxx.xxx.xxx:8080";
        KubernetesClient client = null;
        Config config = new ConfigBuilder().withMasterUrl(master)
                .withTrustCerts(true)
                .withNamespace(namespace).build();
        try {
            client = new DefaultKubernetesClient(config);

        } catch (Exception e) {
        }
        return client;
    }

    @Test
    public void watch(){
        KubernetesClient client = connectK8s();
        try {
            client.pods().inNamespace("watch").watch(new Watcher<Pod>() {
                @Override
                public void eventReceived(Action action, Pod pod) {
                    System.out.println("action = "+action.name());
                    System.out.println("pod = "+ JSON.toJSONString(pod));
                    watcherCallback.podCallback(WatcherActionEnum.get(action.name()), BizConvertUtils.toBizPod(pod));
                }
                @Override
                public void onClose(KubernetesClientException e) {
                }
            });

            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

}
