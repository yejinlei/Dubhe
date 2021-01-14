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
package org.dubhe.task.system;

import cn.hutool.core.util.StrUtil;
import org.dubhe.base.ScheduleTaskHandler;
import org.dubhe.domain.entity.RecycleTask;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.RecycleModuleEnum;
import org.dubhe.enums.RecycleTypeEnum;
import org.dubhe.service.AbstractGlobalRecycle;
import org.dubhe.service.RecycleTaskService;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
            List<RecycleTask> recycleTaskList = recycleTaskService.getRecycleTaskList();
            routeRecycleStrategy(recycleTaskList);
        });
    }


    public void routeRecycleStrategy(List<RecycleTask> recycleTaskList) {
        for (RecycleTask recycleTask : recycleTaskList) {
            if (Objects.equals(RecycleTypeEnum.FILE.getCode(), recycleTask.getRecycleType())) {
                LogUtil.info(LogEnum.GARBAGE_RECYCLE, "开始回收id为[{}]的任务。。。", recycleTask.getId());
                // 回收文件资源
                recycleTaskService.deleteFileByCMD(recycleTask);
            } else {
                if (RecycleModuleEnum.BIZ_DATASET.getValue().equals(recycleTask.getRecycleModule()) && StrUtil.isNotEmpty(recycleTask.getRecycleCustom())) {
                    AbstractGlobalRecycle recycle = SpringContextHolder.getBean(recycleTask.getRecycleCustom());
                    try {
                        recycle.clear(recycleTask);
                        recycleTaskService.updateRecycleStatus(recycleTask, true);
                    } catch (Exception e) {
                        LogUtil.error(LogEnum.BIZ_DATASET, "Class:{} recycleTaskService method clear is error," +
                                "param datasetId is {} ,error info is {}", recycleTask.getRecycleCustom(), recycleTask.getRecycleCondition(), e);
                        recycleTaskService.updateRecycleStatus(recycleTask, false);
                    }
                }
            }

        }
    }
}
