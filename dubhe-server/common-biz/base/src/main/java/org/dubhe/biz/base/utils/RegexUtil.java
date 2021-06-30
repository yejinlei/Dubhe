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

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description 正则匹配工具类
 * @date 2020-04-23
 */
@Slf4j
public class RegexUtil {
    private static final String DIGIT = "^[0-9]*$";
    private static final String FLOAT = "^[-+]?[0-9]*\\.?[0-9]+$";
    /**
     * str待匹配文本
     * regex 正则表达式
     *返回str中匹配regex的第一个子串
     */
    public static String getMatcher(String str,String regex) {
        try{
            if (StringUtils.isEmpty(str) || StringUtils.isEmpty(regex)){
                return "";
            }
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(str);
            matcher.find();
            return matcher.group();
        }catch (IllegalStateException e){
            log.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * 数字匹配
     * @param str
     * @return
     */
    public static boolean isDigits(String str){
        if (StringUtils.isEmpty(str)){
            return false;
        }
        return str.matches(DIGIT);
    }

    /**
     * 浮点数匹配
     * @param str
     * @return
     */
    public static boolean isFloat(String str){
        if (StringUtils.isEmpty(str)){
            return false;
        }
        return str.matches(FLOAT);
    }
}
