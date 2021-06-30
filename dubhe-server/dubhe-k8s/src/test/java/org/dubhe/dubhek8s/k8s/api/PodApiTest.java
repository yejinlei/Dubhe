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

package org.dubhe.dubhek8s.k8s.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.Pod;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.domain.bo.LabelBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description PodApiTest测试类
 * @date 2020-04-14
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class PodApiTest {

    @Resource
    private PodApi podApi;

    @Test
    public void list(){
        /**得到所有命名空间下的pod**/
        List<BizPod> listAll = podApi.listAll();
        Long startTime = System.currentTimeMillis();
        listAll.forEach(o-> System.out.println(JSON.toJSONString(o)));
        Long endTime = System.currentTimeMillis();
        System.out.println("endTime - startTime = "+(endTime - startTime));
        System.out.println("------");

        /**得到指定命名空间下的所有pod**/
        List<Pod> list = podApi.list("kube-system");
        list.forEach(System.out::println);
        System.out.println("------");

        /**得到指定命名空间下，标签是xxx的pod**/
        LabelBO labelBO = new LabelBO("version", "v1");
        List<Pod> listWithLabel = podApi.list("default", labelBO);
        listWithLabel.forEach(System.out::println);
        System.out.println("------");

        /**通过多个label筛选**/
        List<Pod> listWithLabels = podApi.list("default", ImmutableSet.of(
                new LabelBO("version", "v1"),
                new LabelBO("app", "myapp")
        ));
        listWithLabels.forEach(System.out::println);
    }

    @Test
    public void get() {
        BizPod pod = podApi.get("namespace-1", "pod1");
        System.out.println(JSON.toJSONString(pod));
    }

    @Test
    public void getWithResourceName() {
        BizPod pod = podApi.getWithResourceName("namespace-1", "resourcename");
        System.out.println(JSON.toJSONString(pod));
    }

    @Test
    public void getWithNamespace() {
        List<BizPod> podList = podApi.getWithNamespace("namespace-1");
        System.out.println(JSON.toJSONString(podList));
    }

    @Test
    public void transform(){
        BizPod pod = podApi.get("default","my-nginx");
        System.out.println(pod);
    }

    @Test
    public void getToken(){
        String map = podApi.getToken("notebook-namespace-1","tensor-notebook-resource-name-my-notebook3-v78hd9ok-0");
        System.out.println(JSON.toJSONString(map));
    }

    @Test
    public void getTokenByResourceName(){
        String url = podApi.getTokenByResourceName("namespace-1","fcser");
        System.out.println(JSON.toJSONString(url));
    }

    @Test
    public void getUrlByResourceName(){
        String url = podApi.getUrlByResourceName("namespace-18","notebook-rn-20200701160849303ha0n");
        System.out.println(JSON.toJSONString(url));
    }

    @Test
    public void listAllRuningPodGroupByNodeName(){
        System.out.println(JSON.toJSONString(podApi.listAllRuningPodGroupByNodeName()));
    }
    @Test
    public void findByDtName(){
        List<BizPod> bizPodList = podApi.findByDtName("sun");
        System.out.println(bizPodList);
    }
}
