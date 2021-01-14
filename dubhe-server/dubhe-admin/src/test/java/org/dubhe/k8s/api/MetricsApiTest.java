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

package org.dubhe.k8s.api;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description MetricsApiTest测试类
 * @date 2020-05-22
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MetricsApiTest {
    @Autowired
    private MetricsApi metricsApi;

    @Test
    public void getNodeMetrics(){
        System.out.println(JSON.toJSONString(metricsApi.getNodeMetrics()));
    }

    @Test
    public void getContainerMetrics(){
        System.out.println(JSON.toJSONString(metricsApi.getContainerMetrics("namespace-1")));
    }

    @Test
    public void getPodMetricsRealTime(){
        System.out.println(JSON.toJSONString(metricsApi.getPodMetricsRealTime("namespace-1","serving-rn-212")));
    }

}
