/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

/**
 * @description json工具类
 * @date 2020-09-24
 */
public class FastjsonUtils {

    private static final SerializerFeature[] FEATURES = {
        // 输出空置字段
        SerializerFeature.WriteMapNullValue,
        //日期类型用日期字符串 yyyy-MM-dd HH:mm:ss
        SerializerFeature.WriteDateUseDateFormat,
        // list字段如果为null，输出为[]，而不是null
        SerializerFeature.WriteNullListAsEmpty,
        // 数值字段如果为null，输出为0，而不是null
        SerializerFeature.WriteNullNumberAsZero,
        // Boolean字段如果为null，输出为false，而不是null
        SerializerFeature.WriteNullBooleanAsFalse,
        // 字符类型字段如果为null，输出为""，而不是null
        SerializerFeature.WriteNullStringAsEmpty
    };

    /**
     * 将对象转为json
     * @param object
     * @return json的String
     */
    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object, FEATURES);
    }

    /**
     * 将对象转为json(无循环引用)
     * @param object
     * @return json的String
     */
    public static String toJSONNoFeatures(Object object) {
        return JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 将json转为对象
     * @param text
     * @return 对象
     */
    public static Object toBean(String text) {
        return JSON.parse(text);
    }

    /**
     * 将json转为对象
     * @param text 文本字符串
     * @param clazz 类型
     * @param <T> 泛型
     * @return 泛型对象
     */
    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     *  转换为数组
     * @param text 文本字符串
     * @return 泛型对象
     */
    public static <T> Object[] toArray(String text) {
        return toArray(text, null);
    }

    /**
     *  转换为数组
     * @param text 文本字符串
     * @param clazz 类型
     * @return
     */
    public static <T> Object[] toArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz).toArray();
    }

    /**
     * 转换为List
     * @param text 文本字符串
     * @param clazz 类型
     * @return
     */
    public static <T> List<T> toList(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * 将string转化为序列化的json字符串
     * @param text 文本字符串
     * @return json对象
     */
    public static Object textToJson(String text) {
        Object objectJson  = JSON.parse(text);
        return objectJson;
    }

    /**
     * json字符串转化为map
     * @param text json字符串
     * @return Map集合
     */
    public static <K, V> Map<K, V> stringToCollect(String text) {
        Map<K, V> m = (Map<K, V>) JSONObject.parseObject(text);
        return m;
    }

    /**
     * 转换JSON字符串为对象
     * @param jsonData json字符串
     * @param clazz 转换目标对象的类型
     * @return json对象
     */
    public static Object convertJsonToObject(String jsonData, Class<?> clazz) {
        return JSONObject.parseObject(jsonData, clazz);
    }

    /**
     * 将map转化为string
     * @param m Map集合
     * @return 字符串
     */
    public static <K, V> String collectToString(Map<K, V> m) {
        String s = JSONObject.toJSONString(m);
        return s;
    }

    /**
     * json字符串转化为map
     *
     * @param text 字符串
     * @return Map 对象
     */
    public static Map stringToMap(String text) {
        Map m = JSONObject.parseObject(text);
        return m;
    }

    /**
     * 将map转化为string
     *
     * @param m Map集合
     * @return 字符串
     */
    public static String mapToString(Map m) {
        String s = JSONObject.toJSONString(m);
        return s;
    }

    /**
     * 把对象转换为指定对象
     * @param source 原对象
     * @param target 目标class
     * @param <T> 泛型
     * @return 泛型对象
     */
    public static <T> T toObjectFromSource(Object source,Class<T> target) {
        return toBean(convertObjectToJSON(source), target);
    }
}
