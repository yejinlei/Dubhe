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

package org.dubhe.data.schedule;

import org.dubhe.data.service.impl.AnnotationServiceImpl;
import org.dubhe.data.service.impl.DatasetEnhanceServiceImpl;
import org.dubhe.data.service.impl.DatasetServiceImpl;
import org.dubhe.data.service.impl.TaskServiceImpl;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description 数据集状态更新定时任务
 * @date 2020-04-14
 */
@Component
public class DatasetSyncSchedule {

    @Autowired
    private DatasetServiceImpl datasetService;
    @Autowired
    private TaskServiceImpl taskService;
    @Autowired
    private AnnotationServiceImpl annotationService;
    @Autowired
    private DatasetEnhanceServiceImpl datasetEnhanceService;

    /**
     * 数据集状态异步更新
     */
    @Scheduled(cron = "*/20 * * * * ?")
    public void syncStatus() {
        LogUtil.info(LogEnum.BIZ_DATASET, "start to sync dataset status");
        datasetService.syncStatus();
        LogUtil.info(LogEnum.BIZ_DATASET, "end sync dataset status");
    }


    /**
     * 定时任务失败检测
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void abandonTask() {
        LogUtil.info(LogEnum.BIZ_DATASET, "start to abandon task");
        taskService.fail();
        LogUtil.info(LogEnum.BIZ_DATASET, "end abandon task");
    }

    /**
     * 定时移除失败任务
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void removeTask() {
        LogUtil.info(LogEnum.BIZ_DATASET, "start to remove task");
        //自动标注移除失败任务
        annotationService.doRemoveTask(null);
        //数据扩容移除失败任务
        datasetEnhanceService.doRemoveTask(null);
        LogUtil.info(LogEnum.BIZ_DATASET, "end remove task");
    }

}
