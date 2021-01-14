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

package org.dubhe.data.service;

import org.dubhe.BaseTest;
import org.dubhe.data.domain.entity.Label;
import org.dubhe.data.service.impl.LabelServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * @description 标签业务层测试类
 * @date 2020-05-12
 */
public class LabelBOServiceImplTest extends BaseTest {

    @Autowired
    private LabelServiceImpl labelService;

    @Test
    public void exist() {
        System.out.println(labelService.exist(8L));
    }

    @Test
    public void save() {
        List<Label> labels = Arrays.asList(
                Label.builder().id(4L).build(),
                Label.builder().id(7L).build(),
                Label.builder().id(9L).build()
        );
        labelService.save(labels, 13L);
    }
}
