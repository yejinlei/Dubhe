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
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @description TrainJobApiTest测试类
 * @date 2020-04-14
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class TrainJobApiTest {

    @Resource
    private TrainJobApi trainJobApi;

    @Test
    public void list(){
        List<BizJob> list = trainJobApi.list("namespace-1");
        for (BizJob bizJob : list) {
            System.out.println(bizJob);
        }
    }

    @Test
    public void get(){
        BizJob bizJob = trainJobApi.get("namespace", "seven-test");
        System.out.println(JSON.toJSONString(bizJob));
    }

    @Test
    public void create()  {
        PtJupyterJobBO bo = new PtJupyterJobBO();
        bo.setNamespace("namespace-1")
                .setName("train5")
                .setCpuNum(500)
                .setGpuNum(1)
                .setUseGpu(true)
                .setMemNum(200)
                .setCmdLines(Arrays.asList("-c","while true; do echo hello; sleep 10;done"))
                .setFsMounts(new HashMap<String, PtMountDirBO>(){{
                    put("/dataset",new PtMountDirBO("/nfs/xxx/dataset"));
                    put("/workspace",new PtMountDirBO("/nfs/xxx/dataset"));
                    put("/valdataset",new PtMountDirBO("/nfs/xxx/dataset"));
                }})
                .setImage("tensorflow/tensorflow:latest")
                .setBusinessLabel("train")
                .setDelayDeleteTime(10)
                .setDelayCreateTime(10);
        System.out.println("before create");
        PtJupyterJobVO result = trainJobApi.create(bo);
        System.out.println("after create");
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void delete(){
        System.out.println("before delete");
        boolean isDeleted = trainJobApi.delete("test-ns", "my-ml");
        System.out.println("after delete: " + isDeleted);
    }
}
