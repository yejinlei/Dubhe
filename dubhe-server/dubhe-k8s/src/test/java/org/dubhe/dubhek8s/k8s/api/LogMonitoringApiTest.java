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

import org.dubhe.dubhek8s.DubheK8sApplication;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @description LogMonitoringApiTest测试类
 * @date 2020-05-12
 */
@SpringBootTest(classes= DubheK8sApplication.class)
@RunWith(SpringRunner.class)
public class LogMonitoringApiTest {
    @Resource
    LogMonitoringApi logMonitoringApi;


    /**通过资源名称查找日志信息**/
    @Test
    public void searchLogByResName() {
        int from = 1;
        /**size为0表示无限制**/
        int size = 200;

        LogMonitoringBO logMonitoringBo = new LogMonitoringBO();
        logMonitoringBo.setResourceName("train-1-20200803170114-v0033");
        logMonitoringBo.setNamespace("namespace-1");

        LogMonitoringVO logMonitoringVO = logMonitoringApi.searchLogByResName(from, size, logMonitoringBo);

    }

    /**通过Pod名称查找日志信息**/
    @Test
    public void searchLogByPodName() {
        int from = 1;
        /**size为0表示无限制**/
        int size = 200;

        LogMonitoringBO logMonitoringBo = new LogMonitoringBO();
        logMonitoringBo.setPodName("train-1-20200828135251-v0013-5zouu-master-8npcd-95d96");
        logMonitoringBo.setNamespace("namespace-1");
        logMonitoringBo.setBeginTimeMillis(0L);
        logMonitoringBo.setLogKeyword("training mission begins");
        logMonitoringBo.setEndTimeMillis(1601347634000L);

        LogMonitoringVO logMonitoringVO = logMonitoringApi.searchLogByPodName(from, size, logMonitoringBo);

    }

    @Test
    public void addlog(){

        //logMonitoringApi.addLogsToEs("podName", "namespace");
        logMonitoringApi.addLogsToEs("train-1-20200915103934-v0055-3ppzh-master-untg7-ndnq8", "namespace-1",new ArrayList(){{
            add("Container is being created");
            add("Container is being created");
        }
        });

    }

    @Test
    public void searchLogCountByPodName(){
        LogMonitoringBO logMonitoringBo = new LogMonitoringBO();
        logMonitoringBo.setPodName("train-1-20200828135251-v0013-5zouu-master-8npcd-95d96");
        logMonitoringBo.setNamespace("namespace-1");
        Long count = logMonitoringApi.searchLogCountByPodName(logMonitoringBo);
        System.out.println(count);
    }

    @Test
    public void addTadlLog(){
        logMonitoringApi.addTadlLogsToEs(1L,"Once I was a wooden boy");
    }

    @Test
    public void searchTadlLogById(){
        LogMonitoringVO logMonitoringVO = logMonitoringApi.searchTadlLogById(1, 10, 1L);
    }

}
