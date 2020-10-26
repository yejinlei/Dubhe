/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.action;

import lombok.extern.slf4j.Slf4j;
import org.onebrain.operator.watcher.KubeWatcherManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @description Operator运行入口
 * @date 2020-09-23
 */
@Component
@Slf4j
public class OperatorRunner implements ApplicationRunner {

    @Autowired
    private DistributeTrainOperatorManager operatorManager;

    @Autowired
    private KubeWatcherManager watcherManager;

    /**
     * spring 容器完全启动后 注册operator运行逻辑
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //检查crd是否已存在，如果不存在则创建
        operatorManager.createCrdIfNotExists();

        //job监控者启动
        watcherManager.startWatching();
        log.info("job watcher is running");

        //初始化informer
        operatorManager.initInformer();
    }
}
