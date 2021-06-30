/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.biz.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description  构建Wrapper的注解
 * @date 2020-03-26
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {


    String propName() default "";

    Type type() default Type.EQ;


    String blurry() default "";

    enum Type {
        // 相等
        EQ
        // 不等于
        , NE
        // 大于
        , GT
        // 大于等于
        , GE
        // 小于
        , LT
        // 小于等于
        , LE,
        BETWEEN,
        NOT_BETWEEN,
        LIKE,
        NOT_LIKE,
        LIkE_LEFT,
        LIKE_RIGHT,
        IS_NULL,
        IS_NOT_NULL,
        IN,
        NOT_IN,
        INSQL,
        NOT_INSQL,
        ORDER_BY
    }

}

