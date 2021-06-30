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
import org.dubhe.k8s.api.DubheDeploymentApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtModelOptimizationDeploymentBO;
import org.dubhe.k8s.domain.resource.BizDeployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @description DeploymentApiTest测试类
 * @date 2020-05-28
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class DeploymentApiTest {
    @Autowired
    private DubheDeploymentApi deploymentApi;

    @Test
    public void create() {
        PtModelOptimizationDeploymentBO bo = new PtModelOptimizationDeploymentBO();
        bo.setNamespace("namespace");
        bo.setName("deployment");
        bo.setBusinessLabel("model");
        bo.setCpuNum(500);
        bo.setGpuNum(1);
        bo.setMemNum(1000);
        bo.setCmdLines(Arrays.asList("-c","while true; do echo hello; sleep 10;done"));
        bo.setDatasetDir("/nfs/namespace/dataset");
        bo.setDatasetMountPath("/dataset");
        bo.setWorkspaceDir("/nfs/namespace/workspace");
        bo.setWorkspaceMountPath("/workspace");
        bo.setOutputDir("/nfs/namespace/output");
        bo.setOutputMountPath("/output");
        bo.setImage("nvidia/cuda:10.2-base-centos7");

        BizDeployment result = deploymentApi.create(bo);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void getWithResourceName() {
        BizDeployment result = deploymentApi.getWithResourceName("namespace","deployment");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void getWithNamespace() {
        List<BizDeployment> result = deploymentApi.getWithNamespace("namespace");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void listAll() {
        List<BizDeployment> result = deploymentApi.listAll();
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void deleteByResourceName() {
        PtBaseResult result = deploymentApi.deleteByResourceName("namespace","deployment");
        System.out.println(JSON.toJSONString(result));
    }

}
