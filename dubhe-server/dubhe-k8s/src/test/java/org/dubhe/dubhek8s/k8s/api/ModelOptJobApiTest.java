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
import org.dubhe.k8s.api.ModelOptJobApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtModelOptimizationJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @description ModelOptJobApiTest测试类
 * @date 2020-05-31
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class ModelOptJobApiTest {
    @Autowired
    private ModelOptJobApi modelOptJobApi;

    @Test
    public void create() {
        PtModelOptimizationJobBO bo = new PtModelOptimizationJobBO();
        bo.setNamespace("job");
        bo.setName("job9");
        bo.setBusinessLabel("model");
        bo.setCpuNum(500);
        bo.setGpuNum(1);
        bo.setMemNum(1000);
        bo.setCmdLines(Arrays.asList("-c","while true; do echo hello; sleep 10;done"));
        bo.setFsMounts(new HashMap<String, PtMountDirBO>(){{
            put("/dataset",new PtMountDirBO("/nfs/job/dataset"));
            put("/workspace",new PtMountDirBO("/nfs/job/dataset"));
            put("/output",new PtMountDirBO("/nfs/job/output"));
        }});
        bo.setImage("nvidia/cuda:10.2-base-centos7");

        BizJob result = modelOptJobApi.create(bo);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void getWithResourceName() {
        BizJob result = modelOptJobApi.getWithResourceName("namespace","job");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void getWithNamespace() {
        List<BizJob> result = modelOptJobApi.getWithNamespace("namespace");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void listAll() {
        List<BizJob> result = modelOptJobApi.listAll();
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void deleteByResourceName() {
        PtBaseResult result = modelOptJobApi.deleteByResourceName("namespace","job");
        System.out.println(JSON.toJSONString(result));
    }
}
