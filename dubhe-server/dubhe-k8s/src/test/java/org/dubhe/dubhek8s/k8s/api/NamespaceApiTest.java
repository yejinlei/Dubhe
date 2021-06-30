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
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.NamespaceApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @description NamespaceApiTest测试类
 * @date 2020-04-23
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class NamespaceApiTest {
    @Resource
    private NamespaceApi namespaceApi;

    @Test
    public void create() {
        System.out.println("BizNamespace = " + JSON.toJSONString(namespaceApi.create("namespace", null)));
    }

    @Test
    public void get() {
        System.out.println("get = " + JSON.toJSONString(namespaceApi.get("namespace")));
    }

    @Test
    public void listAll() {
        System.out.println("List<BizNamespace> = " + JSON.toJSONString(namespaceApi.listAll()));
    }

    @Test
    public void list() {
        System.out.println("List<BizNamespace> = " + JSON.toJSONString(namespaceApi.list("namespcae")));
        System.out.println("List<BizNamespace> = " + JSON.toJSONString(namespaceApi.list(new HashSet<String>() {
            {
                add("namespcae");
            }
        })));
    }

    @Test
    public void delete() {
        System.out.println("delete = " + JSON.toJSONString(namespaceApi.delete("namespcae")));
    }

    @Test
    public void removeLabel() {
        System.out.println("removeLabel = " + JSON.toJSONString(namespaceApi.removeLabel("namespcae", "label1")));
    }

    @Test
    public void removeLabels() {
        System.out.println("removeLabel = " + JSON.toJSONString(namespaceApi.removeLabels("namespcae", new HashSet<String>() {
            {
                add("label1");
                add("label2");
                add("label3");
            }
        })));
    }

    @Test
    public void addLabel() {
        System.out.println("removeLabel = " + JSON.toJSONString(namespaceApi.addLabel("namespcae", "label1", "label2")));
    }

    @Test
    public void addLabels() {
        System.out.println("removeLabel = " + JSON.toJSONString(namespaceApi.addLabels("namespcae", new HashMap<String, String>(5) {
            {
                put("label1", "label1");
                put("label2", "label2");
                put("label3", "label3");
            }
        })));
    }
}
