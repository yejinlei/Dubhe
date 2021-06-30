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

package org.dubhe.train;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.dubhe.train.domain.dto.PtTrainParamCreateDTO;
import org.dubhe.train.domain.dto.PtTrainParamDeleteDTO;
import org.dubhe.train.domain.dto.PtTrainParamUpdateDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 训练任务管理模块任务参数管理单元测试
 * @date 2020-5-11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TrainParamApiTest extends BaseTest {

    /**
     * 查询任务参数列表
     */
    @Test
    public void ptTrainParamQueryTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/trainParams").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }


    /**
     * 新增任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptTrainParamCreateTest() throws Exception {
        PtTrainParamCreateDTO ptTrainParamCreateDTO = new PtTrainParamCreateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("data_url", "/nfs/testuser1/mnist/MNIST_data");
        ptTrainParamCreateDTO.setParamName("新增任务参数名称单元测试").setAlgorithmId(133L).setRunParams(runParams)
                .setDescription("新增任务参数名称").setDataSourceName("T1:V0004").setDataSourcePath("dataset/159/versionFile/V0004/ofrecord/train")
                .setResourcesPoolType(1).setTrainType(0).setResourcesPoolNode(1).setRunCommand("python p.py").setTrainJobSpecsName("1Core4GB 1TITAN V").setImageName("oneflow").setImageTag("cu102-py37-dist");
        mockMvcTest(MockMvcRequestBuilders.post("/trainParams"), JSON.toJSONString(ptTrainParamCreateDTO), MockMvcResultMatchers.status().isOk(), 200);

    }


    /**
     * 修改任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptTrainParamUpdateTest() throws Exception {
        PtTrainParamUpdateDTO ptTrainParamUpdateDTO = new PtTrainParamUpdateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("key1", 11);
        runParams.put("key2", 11);
        runParams.put("key3", 11);
        runParams.put("key4", 11);
        ptTrainParamUpdateDTO.setId(125L).setParamName("修改任务参数名称单元测试" + System.currentTimeMillis()).setAlgorithmId(133L).setRunParams(runParams)
                .setDescription("修改任务参数名称单元测试").setDataSourceName("T1:V0004").setDataSourcePath("dataset/159/versionFile/V0004/ofrecord/train")
                .setResourcesPoolType(1).setTrainType(0).setResourcesPoolNode(1).setRunCommand("python p.py").setTrainJobSpecsName("1Core4GB 1TITAN V").setImageName("oneflow").setImageTag("cu102-py37-dist");
        mockMvcTest(MockMvcRequestBuilders.put("/trainParams"), JSON.toJSONString(ptTrainParamUpdateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }


    /**
     * 删除任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptTrainParamDeleteTest() throws Exception {
        Set<Long> ids = new HashSet<>();
        ids.add(125L);
        PtTrainParamDeleteDTO ptTrainParamDeleteDTO = new PtTrainParamDeleteDTO();
        ptTrainParamDeleteDTO.setIds(ids);
        mockMvcTest(MockMvcRequestBuilders.delete("/trainParams"), JSON.toJSONString(ptTrainParamDeleteDTO), MockMvcResultMatchers.status().isOk(), 200);
    }
}
