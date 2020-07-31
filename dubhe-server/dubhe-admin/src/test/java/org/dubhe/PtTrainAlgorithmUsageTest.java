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
import org.dubhe.domain.dto.PtTrainAlgorithmUsageCreateDTO;
import org.dubhe.domain.dto.PtTrainAlgorithmUsageUpdateDTO;
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

        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/algorithmUsage").param("isContainDefault", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    @Test
    public void queryAllTest2() throws Exception {

        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/algorithmUsage").param("isContainDefault", "0"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }

    @Test
    public void createTest() throws Exception {

        PtTrainAlgorithmUsageCreateDTO ptTrainAlgorithmUsageCreateDTO = new PtTrainAlgorithmUsageCreateDTO();
        ptTrainAlgorithmUsageCreateDTO.setAuxInfo("测试");

        mockMvcTest(MockMvcRequestBuilders.post("/api/v1/algorithmUsage"),
                JSON.toJSONString(ptTrainAlgorithmUsageCreateDTO), MockMvcResultMatchers.status().is2xxSuccessful(),
                200);

    }


    @Test
    public void deleteTest() throws Exception {
        Long[] longs = new Long[1];
        longs[0] = 13L;
        mockMvcTest(MockMvcRequestBuilders.delete("/api/v1/algorithmUsage"), JSON.toJSONString(longs),
                MockMvcResultMatchers.status().is2xxSuccessful(), 200);

    }

    @Test
    public void updateTest() throws Exception {
        PtTrainAlgorithmUsageUpdateDTO ptTrainAlgorithmUsageUpdateDTO = new PtTrainAlgorithmUsageUpdateDTO();

        ptTrainAlgorithmUsageUpdateDTO.setId(12L);
        ptTrainAlgorithmUsageUpdateDTO.setAuxInfo("更新测试");

        mockMvcTest(MockMvcRequestBuilders.put("/api/v1/algorithmUsage"),
                JSON.toJSONString(ptTrainAlgorithmUsageUpdateDTO), MockMvcResultMatchers.status().is2xxSuccessful(),
                200);
    }


}
