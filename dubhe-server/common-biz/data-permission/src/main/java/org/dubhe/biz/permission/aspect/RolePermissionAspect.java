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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.dubhe.biz.base.enums.BaseErrorCodeEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.permission.base.BaseService;
import org.springframework.stereotype.Component;

/**
 * @description 角色权限切面(验证是否具有管理员权限)
 * @date 2020-11-26
 */
@Aspect
@Component
public class RolePermissionAspect {


    /**
     * 基于注解的切面方法
     */
    @Pointcut("@annotation(org.dubhe.biz.permission.annotation.RolePermission)")
    public void cutMethod() {

    }
    /**
     * 前置通知 验证是否具有管理员权限
     *
     * @param point 切入参数对象
     * @return 返回方法结果集
     */
    @Before(value = "cutMethod()")
    public void before(JoinPoint point) {
        if (!BaseService.isAdmin()) {
            throw new BusinessException(BaseErrorCodeEnum.DATASET_ADMIN_PERMISSION_ERROR);
        }
    }

}
