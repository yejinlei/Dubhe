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

package org.dubhe.k8s.aspect;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.k8s.annotation.K8sValidation;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.enums.ValidationTypeEnum;
import org.dubhe.k8s.utils.ValidationUtils;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @description 参数校验
 * AOP org.dubhe.k8s.api.impl下的所有方法加了@K8sValidation(ValidationTypeEnum.K8S_RESOURCE_NAME) 的参数和参数内的一级字段会被校验
 * @date 2020-06-04
 */
@Component
@Aspect
public class ValidationAspect {
    @Order(1)
    @Around("execution(* org.dubhe.k8s.api.impl.*.*(..)))")
    public Object k8sResourceNameValidation(JoinPoint point) throws Throwable {
        /**获取参数注解**/
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
        /**参数**/
        Object[] args = point.getArgs();
        if (ArrayUtil.isNotEmpty(args)) {
            for (int i = 0; i < parameterAnnotations.length; i++) {
                if (null == args[i]) {
                    continue;
                }
                /**基本类型不做校验**/
                if (args[i].getClass().isPrimitive()) {
                    continue;
                }
                if (args[i] instanceof String) {
                    /**对String类型做校验**/
                    for (Annotation annotation : parameterAnnotations[i]) {
                        if (annotation instanceof K8sValidation && ValidationTypeEnum.K8S_RESOURCE_NAME.equals(((K8sValidation) annotation).value())) {
                            ValidateResourceNameResult validateResult = validateResourceName((String) args[i]);
                            if (!validateResult.isSuccess()) {
                                return getValidationResourceNameErrorReturn(methodSignature.getReturnType(), validateResult.getField());
                            }
                        }
                    }
                } else {
                    /**对非String类型做其字段校验**/
                    ValidateResourceNameResult validateResult = validateArgResourceName(args[i], args[i].getClass());
                    if (!validateResult.isSuccess()) {
                        return getValidationResourceNameErrorReturn(methodSignature.getReturnType(), validateResult.getField());
                    }
                }
            }
        }
        return ((ProceedingJoinPoint) point).proceed();
    }

    /**
     * 校验k8s资源对象名称是否合法
     *
     * @param resourceName 资源名称
     * @return ValidateResourceNameResult 校验资源名称结果类
     */
    private ValidateResourceNameResult validateResourceName(String resourceName) {
        return new ValidateResourceNameResult(ValidationUtils.validateResourceName(resourceName), resourceName);
    }

    /**
     * 对参数内部字段做k8s资源对象名称是否合法校验
     *
     * @param arg 任意对象
     * @param argClass Class类对象
     * @return ValidateResourceNameResult 校验资源名称结果类
     */
    private ValidateResourceNameResult validateArgResourceName(Object arg, Class argClass) {
        Field[] fields = argClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            K8sValidation k8sValidation = field.getDeclaredAnnotation(K8sValidation.class);
            if (k8sValidation == null) {
                continue;
            }
            if (ValidationTypeEnum.K8S_RESOURCE_NAME.equals(k8sValidation.value()) && field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String resourceName = (String) field.get(arg);
                    if (!validateResourceName(resourceName).isSuccess()) {
                        return new ValidateResourceNameResult(false, resourceName);
                    }
                } catch (IllegalAccessException e) {
                    LogUtil.error(LogEnum.BIZ_K8S, "ValidationAspect.validateArgResourceName exception, param:[arg]={}, [argClass]={}, exception:{}", JSON.toJSONString(arg), JSON.toJSONString(argClass), e);
                }
            }
        }
        /**递归校验父类属性**/
        if (argClass.getSuperclass() != Object.class) {
            return validateArgResourceName(arg, argClass.getSuperclass());
        }
        return new ValidateResourceNameResult(true, null);
    }

    /**
     * 校验不通过获取返回值
     *
     * @param returnType Class类对象
     * @param fieldName 字段名称
     * @return Object 任意对象
     */
    private Object getValidationResourceNameErrorReturn(Class<?> returnType, String fieldName) {
        /**获取返回值类型**/
        try {
            if (PtBaseResult.class.isAssignableFrom(returnType)) {
                PtBaseResult validationReturn = (PtBaseResult) returnType.newInstance();
                return validationReturn.validationErrorRequest(fieldName);
            }
        } catch (InstantiationException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ValidationAspect.getValidationResourceNameErrorReturn exception, param:[returnType]={}, [fieldName]={}", JSON.toJSONString(returnType), fieldName, e);
        } catch (IllegalAccessException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ValidationAspect.getValidationResourceNameErrorReturn exception, param:[returnType]={}, [fieldName]={}", JSON.toJSONString(returnType), fieldName, e);
        }
        return null;
    }

    /**
     * 校验结果
     */
    @Data
    private class ValidateResourceNameResult {
        private boolean success;
        private String field;

        public ValidateResourceNameResult(boolean success, String field) {
            this.success = success;
            this.field = field;
        }
    }
}
