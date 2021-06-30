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
package org.dubhe.biz.base.annotation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.util.Arrays;

/**
 * @description 自定义状态校验注解(传入值是否在指定状态范围内)
 * @date 2020-09-18
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FlagValidator.Validator.class)
@Documented
public @interface FlagValidator {

    String[] value() default {};

    String message() default "flag value is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @description 校验传入值是否在默认值范围校验逻辑
     * @date 2020-09-18
     */
    class Validator implements ConstraintValidator<FlagValidator, Integer> {

        private String[] values;

        @Override
        public void initialize(FlagValidator flagValidator) {
            this.values = flagValidator.value();
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                //当状态为空时，使用默认值
                return false;
            }
            return Arrays.stream(values).anyMatch(Integer.toString(value)::equals);
        }
    }
}
