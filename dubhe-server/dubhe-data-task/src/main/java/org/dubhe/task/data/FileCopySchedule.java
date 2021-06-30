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

package org.dubhe.task.data;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.handler.ScheduleTaskHandler;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.service.impl.DatasetVersionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description 已发布版本文件复制定时任务
 * @date 2020-06-18
 */
@Component
public class FileCopySchedule {

    @Autowired
    private DatasetVersionServiceImpl datasetVersionService;

    /**
     * 文件复制
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void fileCopy() {
        ScheduleTaskHandler.process(() -> {
            LogUtil.info(LogEnum.BIZ_DATASET, "file copy --- > start");
            datasetVersionService.fileCopy();
            LogUtil.info(LogEnum.BIZ_DATASET, "file copy --- > end");
        });
    }

}
