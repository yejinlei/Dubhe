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

import org.dubhe.enums.BizEnum;
import org.dubhe.utils.K8sCallBackTool;
import org.dubhe.utils.K8sNameTool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description K8sCallBackToolTest测试类
 * @date 2020-05-28
 */
@ActiveProfiles(value = "notebook")
@RunWith(SpringRunner.class)
@SpringBootTest(classes= AppRun.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class K8sCallBackToolTest{

    @Autowired
    private K8sCallBackTool k8sCallBackTool;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Test
    public void token(){
        System.out.println(k8sCallBackTool.generateToken());
        Assert.assertTrue(k8sCallBackTool.validateToken(k8sCallBackTool.generateToken()));
    }

    @Test
    public void getPodCallbackUrl(){
        Assert.assertEquals("http://xxx.xxx.xxx.xxx:xxxx/api/k8s/callback/pod/notebook",k8sCallBackTool.getPodCallbackUrl(k8sNameTool.getPodLabel(BizEnum.NOTEBOOK)));
    }

}
