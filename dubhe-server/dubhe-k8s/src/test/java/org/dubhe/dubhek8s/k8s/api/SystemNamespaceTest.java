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

import org.dubhe.dubhek8s.domain.vo.NamespaceVO;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description
 * @date 2021-09-14
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class SystemNamespaceTest {

    @Autowired
    SystemNamespaceService systemNamespaceService;

    @Test
    public void findNamespace() {
        NamespaceVO namespace = systemNamespaceService.findNamespace(1L);
        System.out.println(namespace);
    }

}
