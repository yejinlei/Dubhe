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
package org.dubhe.biz.log.handler;


import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;

/**
 * @description 定时任务处理器, 主要做日志标识
 * @date 2020-08-13
 */
public class ScheduleTaskHandler {


    public static void process(Handler handler) {
        LogUtil.startScheduleTrace();
        try {
            handler.run();
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_SYS, "There is something wrong in schedule task handler ：{}", e);
        } finally {
            LogUtil.cleanTrace();
        }
    }


    public interface Handler {
        void run();
    }
}
