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

package org.dubhe.dubhek8s.k8s.api;

import com.alibaba.fastjson.JSON;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.dubhek8s.service.SystemNodeService;
import org.dubhe.k8s.domain.dto.NodeInfoDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @description SystemNodeTest测试类
 * @date 2021-09-18
 */
@SpringBootTest(classes= DubheK8sApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SystemNodeTest {
    @Resource
    private SystemNodeService systemNodeService;

    @Test
    public void findNodesIsolation() {
        System.out.println("findNodesIsolation = "+ JSON.toJSONString(systemNodeService.findNodesIsolation()));
    }

    @Test
    public void editNodeInfo() {
        NodeInfoDTO nodeInfoDTO = new NodeInfoDTO();
        nodeInfoDTO.setName("qjy-ai07");
        nodeInfoDTO.setRemark("");
        System.out.println("editNodeInfo = "+ JSON.toJSONString(systemNodeService.editNodeInfo(nodeInfoDTO)));
    }
}
