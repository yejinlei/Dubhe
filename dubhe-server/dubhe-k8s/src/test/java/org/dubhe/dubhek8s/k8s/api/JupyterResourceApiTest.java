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
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtJupyterResourceBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description JupyterResourceApiTest测试类
 * @date 2020-04-14
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class JupyterResourceApiTest {

    @Resource
    private JupyterResourceApi jupyterResourceApi;
    @Resource
    private PodApi podApi;

    @Test
    public void get(){
        PtJupyterDeployVO result = jupyterResourceApi.get("namespace", "notebook");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void list(){
        List<PtJupyterDeployVO> list = jupyterResourceApi.list("namespace-1");
        list.forEach(obj->{
            System.out.println(JSON.toJSONString(obj));;
        });
    }

    @Test
    public void create() {
        Long startTime = System.currentTimeMillis();
        PtJupyterResourceBO bo = new PtJupyterResourceBO();
        String namespace = "namespace";
        String notebook = "notebook2";
        bo.setNamespace(namespace)
                .setName(notebook)
                .setCpuNum(4000)
                //.setGpuNum(1)
                //.setUseGpu(true)
                .setMemNum(4000)
                .setDatasetReadOnly(false)
                .setImage("notebook/jupyterlab:oneflow-0.0.1-pytorch-1.5.0-tf-2.1.0")
                .setDatasetDir("/nfs/namespace/dataset1")
                .setDatasetMountPath("/datasetPath")
                .setWorkspaceDir("/nfs/namespace/workspace1")
                .setWorkspaceMountPath("/workspace")
                .setWorkspaceRequest("100Mi")
                .setWorkspaceLimit("200Mi")
                .setDelayDeleteTime(20);
        PtJupyterDeployVO result = jupyterResourceApi.create(bo);
        System.out.println(JSON.toJSONString(result));
        int i = 0;
        while(true){
            System.out.println("i = "+ ++i);
            BizPod pod = podApi.getWithResourceName(namespace,notebook);
            if (pod != null && PodPhaseEnum.RUNNING.getPhase().equals(pod.getPhase())){
                System.out.println("Pod:"+pod.getPhase());
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Long podTime = System.currentTimeMillis();
        System.out.println("启动容器 耗时 "+(podTime-startTime)+" 毫秒");
        int j = 0;
        while(true){

            System.out.println("j = "+ ++j);
            String url = podApi.getUrlByResourceName(namespace,notebook);
            if (StringUtils.isNotEmpty(url)){
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Long urlTime = System.currentTimeMillis();
        System.out.println("启动notebook服务 耗时 "+(urlTime-podTime)+" 毫秒");
    }

    @Test
    public void createWithPvc() {
        PtJupyterResourceBO bo = new PtJupyterResourceBO();
        bo.setNamespace("namespace")
                .setName("notebook11")
                .setCpuNum(50)
                .setGpuNum(2)
                .setUseGpu(true)
                .setMemNum(2500000)
                .setDatasetReadOnly(false)
                .setImage("notebook/notebook-jupyter:oneflow-0.0.1-tensorflow-2.1.0-pytorch-1.5.0")
                .setDatasetDir("/nfs/namespace/dataset1")
                .setDatasetMountPath("/datasetPath")
                //.setWorkspaceDir("/nfs/namespace/workspace1")
                .setWorkspaceMountPath("/workspace")
                .setWorkspaceRequest("100Mi")
                .setWorkspaceLimit("200Mi")
                .setBusinessLabel("notebook")
                .setDelayDeleteTime(10);
        PtJupyterDeployVO result = jupyterResourceApi.createWithPvc(bo);
        System.out.println(JSON.toJSONString(result));
        System.out.println(podApi.getUrlByResourceName("namespace","myhfb"));
    }

    @Test
    public void delete() {
        PtBaseResult ptBaseResult = jupyterResourceApi.delete("namespace-75", "notebook-rn-20200819142637099n6t8");
        System.out.println(ptBaseResult);
    }
}
