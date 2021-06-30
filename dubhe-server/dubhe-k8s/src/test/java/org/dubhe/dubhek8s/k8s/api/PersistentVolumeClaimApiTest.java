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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @description PersistentVolumeClaimApiTest测试类
 * @date 2020-04-23
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class PersistentVolumeClaimApiTest {
    @Resource
    private PersistentVolumeClaimApi persistentVolumeClaimApi;

    @Test
    public void create() {
        PtPersistentVolumeClaimBO bo = new PtPersistentVolumeClaimBO();bo.setAccessModes(new HashSet<String>(){{
            add("ReadWriteOnce");
        }});
        bo.setNamespace("namespace");bo.setPvcName("pvc");bo.setProvisioner("k8s.io/minikube-hostpath");
        bo.setParameters(new HashMap<String,String>(5){
            {
                put("resturl", "http://localhost:8080");
                put("restuser", "");
                put("secretNamespace", "");
                put("secretName", "");
                put("key", "value1");
            }
        });
        bo.setRequest("1Gi");
        bo.setLimit("2Gi");
        System.out.println("create = "+ JSON.toJSONString(persistentVolumeClaimApi.create(bo)));
    }

    @Test
    public void createWithPv() throws JsonProcessingException {
        PtPersistentVolumeClaimBO bo = new PtPersistentVolumeClaimBO();bo.setAccessModes(new HashSet<String>(){{
            add("ReadWriteOnce");
        }});
        bo.setNamespace("namespace");bo.setPvcName("notebook-resource-name-nb007-pvc");
        bo.setPath("/notebook/0/20200506112022x4d8");
        bo.setRequest("500Mi");
        bo.setLimit("500Mi");
        System.out.println("create = "+ JSON.toJSONString(persistentVolumeClaimApi.createWithFsPv(bo)));
    }

    @Test
    public void list() {
        System.out.println("list = "+ JSON.toJSONString(persistentVolumeClaimApi.list("namespace-1")));
    }

    @Test
    public void delete() {
        System.out.println("delete = "+ JSON.toJSONString(persistentVolumeClaimApi.delete("namespace","night-test-pvc")));
    }

    @Test
    public void recycle() {
        System.out.println("delete = "+ JSON.toJSONString(persistentVolumeClaimApi.recycle("namespace","oprcds")));
    }

    @Test
    public void deletePv(){
        System.out.println("deletePV = "+ JSON.toJSONString(persistentVolumeClaimApi.deletePv("night-test-pvc-pv")));
    }

    @Test
    public void getPv(){
        System.out.println("deletePV = "+ JSON.toJSONString(persistentVolumeClaimApi.getPv("yzc-test-52-v1-pvc-pv")));
    }
}
