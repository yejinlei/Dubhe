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
package org.dubhe.admin.task;

import org.dubhe.admin.service.RecycleTaskService;
import org.dubhe.biz.base.constant.UserConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.handler.ScheduleTaskHandler;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.recycle.domain.entity.Recycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 回收资源定时任务
 * @date 2020-09-21
 */
@Component
public class RecycleResourcesTask {

    @Autowired
    private RecycleTaskService recycleTaskService;

    /**
     * 每天凌晨1点定时回收已删除资源
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void process() {
        ScheduleTaskHandler.process(() -> {
            List<Recycle> recycleTaskList = recycleTaskService.getRecycleTaskList();
            for (Recycle recycle : recycleTaskList) {
                // one by one
                try {
                    recycleTaskService.recycleTask(recycle, UserConstant.ADMIN_USER_ID);
                } catch (Exception e) {
                    LogUtil.error(LogEnum.GARBAGE_RECYCLE, "scheduled recycle task failed，exception {}", e);
                }
            }
        });
    }

}
