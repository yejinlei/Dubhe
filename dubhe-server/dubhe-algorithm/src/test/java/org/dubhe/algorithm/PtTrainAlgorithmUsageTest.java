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
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageUpdateDTO;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @description 算法用途单元测试
 * @date 2020-06-23
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class PtTrainAlgorithmUsageTest extends BaseTest {

    /**
     * 修改任务参数 算法id=2在算法表中runcommand为空
     */
    @Test
    public void queryAllTest() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/algorithmUsage").param("isContainDefault", "1").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    @Test
    public void queryAllTest2() throws Exception {
        String accessToken = obtainAccessToken();
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/algorithmUsage").param("isContainDefault", "0").header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    @Test
    public void createTest() throws Exception {

        PtTrainAlgorithmUsageCreateDTO ptTrainAlgorithmUsageCreateDTO = new PtTrainAlgorithmUsageCreateDTO();
        ptTrainAlgorithmUsageCreateDTO.setAuxInfo("untilTesting");

        mockMvcTest(MockMvcRequestBuilders.post("/algorithmUsage"),
                JSON.toJSONString(ptTrainAlgorithmUsageCreateDTO), MockMvcResultMatchers.status().is2xxSuccessful(),
                200);

    }


    @Test
    public void deleteTest() throws Exception {
        Long[] longs = new Long[1];
        longs[0] = 38L;
        PtTrainAlgorithmUsageDeleteDTO ptTrainAlgorithmUsageDeleteDTO = new PtTrainAlgorithmUsageDeleteDTO();
        ptTrainAlgorithmUsageDeleteDTO.setIds(longs);
        mockMvcTest(MockMvcRequestBuilders.delete("/algorithmUsage"), JSON.toJSONString(ptTrainAlgorithmUsageDeleteDTO),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);

    }

    @Test
    public void updateTest() throws Exception {
        PtTrainAlgorithmUsageUpdateDTO ptTrainAlgorithmUsageUpdateDTO = new PtTrainAlgorithmUsageUpdateDTO();

        ptTrainAlgorithmUsageUpdateDTO.setId(38L);
        ptTrainAlgorithmUsageUpdateDTO.setAuxInfo("更新测试");

        mockMvcTest(MockMvcRequestBuilders.put("/algorithmUsage"),
                JSON.toJSONString(ptTrainAlgorithmUsageUpdateDTO), MockMvcResultMatchers.status().is2xxSuccessful(),
                200);
    }


}
