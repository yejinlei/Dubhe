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

package org.dubhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.domain.dto.PtTrainJobCreateDTO;
import org.dubhe.domain.dto.PtTrainJobDeleteDTO;
import org.dubhe.domain.dto.PtTrainJobStopDTO;
import org.dubhe.domain.dto.PtTrainJobUpdateDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 训练任务管理模块任务参数管理单元测试
 * @date 2020-5-11
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class PtTrainModelOptJobApiTest extends BaseTest {

    /**
     * 作业列表展示
     *
     * @throws Exception
     */
    @Test
    public void getTrainJobTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 规格展示
     *
     * @throws Exception
     */
    @Test
    public void getTrainJobSpecsTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob/trainJobSpecs").param("resourcesPoolType", "0"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 根据jobId查询训练任务详情
     *
     * @throws Exception
     */
    @Test
    public void getTrainJobDetailTest() throws Exception {
        mockMvcWithNoRequestBody(
                mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob/jobDetail").param("id", "538"))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(),
                200);
    }

    /**
     * 我的训练任务统计
     *
     * @throws Exception
     */
    @Test
    public void statisticsMineTest() throws Exception {
        mockMvcWithNoRequestBody(this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob/mine"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 作业不同版本任务列表展示
     *
     * @param @throws Exception 入参
     * @return void 返回类型
     * @throws @date 2020年6月16日 上午10:19:12
     * @Title: getTrainJobVersionTest
     * @version V1.0
     */
    @Test
    public void getTrainJobVersionTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob/trainJobVersionDetail").param("trainId", String.valueOf(371L)))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);

    }

    /**
     * 数据集状态展示
     *
     * @param @throws Exception 入参
     * @return void 返回类型
     * @throws @date 2020年6月16日 上午10:31:25
     * @Title: getTrainDataSourceStatusTest
     * @version V1.0
     */
    @Test
    public void getTrainDataSourceStatusTest() throws Exception {
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trainJob/dataSourceStatus").param("dataSourcePath",
                "dataset/68,dataset/20741/versionFile/V0003"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);

    }

    /**
     * 创建训练任务
     *
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void createTrainJobTest() throws Exception {
        PtTrainJobCreateDTO ptTrainJobCreateDTO = new PtTrainJobCreateDTO();
        ptTrainJobCreateDTO.setTrainName("创建训练任务单元测试");
        ptTrainJobCreateDTO.setAlgorithmId(134L);
        ptTrainJobCreateDTO.setDataSourceName("CUB-200-2011:V0002");
        ptTrainJobCreateDTO.setDataSourcePath("dataset/242/versionFile/V0002/ofrecord/train");
        ptTrainJobCreateDTO.setDescription("创建训练任务单元测试");
        ptTrainJobCreateDTO.setResourcesPoolType(1).setResourcesPoolNode(1).setTrainType(0);
        ptTrainJobCreateDTO.setTrainJobSpecsName("1Core4GB 1TITAN V").setTrainJobSpecsInfo("1Core4GB 1TITAN V").setRunCommand("python atlas_knowledge_distillation.py").setImageName("atlas").setImageTag("2.1");
        JSONObject runParams = new JSONObject();
        runParams.put("epochs", 100);
        runParams.put("batch_size", 16);
        runParams.put("weight_decay", 1e-4);
        runParams.put("learning_rate", 1e-4);
        ptTrainJobCreateDTO.setRunParams(runParams);

        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/trainJob"), JSON.toJSONString(ptTrainJobCreateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }

    /**
     * 更新训练任务测试
     *
     * @param @throws Exception 入参
     * @return void 返回类型
     * @throws @date 2020年6月16日 上午10:46:55
     * @Title: updateTrainJobTest
     * @version V1.0
     */
    @Test
    public void updateTrainJobTest() throws Exception {
        // TODO Auto-generated method stub
        PtTrainJobUpdateDTO ptTrainJobUpdateDTO = new PtTrainJobUpdateDTO();
        ptTrainJobUpdateDTO.setId(537L);
        ptTrainJobUpdateDTO.setAlgorithmId(134L);
        ptTrainJobUpdateDTO.setDataSourceName("CUB-200-2011:V0002");
        ptTrainJobUpdateDTO.setDataSourcePath("dataset/242/versionFile/V0002/ofrecord/train");
        ptTrainJobUpdateDTO.setDescription("修改训练任务单元测试");
        ptTrainJobUpdateDTO.setResourcesPoolType(1).setResourcesPoolNode(1).setTrainType(0);
        ptTrainJobUpdateDTO.setTrainJobSpecsName("1Core4GB 1TITAN V").setTrainJobSpecsInfo("1Core4GB 1TITAN V").setRunCommand("python atlas_knowledge_distillation.py").setImageName("atlas").setImageTag("2.1");
        JSONObject runParams = new JSONObject();
        runParams.put("epochs", 100);
        runParams.put("batch_size", 16);
        runParams.put("weight_decay", 1e-4);
        runParams.put("learning_rate", 1e-4);
        ptTrainJobUpdateDTO.setRunParams(runParams);
        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/trainJob"), JSON.toJSONString(ptTrainJobUpdateDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);

    }

    /**
     * 删除训练任务
     *
     * @param
     * @return void 返回类型
     * @throws @date     2020年6月16日 上午10:50:31
     * @throws Exception
     * @Title: deleteTrainJobTest
     * @version V1.0
     */
    @Test
    public void deleteTrainJobTest() throws Exception {
        // TODO Auto-generated method stub
        PtTrainJobDeleteDTO ptTrainJobDeleteDTO = new PtTrainJobDeleteDTO();
        ptTrainJobDeleteDTO.setId(118L);
        ptTrainJobDeleteDTO.setTrainId(87L);
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/trainJob"), JSON.toJSONString(ptTrainJobDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);

    }

    /**
     *
     *
     * @throws Exception
     */
    @Test
    public void deleteTrainJobWithNoIdTest() throws Exception {
        // TODO Auto-generated method stub
        PtTrainJobDeleteDTO ptTrainJobDeleteDTO = new PtTrainJobDeleteDTO();
        ptTrainJobDeleteDTO.setTrainId(36L);
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/trainJob"), JSON.toJSONString(ptTrainJobDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);

    }

    /**
     * 停止训练任务测试
     *
     * @param @throws Exception 入参
     * @return void 返回类型
     * @throws @date 2020年6月16日 上午10:55:22
     * @Title: testStopTrainJob
     * @version V1.0
     */
    @Test
    public void testStopTrainJob() throws Exception {

        PtTrainJobStopDTO dto = new PtTrainJobStopDTO();
        dto.setId(46L);
        dto.setTrainId(37L);

        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/trainJob/stop"), JSON.toJSONString(dto),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);
    }


}
