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
import org.checkerframework.checker.units.qual.A;
import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.dubhek8s.service.SystemNodeService;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * @description MetricsApiTest测试类
 * @date 2020-05-22
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class MetricsApiTest {
    @Autowired
    private MetricsApi metricsApi;
    @Autowired
    private SystemNodeService systemNodeService;

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
        System.out.println(JSON.toJSONString(metricsApi.getPodMetricsRealTime("namespace-1","serving-rn-bapq279-7pee8mgd-7d65cc8d97-kdvvk")));
    }

    @Test
    public void getPodMetricsRealTimeByPodName(){
        System.out.println(JSON.toJSONString(metricsApi.getPodMetricsRealTimeByPodName("namespace-23",Arrays.asList("notebook-rn-20210510082610891du31-vq3nz-0","notebook-rn-202104300726245401ko3-u2pb5-0"))));
    }

    @Test
    public void getPodHistoryMetrics(){
        System.out.println(JSON.toJSONString(metricsApi.getPodRangeMetrics(new PodQueryDTO("namespace-1","notebook-rn-20210303015534256intm",null,null,null,10))));
    }

    @Test
    public void getPodRangeMetricsByPodName(){
        System.out.println(JSON.toJSONString(metricsApi.getPodRangeMetricsByPodName(new PodQueryDTO("namespace-1",null, Arrays.asList("serving-rn-bapq279-7pee8mgd-7d65cc8d97-kdvvlk"),System.currentTimeMillis()/1000 - 360,System.currentTimeMillis()/1000,5))));
    }

    @Test
    public void findNodes(){
        System.out.println(JSON.toJSONString(systemNodeService.findNodes()));
    }
}
