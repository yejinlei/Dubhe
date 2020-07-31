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

package org.dubhe.task;

import org.dubhe.enums.LogEnum;
import org.dubhe.service.PtImageService;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description 从harbor同步projectName
 * @date 2020-6-23
 **/
@Component
public class HarborProjectNameSyncTask {

    @Autowired
    private PtImageService ptImageService;

    /**
     * 每天晚上11点开始同步
     **/
    @Scheduled(cron = "0 0 23 * * ?")
    public void syncProjectName() {
        LogUtil.info(LogEnum.BIZ_TRAIN, "开始到harbor同步projectName到harbor_project表。。。。。");
        ptImageService.harborImageNameSync();
    }
}
