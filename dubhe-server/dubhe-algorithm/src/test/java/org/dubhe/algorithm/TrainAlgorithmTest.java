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

package org.dubhe.algorithm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUpdateDTO;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 算法管理模块算法管理单元测试
 * @date 2020-06-18
 */
public class TrainAlgorithmTest extends BaseTest {

    /**
     * 查询算法列表
     */
    @Test
    public void ptTrainAlgorithmQueryTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/algorithms").param("algorithmSource", "2").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 查询当前用户的算法个数
     */
    @Test
    public void getAlgorithmCountTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/algorithms/myAlgorithmCount").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }


    /**
     * 新增算法
     */
    @Test
    public void ptTrainAlgorithmCreateTest() throws Exception {
        PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO = new PtTrainAlgorithmCreateDTO();
        JSONObject jsonObject = new JSONObject();
        ptTrainAlgorithmCreateDTO.setAlgorithmName("untilTesting")
                .setDescription("untilTesting")
                .setCodeDir("upload-temp/1/20201202135732212Bp8F/OneFlow算法.zip");
        mockMvcTest(MockMvcRequestBuilders.post("/algorithms"), JSON.toJSONString(ptTrainAlgorithmCreateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 修改算法
     */
    @Test
    public void ptTrainAlgorithmUpdateTest() throws Exception {
        PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO = new PtTrainAlgorithmUpdateDTO();
        ptTrainAlgorithmUpdateDTO.setId(138L)
                .setAlgorithmName("untilTesting" + System.currentTimeMillis())
                .setDescription("untilTesting");
        mockMvcTest(MockMvcRequestBuilders.put("/algorithms"), JSON.toJSONString(ptTrainAlgorithmUpdateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }


    /**
     * 删除算法
     */
    @Test
    public void ptTrainAlgorithmDeleteTest() throws Exception {
        Set<Long> ids = new HashSet<>();
        ids.add(138L);
        PtTrainAlgorithmDeleteDTO ptTrainAlgorithmDeleteDTO = new PtTrainAlgorithmDeleteDTO();
        ptTrainAlgorithmDeleteDTO.setIds(ids);
        mockMvcTest(MockMvcRequestBuilders.delete("/algorithms"), JSON.toJSONString(ptTrainAlgorithmDeleteDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

}
