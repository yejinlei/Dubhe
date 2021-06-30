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
import org.dubhe.k8s.api.ModelServingApi;
import org.dubhe.k8s.domain.bo.ModelServingBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @description
 * @date 2020-09-11
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class ModelServingApiTest {
    @Autowired
    private ModelServingApi modelServingApi;

    @Test
    public void create(){
        ModelServingBO bo = new ModelServingBO();
        bo.setNamespace("sunjd");
        bo.setResourceName("model-serving");
        bo.setReplicas(10);
        bo.setMemNum(4000);
        bo.setCpuNum(1);
        bo.setGpuNum(3);
        bo.setImage("quay.io/kubernetes-ingress-controller/grpc-fortune-teller:0.1");
        bo.setGrpcPort(50051);
        bo.setBusinessLabel("serving");
        bo.setCmdLines(Arrays.asList("-c","while true; do echo hello; sleep 10;done"));
        bo.setFsMounts(new HashMap<String, PtMountDirBO>(){{
            put("/usr/local/TS_Serving/serving",new PtMountDirBO("/nfs/dubhe-dev/serving/TS_Serving"));
            put("/usr/local/TS_Serving/models/resnet50",new PtMountDirBO("/nfs/dubhe-dev/serving/models/oneflow_models/resnet50/"));
        }});
        System.out.println(JSON.toJSONString(modelServingApi.create(bo)));
    }

    @Test
    public void delete(){
        modelServingApi.delete("namespace-1","serving-rn-nogn35");
        modelServingApi.delete("namespace-1","serving-rn-nojb33");
        modelServingApi.delete("namespace-1","serving-rn-nxvr86");
        modelServingApi.delete("namespace-1","serving-rn-nyfe87");

        modelServingApi.delete("namespace-1","serving-rn-rije34");
        modelServingApi.delete("namespace-1","serving-rn-ubvo102");

        System.out.println(JSON.toJSONString(modelServingApi.delete("namespace-1","serving-rn-nyfe87")));
    }
}
