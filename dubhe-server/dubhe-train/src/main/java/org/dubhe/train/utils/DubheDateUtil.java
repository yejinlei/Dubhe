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

package org.dubhe.train.utils;

import java.util.concurrent.TimeUnit;

/**
 * @description 日期工具类
 * @date 2020-5-29
 **/
public class DubheDateUtil {

    private static final int SIXTY = 60;

    /**
     * 将秒数 转为 小时：分钟：秒
     *
     * @param second 秒数
     * @return
     */
    public static String convert2Str(Long second) {
        if (null == second || second < 1) {
            return "";
        }
        Long hh = second / (SIXTY * SIXTY);
        second = second % (SIXTY * SIXTY);
        Long mm = second / SIXTY;
        Long ss = second % SIXTY;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", hh)).append(":").append(String.format("%02d", mm)).append(":").append(String.format("%02d", ss));
        return sb.toString();
    }

    /**
     * 将秒数 转为 小时：分钟：秒
     *
     * @param second 秒数
     * @return
     */
    public static String secondConvertString(Long second) {
        if (null == second || second < 1) {
            return "";
        }
        return String.format(TrainUtil.RUNTIME,
                TimeUnit.MILLISECONDS.toHours(second),
                TimeUnit.MILLISECONDS.toMinutes(second) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(second) % TimeUnit.MINUTES.toSeconds(1)
        );
    }
}
