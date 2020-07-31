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

import org.dubhe.data.service.FileService;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description 视频采样定时任务
 * @date 2020-05-22
 */
@Component
public class VideoSampleSchedule {

    @Autowired
    private FileService fileService;

    /**
     * 视频采样
     */
    @Async("executor")
    @Scheduled(cron = "*/15 * * * * ?")
    public void timerVideoSample() {
        LogUtil.info(LogEnum.BIZ_DATASET, "video sample --- > start");
        fileService.videoSample();
        LogUtil.info(LogEnum.BIZ_DATASET, "video sample --- > end");
    }

}
