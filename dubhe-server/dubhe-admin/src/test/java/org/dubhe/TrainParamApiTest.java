/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.domain.dto.PtTrainParamCreateDTO;
import org.dubhe.domain.dto.PtTrainParamUpdateDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description: 训练任务管理模块任务参数管理单元测试
 * @date: 2020-5-11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TrainParamApiTest extends BaseTest {

    /**
     * 查询任务参数列表
     */
    @Test
    public void ptTrainParamQueryTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainParams"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 查询任务参数列表
     */
    @Test
    public void ptTrainParamQueryTest1() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainParams").param("paramName", "lpf").param("resourcesPoolType", "0"))
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
        ptTrainParamCreateDTO.setParamName("新增任务参数名称").setAlgorithmId((long) 1).setRunParams(runParams)
                .setDescription("描述信息").setDataSourcePath("1")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");
        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamCreateDTO), MockMvcResultMatchers.status().isOk(), 200);

    }

    /**
     * 新增任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamCreateTest1() throws Exception {
        PtTrainParamCreateDTO ptTrainParamCreateDTO = new PtTrainParamCreateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("key1", 2);
        runParams.put("key2", 2);
        runParams.put("key3", 2);
        runParams.put("key4", 2);
        ptTrainParamCreateDTO.setParamName("新增任务参数名称")
                .setAlgorithmId((long) 1)
                .setRunParams(runParams)
                .setDescription("描述信息")
                .setDataSourcePath("/usr/local/data/out.json")
                .setDataSourceName("out.json")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");
        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamCreateDTO), MockMvcResultMatchers.status().is4xxClientError(), 400);

    }

    /**
     * 新增任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamCreateTest2() throws Exception {
        PtTrainParamCreateDTO ptTrainParamCreateDTO = new PtTrainParamCreateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("key1", 3);
        runParams.put("key2", 3);
        runParams.put("key3", 3);
        runParams.put("key4", 3);
        ptTrainParamCreateDTO.setParamName("新增任务参数名称" + System.currentTimeMillis()).setAlgorithmId(Long.MAX_VALUE).setRunParams(runParams)
                .setDescription("描述信息")
                .setDataSourcePath("/usr/local/data/out.json")
                .setDataSourceName("out.json")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");
        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamCreateDTO), MockMvcResultMatchers.status().is4xxClientError(), 400);

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
        ptTrainParamUpdateDTO.setId((long) 1).setParamName("修改任务参数名称" + System.currentTimeMillis()).setAlgorithmId((long) 1)
                .setRunParams(runParams)
                .setDescription("描述信息")
                .setDataSourcePath("/usr/local/data/out.json")
                .setDataSourceName("out.json")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("harbor.dubhe.ai/tensorflow/tensorflow:latest");
        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamUpdateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 修改任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamUpdateTest2() throws Exception {
        PtTrainParamUpdateDTO ptTrainParamUpdateDTO = new PtTrainParamUpdateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("key1", 22);
        runParams.put("key2", 22);
        runParams.put("key3", 22);
        runParams.put("key4", 22);
        ptTrainParamUpdateDTO.setId((long) 2).setParamName("修改测试").setAlgorithmId((long) 1).setRunParams(runParams)
                .setDescription("描述信息").setDataSourcePath("/usr/local/data/out.json")
                .setDataSourceName("out.json")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");
        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamUpdateDTO), MockMvcResultMatchers.status().is4xxClientError(), 400);
    }

    /**
     * 修改任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamUpdateTest1() throws Exception {
        PtTrainParamUpdateDTO ptTrainParamUpdateDTO = new PtTrainParamUpdateDTO();
        JSONObject runParams = new JSONObject();
        runParams.put("key1", 33);
        runParams.put("key2", 33);
        runParams.put("key3", 33);
        runParams.put("key4", 33);
        ptTrainParamUpdateDTO.setId((long) 1).setParamName("修改任务参数名称").setAlgorithmId((long) 10).setRunParams(runParams)
                .setDescription("描述信息").setDataSourcePath("/usr/local/data/out.json")
                .setDataSourceName("out.json")
                .setResourcesPoolType(1).setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");
        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/trainParams"), JSON.toJSONString(ptTrainParamUpdateDTO), MockMvcResultMatchers.status().is4xxClientError(), 400);
    }

    /**
     * 删除任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void ptTrainParamDeleteTest() throws Exception {
        Long[] ids = {1L};
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/trainParams"), JSON.toJSONString(ids), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 删除任务参数
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamDeleteTest0() throws Exception {
        Long[] ids = {};
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/trainParams"), JSON.toJSONString(ids), MockMvcResultMatchers.status().is4xxClientError(), 400);
    }

    /**
     * 删除任务参数
     * 传入id数组在映射表中有id不存在测试
     * id=10在表中不存在
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void ptTrainParamDeleteTest1() throws Exception {
        Long[] ids = {1L, 10L};
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/trainParams"), JSON.toJSONString(ids), MockMvcResultMatchers.status().is4xxClientError(), 400);
    }

}
