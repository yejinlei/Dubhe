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

/**
 * @description 计算工具类
 * @date 2020-06-04
 */
public class MathUtils {

    private static String FLOAT_DIVISION_FORMATE = "%1$0";

    /**
     * 字符串整数加法
     * num1+num2
     * @param num1
     * @param num2
     * @return
     */
    public static String add(String num1, String num2) {
        return String.valueOf((!RegexUtil.isDigits(num1) ? MagicNumConstant.ZERO : Integer.valueOf(num1)) + (!RegexUtil.isDigits(num2) ? MagicNumConstant.ZERO : Integer.valueOf(num2)));
    }

    /**
     * 字符串整数减法
     * num1 - num2
     * @param num1
     * @param num2
     * @return
     */
    public static String reduce(String num1, String num2) {
        return String.valueOf((!RegexUtil.isDigits(num1) ? MagicNumConstant.ZERO : Integer.valueOf(num1)) - (!RegexUtil.isDigits(num2) ? MagicNumConstant.ZERO : Integer.valueOf(num2)));
    }

    /**
     *
     * 浮点数除法 num1/num2
     * @param num1
     * @param num2
     * @param decimal 结果小数位数
     * @return
     */
    public static Float floatDivision(String num1, String num2, Integer decimal) {
        if (!RegexUtil.isFloat(num1) || !RegexUtil.isFloat(num2)) {
            return null;
        }
        if (Float.valueOf(num2).equals(0f)) {
            return null;
        }
        if (decimal != null && decimal > MagicNumConstant.ZERO) {
            Integer d = Integer.valueOf(MagicNumConstant.ONE + String.format(FLOAT_DIVISION_FORMATE + decimal + "d", MagicNumConstant.ZERO));
            return (float) (Math.round((Float.valueOf(num1) / Float.valueOf(num2)) * d)) / d;
        } else {
            return Float.valueOf(num1) / Float.valueOf(num2);
        }
    }
}
