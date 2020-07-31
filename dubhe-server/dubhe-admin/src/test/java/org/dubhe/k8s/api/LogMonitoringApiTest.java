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

package org.dubhe.k8s.api;

import org.dubhe.AppRun;
import org.dubhe.k8s.domain.bo.LogMonitoringBO;
import org.dubhe.k8s.domain.vo.LogMonitoringVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @description LogMonitoringApiTest测试类
 * @date 2020-05-12
 */
@SpringBootTest(classes= AppRun.class)
@RunWith(SpringRunner.class)
public class LogMonitoringApiTest {
    @Resource
    LogMonitoringApi logMonitoringApi;


    /**通过资源名称查找日志信息**/
    @Test
    public void searchLog() {
        int from = 1;
        /**size为0表示无限制**/
        int size = 200;
        LogMonitoringBO logMonitoringBo = new LogMonitoringBO();
        logMonitoringBo.setIndexName("logstash-*");
        logMonitoringBo.setResourceName("train-1-20200713103822-v0013");
        logMonitoringBo.setNamespace("namespace-1");

        LogMonitoringVO logMonitoringVO = logMonitoringApi.searchLog(from, size, logMonitoringBo);

    }

    @Test
    public void addlog(){

        logMonitoringApi.addLogsToEs("podName", "namespace");

    }

}
