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

import org.dubhe.biz.base.constant.MagicNumConstant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @description 时间格式转换工具类
 * @date 2020-05-20
 */
public class TimeTransferUtil {

    private static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";

    /**
     * Date转换为UTC时间
     *
     * @param date
     * @return utcTime
     */
    public static String dateTransferToUtc(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        /**UTC时间与CST时间相差8小时**/
        calendar.set(Calendar.HOUR,calendar.get(Calendar.HOUR) - MagicNumConstant.EIGHT);
        SimpleDateFormat utcSimpleDateFormat = new SimpleDateFormat(UTC_FORMAT);
        Date utcDate = calendar.getTime();
        return utcSimpleDateFormat.format(utcDate);
    }
}
