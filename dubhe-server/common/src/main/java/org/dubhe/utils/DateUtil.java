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

package org.dubhe.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/**
 * @description 日期工具类
 * @date 2020-06-10
 */
public class DateUtil {

    private DateUtil(){

    }


    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static Timestamp getCurrentTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now());
    }



    /**
     * 获取六小时后时间
     * @return
     */
    public static long getAfterSixHourTime(){
        long l1 = getTimestampOfDateTime(LocalDateTime.now());
        long milli = getTimestampOfDateTime(LocalDateTime.now().plusHours(6));
        return (milli-l1);
    }


    /**
     * LocalDateTime -> long
     * @param localDateTime
     * @return
     */
    public static long getTimestampOfDateTime(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * 获取第二天凌晨时间
     * @return
     */
    public static long getSecondTime(){
        LocalDateTime localDateTime = LocalDateTime.now();
        long l1 = localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();

        LocalDate localDate = LocalDate.now();
        LocalDate localDate1 = localDate.plusDays(1);
        LocalDateTime localDateTime1 = localDate1.atStartOfDay();
        long milli = localDateTime1.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        System.out.println("当前时间到第二天凌晨的毫秒数"+(milli-l1));
        return (milli-l1);
    }

    /**
     * @return 当前字符串时间yyyy-MM-dd HH:mm:ss SSS
     */
    public static String getCurrentTimeStr(){
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return dateFormat.format(date);
    }

    /**
     *
     * @return 当前字符串时间yyyyMMddHHmmss
     */
    public static String getTimestampStr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return df.format(System.currentTimeMillis());
    }

    /**
     * 获取当前时间与目标时间相差秒数
     * @param timestamp 目标时间
     * @return
     */
    public static long getSeconds(Timestamp timestamp) {
        LocalDateTime time = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);
        return duration.abs().getSeconds();
    }

}
