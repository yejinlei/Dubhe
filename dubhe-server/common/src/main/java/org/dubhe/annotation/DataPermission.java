/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤Mapper拦截
 *
 * @date 2020-06-22
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 不需要数据权限的方法名
     */
    String[] ignores() default {};

    /**
     * 只在方法的注解上使用，代表方法的数据权限类型，如果不加注解，只会识别带"select"方法名的方法
     *
     * @return
     */
    String[] permission() default {};

}
