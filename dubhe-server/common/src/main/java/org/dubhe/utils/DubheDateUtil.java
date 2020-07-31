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

package org.dubhe.utils;

/**
 * @description 日期工具类
 * @date 2020-5-29
 **/
public class DubheDateUtil {

    private static final int SIXTY = 60;

    /**
     * 将时间 秒 转为 小时
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
}
