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
package org.dubhe.biz.permission.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.dubhe.biz.base.context.DataContext;
import org.dubhe.biz.permission.annotation.DataPermissionMethod;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.CommonPermissionDataDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @description 数据权限切面
 * @date 2020-11-26
 */
@Aspect
@Component
public class PermissionAspect {

    @Resource
    private UserContextService userContextService;

    /**
     * 公共数据的有用户ID
     */
    public static final Long PUBLIC_DATA_USER_ID = 0L;

    /**
     * 基于注解的切面方法
     */
    @Pointcut("@annotation(org.dubhe.biz.permission.annotation.DataPermissionMethod)")
    private void cutMethod() {

    }

    /**
     *环绕通知
     * @param joinPoint 切入参数对象
     * @return 返回方法结果集
     * @throws Throwable
     */
    @Around("cutMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法传入参数
        Object[] params = joinPoint.getArgs();
        DataPermissionMethod dataPermissionMethod = getDeclaredAnnotation(joinPoint);
        UserContext curUser = userContextService.getCurUser();

        if (!Objects.isNull(curUser) && !Objects.isNull(dataPermissionMethod)) {
            Set<Long> ids = new HashSet<>();
            ids.add(curUser.getId());
            CommonPermissionDataDTO commonPermissionDataDTO = CommonPermissionDataDTO.builder().type(dataPermissionMethod.interceptFlag()).resourceUserIds(ids).build();
            if (DatasetTypeEnum.PUBLIC.equals(dataPermissionMethod.dataType())) {
                ids.add(PUBLIC_DATA_USER_ID);
                commonPermissionDataDTO.setResourceUserIds(ids);
            }
            DataContext.set(commonPermissionDataDTO);
        }
        // 执行源方法
        try {
           return joinPoint.proceed(params);
        }  finally {
            if(!Objects.isNull(DataContext.get())){
                DataContext.remove();
            }
        }
    }

    /**
     * 获取方法中声明的注解
     *
     * @param joinPoint 切入参数对象
     * @return DataPermissionMethod 方法注解类型
     */
    public DataPermissionMethod getDeclaredAnnotation(ProceedingJoinPoint joinPoint){
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 反射获取目标类
        Class<?> targetClass = joinPoint.getTarget().getClass();
        // 拿到方法对应的参数类型
        Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        // 根据类、方法、参数类型（重载）获取到方法的具体信息
        Method objMethod = null;
        try {
            objMethod = targetClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            LogUtil.error(LogEnum.BIZ_DATASET,"获取注解方法参数异常 error:{}",e);
        }
        // 拿到方法定义的注解信息
        DataPermissionMethod annotation = objMethod.getDeclaredAnnotation(DataPermissionMethod.class);
        // 返回
        return annotation;
    }
}
