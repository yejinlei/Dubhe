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
package org.dubhe.tadl.utils;

import com.google.common.base.CaseFormat;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * @description key名称转换工具
 * @date 2020-03-22
 */
@Component
public class KeyNameConvertUtil {

    /**
     * 替换map中所有的key都为驼峰风格
     *
     * @param map 要转换的map
     * @return 转换后的map
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> convertToCamelStyle(HashMap<String, Object> map) {
        HashMap<String, Object> hashMap = new HashMap<>();
        map.forEach((k, v) -> {
            if (v instanceof Map) {
                hashMap.remove(k);
                hashMap.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), convertToCamelStyle((HashMap<String, Object>) v));
            } else {
                hashMap.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), v);
            }
        });
        return hashMap;
    }

    /**
     * 替换map中所有的key都为下划线风格
     *
     * @param map 要转换的map
     * @return 转换后的map
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> convertToUnderlineStyle(HashMap<String, Object> map) {
        HashMap<String, Object> hashMap = new HashMap<>();
        map.forEach((k, v) -> {
            if (v instanceof Map) {
                hashMap.remove(k);
                hashMap.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, k), convertToUnderlineStyle((HashMap<String, Object>) v));
            } else {
                hashMap.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, k), v);
            }
        });
        return hashMap;
    }

}
