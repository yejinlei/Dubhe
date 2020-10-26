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
import org.dubhe.domain.dto.PtTrainJobCreateDTO;
import org.dubhe.domain.dto.PtTrainJobStopDTO;
import org.dubhe.domain.dto.PtTrainQueryDTO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @description
 * @date 2020-04-17
 */
public class TrainModelOptJobApiTest extends BaseTest {

    /**
     * 停止任务测试
     *
     * @throws Exception
     */
    @Test
    public void testStopTrainJob() throws Exception {

        PtTrainJobStopDTO dto = new PtTrainJobStopDTO();
        dto.setId(1L);
        dto.setTrainId(10L);
        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.
                        post("/api/train_job/1")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.toJSONString(dto)
                ))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = JSON.parseObject(response.getContentAsString());
        Assert.assertNotNull(jsonObject.get("code"));
    }


    /**
     * 我的训练任务统计
     *
     * @throws Exception
     */
    @Test
    public void statisticsMine() throws Exception {

        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.
                        get("/api/train_job/mine")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = JSON.parseObject(response.getContentAsString());
        Assert.assertNotNull(jsonObject.get("code"));
    }

    /**
     * 分页查询训练任务
     */
    @Test
    public void testGetTrainJob() throws Exception {
        PtTrainQueryDTO dto = new PtTrainQueryDTO();
        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.
                        get("/api/v1/trainJob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = JSON.parseObject(response.getContentAsString());
        Assert.assertNotNull(jsonObject.get("code"));
    }

    /**
     * 分页查询训练任务
     */
    @Test
    public void testCreateTrainJob() throws Exception {
        PtTrainJobCreateDTO dto = new PtTrainJobCreateDTO();
        dto.setAlgorithmId(3L);
        dto.setRunParams(new JSONObject());
        dto.setDataSourceName("dataset/68");
        dto.setDataSourcePath("dataset/68");
        dto.setTrainName("test-train"+System.currentTimeMillis());
        dto.setTrainParamDesc("test-train");
        dto.setTrainJobSpecsName("11111").setRunCommand("python p.py").setImageName("tensorflow").setImageTag("latest");

        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.
                        post("/api/v1/trainJob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();
        JSONObject jsonObject = JSON.parseObject(response.getContentAsString());
        Assert.assertNotNull(jsonObject.get("code"));
    }
}
