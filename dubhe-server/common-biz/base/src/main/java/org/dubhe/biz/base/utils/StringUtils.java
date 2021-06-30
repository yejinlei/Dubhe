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

import com.alibaba.fastjson.JSON;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.SymbolConstant;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description 字符串工具类, 继承org.apache.commons.lang3.StringUtils类
 * @date 2020-03-25
 */
@Slf4j
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final char SEPARATOR = '_';

    private static final String UNKNOWN = "unknown";

    private static Pattern linePattern = Pattern.compile("_(\\w)");


    /**
     * 驼峰命名法工具
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.toLowerCase();

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 驼峰命名法工具
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * 驼峰命名法工具
     *
     * @return toCamelCase(" hello_world ") == "helloWorld"
     * toCapitalizeCamelCase("hello_world") == "HelloWorld"
     * toUnderScoreCase("helloWorld") = "hello_world"
     */
    static String toUnderScoreCase(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }


    /**
     * 获取ip地址
     *
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String comma = ",";
        String localhost = "127.0.0.1";
        if (ip.contains(comma)) {
            ip = ip.split(",")[0];
        }
        if (localhost.equals(ip)) {
            // 获取本机真正的ip地址
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return ip;
    }

    public static String getBrowser(HttpServletRequest request) {
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        Browser browser = userAgent.getBrowser();
        return browser.getName();
    }


    /**
     * 获得当天是周几
     */
    public static String getWeekDay() {
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 字符串匹配工具类(正则表达式)
     *
     * @param str
     * @param regexp
     * @return
     */
    public static boolean stringMatch(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        return pattern.matcher(str).find();
    }

    /**
     * 字符串长度不够补位
     *
     * @param sourceStr 源字符串
     * @param length    最终长度
     * @param type      补位方式 0:左边 1:右边
     * @return
     */
    public static String stringFillIn(String sourceStr, int length, int type) {
        if (sourceStr.length() > length) {
            return sourceStr;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < length - sourceStr.length(); i++) {
            stringBuilder.append("0");
        }

        if (type == MagicNumConstant.ZERO) {
            return stringBuilder.toString() + sourceStr;
        } else {
            return sourceStr + stringBuilder.toString();
        }
    }


    /**
     * 从头截取string字符串
     *
     * @param str             被截取字符串
     * @param truncationIndex 0 -> 截取长度
     * @return
     */
    public static String truncationString(String str, int truncationIndex) {
        if (str == null) {
            return SymbolConstant.BLANK;
        } else if (truncationIndex < 1
                || str.length() <= truncationIndex) {
            return str;
        }
        return str.substring(0, truncationIndex);
    }

    /**
     * 字符串驼峰转下划线
     *
     * @param str 被转字符串
     * @return String 转换后的字符串
     */
    public static String humpToLine(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char character : chars) {
            if (Character.isUpperCase(character)) {
                stringBuilder.append("_");
                character = Character.toLowerCase(character);
            }
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }

    /**
     * 字符串下划线转驼峰
     *
     * @param str 被转字符串
     * @return String 转换后的字符串
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 字符串截取前
     * @param str
     * @return
     */
    public static String substringBefore(String str, String separator) {

        if (!isEmpty(str) && separator != null) {
            if (separator.isEmpty()) {
                return "";
            } else {
                int pos = str.indexOf(separator);
                return pos == -1 ? str : str.substring(0, pos);
            }
        } else {
            return str;
        }
    }

    /**
     * 字符串截取后
     * @param str
     * @return
     */
    public static String substringAfter(String str, String separator) {

        if (isEmpty(str)) {
            return str;
        } else if (separator == null) {
            return "";
        } else {
            int pos = str.indexOf(separator);
            return pos == -1 ? "" : str.substring(pos + separator.length());
        }
    }

    /**
     * 获取UUID字符串
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace(SymbolConstant.HYPHEN, SymbolConstant.BLANK);
    }

    /**
     * 生成4位随机[a-z]字符串
     *
     * @return
     */
    public static String getRandomString() {
        return RandomStringUtils.randomAlphabetic(NumberConstant.NUMBER_4).toLowerCase();
    }

    /**
     * 生成时间戳 + 4位随机数
     *
     * @return
     */
    public static String getTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = df.format(System.currentTimeMillis());
        return dateStr + getRandomString();
    }

    /**
     * 往json字符串的map添加键值对
     *
     * @param key 键
     * @param value 值 非null
     * @param jsonStringMap json字符串的map
     * @return
     */
    public static String putIntoJsonStringMap(String key, String value, String jsonStringMap) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return jsonStringMap;
        }
        try {
            if (StringUtils.isEmpty(jsonStringMap)) {
                jsonStringMap = JSON.toJSONString(new HashMap<String, String>());
            }
            Map<String, String> map = JSON.parseObject(jsonStringMap, Map.class);
            map.put(key, value);
            return JSON.toJSONString(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jsonStringMap;
    }

    /**
     * 删除json字符串的键值对
     *
     * @param key 键
     * @param jsonStringMap json字符串的map
     * @return
     */
    public static String removeFromJsonStringMap(String key, String jsonStringMap) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(jsonStringMap)) {
            return jsonStringMap;
        }
        try {
            Map<String, String> map = JSON.parseObject(jsonStringMap, Map.class);
            map.remove(key);
            return JSON.toJSONString(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jsonStringMap;
    }

    /**
     * 从json字符串的map中获取value
     *
     * @param key 键
     * @param jsonStringMap json字符串的map
     * @return value
     */
    public static String getValueFromJsonStringMap(String key, String jsonStringMap) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(jsonStringMap)) {
            return null;
        }
        try {
            Map<String, String> map = JSON.parseObject(jsonStringMap, Map.class);
            return map.get(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
