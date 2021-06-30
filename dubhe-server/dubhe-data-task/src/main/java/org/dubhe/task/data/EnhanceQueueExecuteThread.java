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

import lombok.extern.slf4j.Slf4j;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.data.service.impl.DatasetEnhanceServiceImpl;
import org.dubhe.data.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @description 增强完成任务队列处理线程
 * @date 2020-09-20
 */
@Slf4j
@Component
public class EnhanceQueueExecuteThread implements Runnable {

    @Autowired
    private DatasetEnhanceServiceImpl datasetEnhanceService;
    @Autowired
    private TaskUtils taskUtils;

    /**
     * 增强算法执行中任务队列
     */
    private static final String IMGPROCESS_START_QUEUE = "imgProcess_processing";
    /**
     * 增强算法未执行任务队列
     */
    private static final String IMGPROCESS_PENDING_QUEUE = "imgProcess_unprocessed";

    /**
     * 启动增强任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "增强完成任务处理队列");
        thread.start();
    }

    /**
     * 增强任务处理线程方法
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (datasetEnhanceService.getEnhanceFinishedTask()) {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.ONE_HUNDRED);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get enhance finish task failed:{}", e);
            }
        }
    }

    /**
     * 判断增强任务是否过期
     */
    @Scheduled(cron = "*/15 * * * * ?")
    public void timerEnhanceFinished() {
        LogUtil.info(LogEnum.BIZ_DATASET, "enhance schedule --- > start");
        taskUtils.restartTask(IMGPROCESS_START_QUEUE, IMGPROCESS_PENDING_QUEUE);
        LogUtil.info(LogEnum.BIZ_DATASET, "enhance schedule --- > end");
    }

}
