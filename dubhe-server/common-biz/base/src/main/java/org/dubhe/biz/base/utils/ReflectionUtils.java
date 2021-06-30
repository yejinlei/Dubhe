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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 反射工具类
 * @date 2020-05-29
 **/
public class ReflectionUtils {

    /**
     * 获取所有字段集合
     *
     * @param clazz 反射对象
     * @return List<String> 字段集合
     **/
    public static List<String> getFieldNames(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNameList = new ArrayList<>();
        for (Field field : fields) {
            fieldNameList.add(field.getName());
        }
        return fieldNameList;
    }
}
