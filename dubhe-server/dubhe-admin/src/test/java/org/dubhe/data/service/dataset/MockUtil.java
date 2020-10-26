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

package org.dubhe.data.service.dataset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.MultiValueMap;

/**
 * @date 2020-05-15
 */
@Slf4j
public class MockUtil {

    /**
     * mock测试 POST请求
     * @param requestJson
     * @param mockMvc
     * @param url
     * @return
     * @throws Exception
     */
    public static MvcResult post(String requestJson, MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson))
                .andDo(MockMvcResultHandlers.print()).andReturn();
    }

    /**
     * mock测试 put请求
     * @param url
     * @param params
     * @param mockMvc
     * @return
     * @throws Exception
     */
    public static MvcResult put(String url,MultiValueMap<String, String> params,MockMvc mockMvc) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url).params(params))
                .andDo(MockMvcResultHandlers.print()).andReturn();
    }

    /**
     * mock测试 get请求
     * @param url
     * @param mockMvc
     * @return
     * @throws Exception
     */
    public static MvcResult get(String url,MockMvc mockMvc) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    /**
     * mock测试 delete请求
     * @param url
     * @param params
     * @param mockMvc
     * @return
     * @throws Exception
     */
    public static MvcResult delete(String url,MultiValueMap<String, String> params,MockMvc mockMvc) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url).params(params))
                .andDo(MockMvcResultHandlers.print()).andReturn();
    }

    /**
     * 打印结果
     * @param mvcResult
     * @param bus
     */
    public static void showResponse(MvcResult mvcResult,String bus) {
        log.info("bus结果:"+mvcResult.getResponse());
    }

}
