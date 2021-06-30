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
package org.dubhe.biz.permission.annotation;


import org.dubhe.biz.base.enums.DatasetTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 数据权限方法注解
 * @date 2020-11-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermissionMethod {

    /**
     * 是否需要拦截标识 true: 不拦截 false: 拦截
     *
     * @return 拦截标识
     */
    boolean interceptFlag() default false;

    /**
     * 数据类型
     *
     * @return 数据集类型
     */
    DatasetTypeEnum dataType() default DatasetTypeEnum.PRIVATE;
}
