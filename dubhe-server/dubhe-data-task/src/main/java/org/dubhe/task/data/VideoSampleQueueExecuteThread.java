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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @description 视频采样任务完成处理
 * @date 2020-09-11
 */
@Slf4j
@Component
public class VideoSampleQueueExecuteThread implements Runnable {

    @Autowired
    private FileService fileService;

    /**
     * 启动视频采样任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "采样完成任务处理队列");
        thread.start();
    }

    /**
     * 视频采样任务处理线程方法
     */
    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            try {
                fileService.videoSample();
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get vidwoSample finish or failed task failed:{}", e);
                TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
            }
        }
    }

    /**
     * 采样任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void expireSampleTask() {
        fileService.expireSampleTask();
    }

}
