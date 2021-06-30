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
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.log.handler.ScheduleTaskHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @description 回收无效文件资源定时任务
 * @date 2020-09-21
 */
@Component
public class RecycleInvalidResourcesTask {

    @Autowired
    private NfsConfig nfsConfig;

    @Autowired
    private RecycleTaskService recycleTaskService;

    /**
     * 文件存储临时文件根目录
     */
    public static final String UPLOAD_TEMP = File.separator + "upload-temp";

    /**
     * 每天晚上12点定时回收无效文件资源
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void process() {
        ScheduleTaskHandler.process(() -> {
            String sourcePath = nfsConfig.getRootDir() + nfsConfig.getBucket() + UPLOAD_TEMP;
            recycleTaskService.deleteInvalidResourcesByCMD(sourcePath);
        });
    }
}