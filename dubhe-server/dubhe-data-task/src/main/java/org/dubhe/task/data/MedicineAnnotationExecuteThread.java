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
import org.dubhe.dcm.service.MedicineAnnotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @description 医学影像自动标注任务处理
 * @date 2020-11-16
 */
@Slf4j
@Component
public class MedicineAnnotationExecuteThread implements Runnable {

    @Autowired
    private MedicineAnnotationService medicineAnnotationService;

    /**
     * 启动医学自动标注任务处理线程
     */
    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "医学自动标注任务处理队列");
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(medicineAnnotationService.finishAuto()){
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.ONE_HUNDRED);
                } else {
                    TimeUnit.MILLISECONDS.sleep(MagicNumConstant.THREE_THOUSAND);
                }
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get medical finished task failed:{}", e);
            }
        }
    }

}
