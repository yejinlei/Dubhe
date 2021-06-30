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

import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.base.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @description 用户查询训练日志
 * @date 2020-6-20
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class PtTrainLogServiceTest extends BaseTest {

    /**
     * 查询训练日志
     *
     * @throws Exception
     */
    @Test
    public void searchTrainLogInfo() throws Exception {
        String accessToken = obtainAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobId", "68");
        params.add("startLine", "1");
        params.add("lines", "50");
        mockMvcWithNoRequestBody(mockMvc.perform(MockMvcRequestBuilders.get("/trainLog").params(params).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse(), 200);
    }
}
