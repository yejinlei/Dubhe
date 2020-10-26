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

package org.dubhe.utils;

import org.dubhe.enums.BizNfsEnum;
import org.dubhe.k8s.domain.PtBaseResult;
import org.apache.shiro.SecurityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description Notebook 工具测试类
 *
 * @date 2020.04.27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootConfiguration
public class NotebookUtilTest {


    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;




    public NotebookUtilTest() {
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        org.apache.shiro.mgt.SecurityManager securityManager = new org.apache.shiro.mgt.DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);


    }


    @Test
    public void getCurUserId() {
        Assert.assertEquals(0L,NotebookUtil.getCurUserId());
    }

    @Test
    public void generateNameSpace() {
        Assert.assertEquals("notebook-namespace-1",NotebookUtil.generateNameSpace(1L));

    }

    @Test
    public void generateResourceName() {
        Assert.assertEquals("notebook-resource-name-notebookName",NotebookUtil.generateResourceName("notebookName"));
    }

    @Test
    public void getImageName() {
        Assert.assertEquals("10.5.24.118:5000/notebook-tf-of-pytorch",NotebookUtil.getImageName());
    }

    @Test
    public void generatePvcPath() {
        String pvcPath = NotebookUtil.generatePvcPath(1L);
        Assert.assertTrue(pvcPath,pvcPath.contains("/notebook/1/"));
    }

    @Test
    public void getK8sStatusInfo() {
        Assert.assertEquals("",NotebookUtil.getK8sStatusInfo(new PtBaseResult("401",null)));
        Assert.assertEquals("123",NotebookUtil.getK8sStatusInfo(new PtBaseResult("401","123")));
        Assert.assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                ,NotebookUtil.getK8sStatusInfo(
                        new PtBaseResult(
                                "401",
                                "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234")
                )
        );
        Assert.assertEquals("new test exception",NotebookUtil.getK8sStatusInfo(new Exception("new test exception")));
        Assert.assertEquals("K8s NullPointerException",NotebookUtil.getK8sStatusInfo(new NullPointerException()));
    }

    @Test
    public void testMatch(){
        String regex = NotebookUtil.K8S_REGEX;
        Assert.assertTrue("aa".matches(regex));
        Assert.assertTrue("a-a".matches(regex));
        Assert.assertFalse("a-A".matches(regex));
    }

    @Test
    public void checkUrlContainsToken(){
        Assert.assertFalse(NotebookUtil.checkUrlContainsToken(null));
        Assert.assertFalse(NotebookUtil.checkUrlContainsToken(""));
        Assert.assertFalse(NotebookUtil.checkUrlContainsToken("http://t507os.www.dantegarden.com:32191/?"));
        Assert.assertTrue(NotebookUtil.checkUrlContainsToken("http://t507os.www.dantegarden.com:32191?token=ec21eb536cf3c79236b2290a97cd7e6279db1eeb232c515e"));
    }

    @Test
    public void notebookStartTimeout() throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = df.parse("2020-05-18 15:21:15");
        Assert.assertTrue(NotebookUtil.notebookStartTimeout(startDate));
        Assert.assertTrue(NotebookUtil.notebookStartTimeout(null));
    }

    @Test
    public void getTimeoutSecondLong() {
        System.out.println(NotebookUtil.getTimeoutSecondLong());
    }

    @Test
    public void generateName() {
        Assert.assertEquals("algorithm-0", NotebookUtil.generateName(BizNfsEnum.ALGORITHM,0L));
    }

}
