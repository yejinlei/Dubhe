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


import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @description: UTC时间转换CST时间工具类
 * @create: 2020/5/20 12:10
 */
@Slf4j
public class TimeTransferUtil {
    /**
     * @param utcTime
     * @return cstTime
     */
    public static String cstTransfer(String utcTime){
        Date utcDate = null;
        /**2020-05-20T03:13:22Z 对应的时间格式 yyyy-MM-dd'T'HH:mm:ss'Z'**/
        SimpleDateFormat utcSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        try {
            utcDate = utcSimpleDateFormat.parse(utcTime);
        } catch (ParseException e) {
            log.info(e.getMessage());
            return null;
        }
        /**System.out.println("UTC时间："+date);**/
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utcDate);
        calendar.set(Calendar.HOUR,calendar.get(Calendar.HOUR)+8);
        SimpleDateFormat cstSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date cstDate = calendar.getTime();
        String cstTime = cstSimpleDateFormat.format(calendar.getTime());
        return cstTime;
    }
}
