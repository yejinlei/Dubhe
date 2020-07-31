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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.dubhe.support.login.UsernamePasswordCaptchaToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;

/**
 * @description 基础测试类
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
    protected MockMvc mockMvc;

    @Autowired
    private DefaultWebSecurityManager defaultWebSecurityManager;

    public BaseTest() {
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        SecurityUtils.setSecurityManager(defaultWebSecurityManager);
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordCaptchaToken token = new UsernamePasswordCaptchaToken("admin", "admin");
        token.setRememberMe(true);
        subject.login(token);
    }

    /**
     * mockMvcTest
     * content                       返回结果
     * status                        返回状态码
     *
     * @param mockHttpServletRequestBuilder 模拟HTTP请求
     * @param s                             传入参数
     * @param ok                            预期结果
     * @param i                             预期状态码
     * @throws throws Exception
     */
    public void mockMvcTest(MockHttpServletRequestBuilder mockHttpServletRequestBuilder, String s, ResultMatcher ok, int i) throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(
                mockHttpServletRequestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(s)
        ).andExpect(ok)
                .andReturn()
                .getResponse();
        response.setCharacterEncoding("UTF-8");
        //得到返回状态码
        int status = response.getStatus();
        //得到返回结果
        String content = response.getContentAsString();
        //断言，判断返回代码是否正确
        Assert.assertEquals(i, status);
        System.out.println(content);
    }

    /**
     * @param @param  response
     * @param @param  i
     * @param @throws UnsupportedEncodingException 入参
     * @return void 返回类型
     * @throws @Title: mockMvcWithNoRequestBody
     */
    public void mockMvcWithNoRequestBody(MockHttpServletResponse response, int i) throws UnsupportedEncodingException {
        response.setCharacterEncoding("UTF-8");
        // 得到返回代码
        int status = response.getStatus();
        // 得到返回结果
        String content = response.getContentAsString();
        // 断言，判断返回代码是否正确
        Assert.assertEquals(i, status);
        System.out.println("返回的参数" + content);
    }

}
