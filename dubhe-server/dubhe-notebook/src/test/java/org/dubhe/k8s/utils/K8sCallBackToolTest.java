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

package org.dubhe.k8s.utils;


import org.dubhe.notebook.DubheNotebookApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.constraints.AssertTrue;


/**
 * @description K8sCallBackToolTest测试类
 * @date 2020-05-28
 */
@SpringBootTest(classes = DubheNotebookApplication.class)
public class K8sCallBackToolTest {

    @Autowired
    private K8sCallBackTool k8sCallBackTool;

    @Autowired
    private K8sNameTool k8sNameTool;

    @Test
    public void token(){
        System.out.println(k8sCallBackTool.generateToken());
        assert k8sCallBackTool.validateToken(k8sCallBackTool.generateToken());
    }



}
