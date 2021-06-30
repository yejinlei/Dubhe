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

package org.dubhe.biz.base.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @description 日期工具类
 * @date 2020-11-26
 */
public class DateUtil {

    private DateUtil() {

    }

    /**
     * 北京时间
     */
    public final static String DEFAULT_TIME_ZONE = "GMT+8";


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
    public static long getAfterSixHourTime() {
        long l1 = getTimestampOfDateTime(LocalDateTime.now());
        long milli = getTimestampOfDateTime(LocalDateTime.now().plusHours(6));
        return (milli - l1);
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
    public static long getSecondTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        long l1 = localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();

        LocalDate localDate = LocalDate.now();
        LocalDate localDate1 = localDate.plusDays(1);
        LocalDateTime localDateTime1 = localDate1.atStartOfDay();
        long milli = localDateTime1.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        return (milli - l1);
    }

    /**
     * @return 当前字符串时间yyyy-MM-dd HH:mm:ss SSS
     */
    public static String getCurrentTimeStr() {
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
     * 获取垃圾回收具体的预执行时间
     *
     * @param afterDay 延迟执行天数
     * @return Timestamp 具体的执行时间
     */
    public static Timestamp getRecycleTime(int afterDay){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.add(Calendar.DATE, afterDay);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 01:00:00");

        return Timestamp.valueOf(sdf.format(calendar.getTime()));
    }
}