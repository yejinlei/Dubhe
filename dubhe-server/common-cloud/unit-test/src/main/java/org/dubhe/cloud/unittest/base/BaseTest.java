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

package org.dubhe.cloud.unittest.base;

import cn.hutool.http.HttpStatus;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.cloud.unittest.config.UnitTestConfig;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description ???????????????
 * @date 2020-04-20
 */
@ActiveProfiles(value = "dev")
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableTransactionManagement
@WebAppConfiguration
public class BaseTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    protected MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UnitTestConfig unitTestConfig;

    public BaseTest() {
    }

    /**
     * ?????????MockMvc??????,??????Security????????????
     */
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
    }

    /**
     * mockMvcTest
     * content                       ????????????
     * status                        ???????????????
     *
     * @param mockHttpServletRequestBuilder ??????HTTP??????
     * @param s                             ????????????
     * @param ok                            ????????????
     * @param i                             ???????????????
     * @throws throws Exception
     */
    public void mockMvcTest(MockHttpServletRequestBuilder mockHttpServletRequestBuilder, String s, ResultMatcher ok, int i) throws Exception {
        String accessToken = obtainAccessToken();
        MockHttpServletResponse response = this.mockMvc.perform(
                mockHttpServletRequestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(s).header(AuthConst.AUTHORIZATION, AuthConst.ACCESS_TOKEN_PREFIX + accessToken)
        ).andExpect(ok)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding("UTF-8");
        //?????????????????????
        int status = response.getStatus();
        //??????????????????
        String content = response.getContentAsString();
        //???????????????????????????????????????
        Assert.assertEquals(i, status);
        System.out.println(content);
    }

    /**
     * @param response ????????????
     * @param  i ?????????
     * @param @throws UnsupportedEncodingException ??????
     * @return void ????????????
     * @throws @Title: mockMvcWithNoRequestBody
     */
    public void mockMvcWithNoRequestBody(MockHttpServletResponse response, int i) throws UnsupportedEncodingException {
        response.setCharacterEncoding("UTF-8");
        // ??????????????????
        int status = response.getStatus();
        // ??????????????????
        String content = response.getContentAsString();
        // ???????????????????????????????????????
        Assert.assertEquals(i, status);
        System.out.println("???????????????" + content);
    }

    /**
     *  ??????????????????token
     * @return String  token
     */
    public String obtainAccessToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", AuthConst.GRANT_TYPE);
        params.add("client_id", AuthConst.CLIENT_ID);
        params.add("client_secret", AuthConst.CLIENT_SECRET);
        params.add("username", unitTestConfig.getUsername());
        params.add("password", unitTestConfig.getPassword());
        params.add("scope", "all");
        HttpHeaders headers = new HttpHeaders();
        // ?????????????????????application/x-www-form-urlencoded??????
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        ResponseEntity<DataResponseBody> responseEntity = restTemplate.postForEntity("http://" + ApplicationNameConst.SERVER_AUTHORIZATION + "/oauth/token", httpEntity, DataResponseBody.class);
        if (HttpStatus.HTTP_OK != responseEntity.getStatusCodeValue()) {
            return null;
        }
        DataResponseBody restResult = responseEntity.getBody();
        Map map = new LinkedHashMap();
        if (restResult.succeed()) {
            map = (Map) restResult.getData();
        }
        // ?????? token
        return (String) map.get("token");
    }
}
