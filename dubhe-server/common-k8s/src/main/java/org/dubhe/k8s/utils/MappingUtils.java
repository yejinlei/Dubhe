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

package org.dubhe.k8s.utils;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.biz.log.utils.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description 将 fabric8 pojo 转换为 resource下Biz类
 *              规则: 根据 ":" 分割 @K8sField,反射注入属性
 *              示例:@K8sField("spec:template:spec:containers")
 *                  private List<BizContainer> containers;     对应 Deployment.spec.template.containers
 * @date 2020-04-17
 */
public class MappingUtils {

    /**
     * fabric pojo 转换为 biz pojo 注意output类和input类中 不要使用基本类型，应使用封装类型
     **/
    public static <T, V> V mappingTo(T input, Class<V> outputClass) {
        //不判null的话，若T的属性有默认值，则会输出包含对应默认值的V实例
        if (null == input || outputClass == null) {
            return null;
        }
        //相同基本类的封装类直接输出 比如 String
        if (outputClass.isInstance(input)) {
            return outputClass.cast(input);
        }

        Field[] fields = outputClass.getDeclaredFields();
        V output = null;
        try {
            output = outputClass.newInstance();
            if (fields == null || fields.length < 1) {
                return output;
            }

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                K8sField k8sField = field.getDeclaredAnnotation(K8sField.class);
                if (k8sField == null) {
                    continue;
                }
                String expression = k8sField.value();
                String[] splitExpression = expression.split(SymbolConstant.COLON);
                Object currentArg = input;

                int j = 0;
                while (j < splitExpression.length) {
                    if (currentArg == null) {
                        break;
                    }
                    String funcName = SymbolConstant.GET + StrUtil.upperFirst(splitExpression[j]);
                    Method getMethod = currentArg.getClass().getDeclaredMethod(funcName, null);
                    currentArg = getMethod.invoke(currentArg, null);
                    j++;
                }

                if (currentArg == null) {
                    continue;
                }

                //List类型处理
                if (List.class.isAssignableFrom(field.getType())
                        && field.getGenericType() instanceof ParameterizedType
                        && List.class.isAssignableFrom(currentArg.getClass())) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    //泛型里的类型
                    Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
                    List outputFieldList = new ArrayList();
                    List<?> inputArgList = ((List) currentArg);
                    for (Object inputArg : inputArgList) {
                        Object outputElm = mappingTo(inputArg, actualTypeArgument);
                        outputFieldList.add(outputElm);
                    }
                    currentArg = outputFieldList;
                } else if (Map.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType
                        && Map.class.isAssignableFrom(currentArg.getClass())) {
                    /**处理map value类型不同**/
                    Map map = (Map) currentArg;
                    map.forEach((key, value) -> {
                        map.put(mappingTo(key, getMapValueClass(field, 0)), mappingTo(value, getMapValueClass(field, 1)));
                    });
                } else if (!field.getType().isInstance(currentArg) && field.getType().getDeclaredFields().length > 0) {
                    //递归处理属性
                    currentArg = mappingTo(currentArg, field.getType());
                }

                //如果类型相同或是field的子类，赋值
                String setFuncName = SymbolConstant.SET + StrUtil.upperFirst(field.getName());
                Method setMethod = outputClass.getDeclaredMethod(setFuncName, field.getType());
                setMethod.invoke(output, currentArg);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MappingUtils.mappingTo error, message{} ",e.getMessage(), e);
            throw new RuntimeException("fabric to biz failed");
        }
        return output;
    }

    /**
     * 获取map 的key或value 的类型
     *
     * @param field 反射字段
     * @param fieldIndex 0获取key的Class 1获取Value的Class
     * @return Class Class类对象
     */
    private static Class getMapValueClass(Field field, int fieldIndex) {
        try {
            if (Map.class.isAssignableFrom(field.getType())) {
                Type mapMainType = field.getGenericType();
                if (mapMainType instanceof ParameterizedType) {
                    // 执行强制类型转换
                    ParameterizedType parameterizedType = (ParameterizedType) mapMainType;
                    // 获取泛型类型的泛型参数
                    Type[] types = parameterizedType.getActualTypeArguments();
                    return Class.forName(types[fieldIndex].getTypeName());
                } else {
                    LogUtil.error(LogEnum.BIZ_K8S, "Error getting generic type {}",mapMainType.getTypeName());
                }
            }
        } catch (ClassNotFoundException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MappingUtils.getMapValueClass error, message {}", e.toString(),e);
        }
        return null;
    }
}
