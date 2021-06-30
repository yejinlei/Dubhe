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

import org.dubhe.biz.base.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * @description 数字验证工具
 * @date 2020-05-18
 */
public class NumberUtil {

    private static final String REGEX = "^[0-9]*$";

    private NumberUtil() {

    }

    /**
     * 判断是否为数字格式不限制位数
     *
     * @param object 待校验参数
     */
    public static void isNumber(Object object) {
        if (!((Pattern.compile(REGEX)).matcher(String.valueOf(object)).matches())) {
            throw new BusinessException("parameter is incorrect");
        }
    }
}
