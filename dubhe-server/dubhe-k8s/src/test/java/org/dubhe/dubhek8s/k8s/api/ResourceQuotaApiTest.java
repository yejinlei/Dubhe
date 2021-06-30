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
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.domain.bo.PtResourceQuotaBO;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizScopedResourceSelectorRequirement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @description ResourceQuotaApiTest测试类
 * @date 2020-04-23
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class ResourceQuotaApiTest {
    @Resource
    private ResourceQuotaApi resourceQuotaApi;

    @Test
    public void create() {
        PtResourceQuotaBO bo = new PtResourceQuotaBO();
        bo.setNamespace("namespace");bo.setName("resource-quota");
        HashMap<String, BizQuantity> hard = new HashMap<String, BizQuantity>(5){
            {
                put("cpu",new BizQuantity("1",""));
                put("memory",new BizQuantity("1024","Mi"));
                put("pods",new BizQuantity("10",""));
            }
        };
        bo.setHard(hard);
        bo.setScopeSelector(Arrays.asList(new BizScopedResourceSelectorRequirement("In","PriorityClass",Arrays.asList("medium"))));
        System.out.println("create = "+ JSON.toJSONString(resourceQuotaApi.create(bo)));
    }

    @Test
    public void list(){
        System.out.println("list = "+ JSON.toJSONString(resourceQuotaApi.list("namespace")));
    }

    @Test
    public void delete(){
        System.out.println("delete = "+ JSON.toJSONString(resourceQuotaApi.delete("namespace","resource-quota")));
    }

    @Test
    public void reachLimitsOfResources(){
        System.out.println(JSON.toJSONString(resourceQuotaApi.reachLimitsOfResources("namespace-37",16000,16000,2).getMessage()));
    }
}
