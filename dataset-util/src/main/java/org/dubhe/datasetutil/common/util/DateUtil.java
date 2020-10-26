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
package org.dubhe.datasetutil.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description 时间 配置类
 * @date 2020-09-17
 */
public class DateUtil {

    private DateUtil(){
    }

    /**
     * 时间格式
     */
    private static final String DATA_FORMAT_STR = "yyyy年MM月dd日 HH时mm分ss秒";

    /**
     * 获取开始时间
     *
     * @return String 返回控制台开始时间
     */              
    public static String getNowStr() {
        return new SimpleDateFormat(DATA_FORMAT_STR).format(new Date());
    }

}
