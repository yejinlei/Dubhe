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
import org.dubhe.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.domain.dto.PtTrainAlgorithmDeleteDTO;
import org.dubhe.domain.dto.PtTrainAlgorithmUpdateDTO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
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
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/algorithm").param("algorithmSource", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    /**
     * 查询当前用户的算法个数
     */
    @Test
    public void getAlgorithmCountTest() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/algorithm/myAlgorithmCount")
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        response.setCharacterEncoding("UTF-8");
        //得到返回状态码
        int status = response.getStatus();
        //得到返回结果
        String content = response.getContentAsString();
        //断言，判断返回代码是否正确
        Assert.assertEquals(200, status);
        System.out.println(content);

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
        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/algorithm"), JSON.toJSONString(ptTrainAlgorithmCreateDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

    /**
     * 修改算法
     */
    @Test
    public void ptTrainAlgorithmUpdateTest() throws Exception {
        PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO = new PtTrainAlgorithmUpdateDTO();
        ptTrainAlgorithmUpdateDTO.setId(138L)
                .setAlgorithmName("untilTesting"+ System.currentTimeMillis())
                .setDescription("untilTesting");
        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/algorithm"), JSON.toJSONString(ptTrainAlgorithmUpdateDTO), MockMvcResultMatchers.status().isOk(), 200);
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
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/algorithm"), JSON.toJSONString(ptTrainAlgorithmDeleteDTO), MockMvcResultMatchers.status().isOk(), 200);
    }

}
