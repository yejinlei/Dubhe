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

import org.dubhe.base.MagicNumConstant;

/**
 * @description: 计算工具类
 * @create: 2020/6/4 14:53
 */
public class MathUtils {


    /**
     * 字符串整数加法
     * num1+num2
     * @param num1
     * @param num2
     * @return
     */
    public static String add(String num1,String num2){
        return String.valueOf((!RegexUtil.isDigits(num1) ? MagicNumConstant.ZERO:Integer.valueOf(num1)) + (!RegexUtil.isDigits(num2)?MagicNumConstant.ZERO:Integer.valueOf(num2)));
    }
    /**
     * 字符串整数减法
     * num1 - num2
     * @param num1
     * @param num2
     * @return
     */
    public static String reduce(String num1,String num2){
        return String.valueOf((!RegexUtil.isDigits(num1) ? MagicNumConstant.ZERO:Integer.valueOf(num1)) - (!RegexUtil.isDigits(num2)?MagicNumConstant.ZERO:Integer.valueOf(num2)));
    }
}
