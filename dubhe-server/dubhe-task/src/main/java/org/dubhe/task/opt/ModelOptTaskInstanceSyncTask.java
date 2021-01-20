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

package org.dubhe.task.opt;

import lombok.extern.slf4j.Slf4j;
import org.dubhe.service.ModelOptTaskInstanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 模型优化实例状态同步定时任务
 * @date 2020-06-08
 */
@Slf4j
@Component
public class ModelOptTaskInstanceSyncTask {

    @Resource
    private ModelOptTaskInstanceService modelOptTaskInstanceService;

    /**
     * 每分钟0秒同步模型优化实例的状态
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncInstanceStatus() {
        log.info("ModelOptTaskInstanceSyncTask#syncInstanceStatus start");
        modelOptTaskInstanceService.syncInstanceStatus();
        log.info("ModelOptTaskInstanceSyncTask#syncInstanceStatus end");
    }
}
