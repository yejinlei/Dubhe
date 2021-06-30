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

package org.dubhe.dubhek8s.k8s;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.dubhek8s.event.callback.PodCallback;
import org.dubhe.k8s.enums.WatcherActionEnum;
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
@SpringBootTest(classes= DubheK8sApplication.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PodCallbackTest {
    @Autowired
    private PodCallback podCallback;

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
                    podCallback.podCallback(WatcherActionEnum.get(action.name()), BizConvertUtils.toBizPod(pod));
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

    @Test
    public void callback(){
        String podStr = "{\"additionalProperties\":{},\"apiVersion\":\"v1\",\"kind\":\"Pod\",\"metadata\":{\"additionalProperties\":{},\"annotations\":{\"cni.projectcalico.org/podIP\":\"172.16.29.81/32\"},\"creationTimestamp\":\"2020-09-28T07:20:31Z\",\"finalizers\":[],\"generateName\":\"serving-rn-173-wan40byz-64d88c69b4-\",\"labels\":{\"platform/business\":\"serving\",\"platform/create-by\":\"platform\",\"platform/p-kind\":\"Deployment\",\"platform/p-name\":\"serving-rn-173-wan40byz\",\"platform/resource-name\":\"serving-rn-173\",\"platform/runtime-env\":\"serving\",\"pod-template-hash\":\"64d88c69b4\"},\"managedFields\":[],\"name\":\"serving-rn-173-wan40byz-64d88c69b4-8zq4t\",\"namespace\":\"namespace-1\",\"ownerReferences\":[{\"additionalProperties\":{},\"apiVersion\":\"apps/v1\",\"blockOwnerDeletion\":true,\"controller\":true,\"kind\":\"ReplicaSet\",\"name\":\"serving-rn-173-wan40byz-64d88c69b4\",\"uid\":\"c8e7e4c2-c19f-4ee1-9dd7-7e32e258086c\"}],\"resourceVersion\":\"20702829\",\"selfLink\":\"/api/v1/namespaces/namespace-1/pods/serving-rn-173-wan40byz-64d88c69b4-8zq4t\",\"uid\":\"15a1f63c-5bd4-4c6f-90bd-5c984a3b3086\"},\"spec\":{\"additionalProperties\":{},\"containers\":[{\"additionalProperties\":{},\"args\":[\"-c\",\"python /usr/local/TS_Serving/serving/server.py --platform='tensorflow' --model_name='resnet50' --model_path='/usr/local/TS_Serving/models/resnet50' --log_dir='/usr/local/TS_Serving/logs/'\"],\"command\":[\"/bin/bash\"],\"env\":[],\"envFrom\":[],\"image\":\"harbor.dubhe.ai/serving/serving:v1.0\",\"imagePullPolicy\":\"IfNotPresent\",\"name\":\"serving-rn-173-wan40byz\",\"ports\":[{\"additionalProperties\":{},\"containerPort\":5000,\"name\":\"http\",\"protocol\":\"TCP\"}],\"resources\":{\"additionalProperties\":{},\"limits\":{\"cpu\":{\"additionalProperties\":{},\"amount\":\"1\",\"format\":\"\"},\"nvidia.com/gpu\":{\"additionalProperties\":{},\"amount\":\"1\",\"format\":\"\"}},\"requests\":{\"cpu\":{\"additionalProperties\":{},\"amount\":\"1\",\"format\":\"\"},\"nvidia.com/gpu\":{\"additionalProperties\":{},\"amount\":\"1\",\"format\":\"\"}}},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"volumeDevices\":[],\"volumeMounts\":[{\"additionalProperties\":{},\"mountPath\":\"/usr/local/TS_Serving/models/resnet50\",\"name\":\"volume-0\"},{\"additionalProperties\":{},\"mountPath\":\"/usr/local/TS_Serving/serving\",\"name\":\"volume-1\"},{\"additionalProperties\":{},\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\",\"name\":\"default-token-l7kbz\",\"readOnly\":true}]}],\"dnsPolicy\":\"ClusterFirst\",\"enableServiceLinks\":true,\"ephemeralContainers\":[],\"hostAliases\":[],\"imagePullSecrets\":[],\"initContainers\":[],\"nodeName\":\"qjy-ai05\",\"nodeSelector\":{\"gpu\":\"gpu\"},\"priority\":0,\"readinessGates\":[],\"restartPolicy\":\"Always\",\"schedulerName\":\"default-scheduler\",\"securityContext\":{\"additionalProperties\":{},\"supplementalGroups\":[],\"sysctls\":[]},\"serviceAccount\":\"default\",\"serviceAccountName\":\"default\",\"terminationGracePeriodSeconds\":30,\"tolerations\":[{\"additionalProperties\":{},\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/not-ready\",\"operator\":\"Exists\",\"tolerationSeconds\":300},{\"additionalProperties\":{},\"effect\":\"NoExecute\",\"key\":\"node.kubernetes.io/unreachable\",\"operator\":\"Exists\",\"tolerationSeconds\":300}],\"topologySpreadConstraints\":[],\"volumes\":[{\"additionalProperties\":{},\"name\":\"volume-0\",\"nfs\":{\"additionalProperties\":{},\"path\":\"/nfs/dubhe-dev/serving/models/tensorflow_models/resnet50/\",\"server\":\"127.0.0.1\"}},{\"additionalProperties\":{},\"name\":\"volume-1\",\"nfs\":{\"additionalProperties\":{},\"path\":\"/nfs/dubhe-dev/serving/TS_Serving\",\"server\":\"127.0.0.1\"}},{\"additionalProperties\":{},\"name\":\"default-token-l7kbz\",\"secret\":{\"additionalProperties\":{},\"defaultMode\":420,\"items\":[],\"secretName\":\"default-token-l7kbz\"}}]},\"status\":{\"additionalProperties\":{},\"conditions\":[{\"additionalProperties\":{},\"lastTransitionTime\":\"2020-09-28T07:20:02Z\",\"status\":\"True\",\"type\":\"Initialized\"},{\"additionalProperties\":{},\"lastTransitionTime\":\"2020-09-28T07:20:05Z\",\"status\":\"True\",\"type\":\"Ready\"},{\"additionalProperties\":{},\"lastTransitionTime\":\"2020-09-28T07:20:05Z\",\"status\":\"True\",\"type\":\"ContainersReady\"},{\"additionalProperties\":{},\"lastTransitionTime\":\"2020-09-28T07:20:31Z\",\"status\":\"True\",\"type\":\"PodScheduled\"}],\"containerStatuses\":[{\"additionalProperties\":{},\"containerID\":\"docker://777ee925f078ed62d77902117de2043ffd2323894c00d6ea4d9d5d0bee1fad50\",\"image\":\"harbor.dubhe.ai/serving/serving:v1.0\",\"imageID\":\"docker-pullable://harbor.dubhe.ai/serving/serving@sha256:f09d3b1a7e854582c33235d25569ef7ea6eff12446ce6bd8662147d68c6a88ee\",\"lastState\":{\"additionalProperties\":{}},\"name\":\"serving-rn-173-wan40byz\",\"ready\":true,\"restartCount\":0,\"started\":true,\"state\":{\"additionalProperties\":{},\"running\":{\"additionalProperties\":{},\"startedAt\":\"2020-09-28T07:20:04Z\"}}}],\"ephemeralContainerStatuses\":[],\"hostIP\":\"10.5.24.134\",\"initContainerStatuses\":[],\"phase\":\"Running\",\"podIP\":\"172.16.29.81\",\"podIPs\":[{\"additionalProperties\":{},\"ip\":\"172.16.29.81\"}],\"qosClass\":\"Burstable\",\"startTime\":\"2020-09-28T07:20:02Z\"}}";
        Pod pod = JSON.parseObject(podStr, Pod.class);
        WatcherActionEnum watcherActionEnum = WatcherActionEnum.get("ADDED");
        podCallback.podCallback(watcherActionEnum, BizConvertUtils.toBizPod(pod));
    }

}
