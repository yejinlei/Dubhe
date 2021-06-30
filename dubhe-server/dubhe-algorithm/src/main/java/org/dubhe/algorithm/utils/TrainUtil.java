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

package org.dubhe.algorithm.utils;

import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;

import java.sql.Timestamp;

/**
 * @description 训练任务工具类
 * @date 2020-07-14
 */
public class TrainUtil {


    private TrainUtil() {

    }

    public static final String REGEXP = "^[a-zA-Z0-9\\-\\_\\u4e00-\\u9fa5]+$";
    public static final String REGEXP_NAME = "^[a-zA-Z0-9\\-\\_]+$";
    public static final String REGEXP_TAG = "^[a-zA-Z0-9\\-\\_\\.]+$";

    public static final String RUNTIME = "%02d:%02d:%02d";
    public static final String FOUR_DECIMAL = "%04d";
    public static final String FOUR_TWO = "%.2f";

    public static final int NUMBER_ZERO = 0;
    public static final int NUMBER_ONE = 1;
    public static final int NUMBER_TWO = 2;
    public static final int NUMBER_SEVEN = 7;
    public static final int NUMBER_EIGHT = 8;
    public static final int NUMBER_TWENTY = 20;
    public static final int NUMBER_THIRTY_TWO = 32;
    public static final int NUMBER_SIXTY_FOUR = 64;
    public static final int NUMBER_ONE_HUNDRED_AND_TWENTY_SEVEN = 127;
    public static final int NUMBER_ONE_HUNDRED_AND_TWENTY_EIGHT = 128;
    public static final int NUMBER_ONE_HUNDRED_AND_SIXTY_EIGHT = 168;
    public static final int NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE = 255;
    public static final int NUMBER_ONE_THOUSAND = 1000;
    public static final int NUMBER_ONE_THOUSAND_AND_TWENTY_FOUR = 1024;

    // 初始化训练时间
    public static final String INIT_RUNTIME = SymbolConstant.BLANK;

    /**
     *  获取延时时间
     *  @param delayTime 延时时间（单位为小时）
     *  @return 延时时间
     */
    public static Timestamp getDelayTime(Integer delayTime) {
        return new Timestamp(System.currentTimeMillis() + delayTime * MagicNumConstant.SIXTY * MagicNumConstant.SIXTY * MagicNumConstant.ONE_THOUSAND);
    }

    /**
     *  获取倒计时
     *  @param delayTime 延时时间(单位为毫秒)
     *  @return 倒计时（单位为分钟）
     */
    public static Integer getCountDown(Long delayTime) {
        return (int) ((delayTime - System.currentTimeMillis()) / (MagicNumConstant.SIXTY * MagicNumConstant.ONE_THOUSAND));
    }

}
