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

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.exception.BusinessException;

/**
 * 调用结果处理工具类
 *
 */
public class ResultUtil {
    /**
     * 判断调用结果非空
     *
     * @param object
     * @param errorMessageTemplate
     * @param params
     */
    public static void notNull(Object object, String errorMessageTemplate, Object... params) {
        if (object == null) {
            throw new BusinessException(StrUtil.format(errorMessageTemplate, params));
        }
    }

    /**
     * 判断调用结果相等
     *
     * @param object1
     * @param object2
     * @param errorMessageTemplate
     * @param params
     */
    public static void isEquals(Object object1, Object object2, String errorMessageTemplate, Object... params) {
        if(object1 == null) {
            if (object2 == null) {
                return;
            }
            throw new BusinessException(String.format(errorMessageTemplate, params));
        }
        if (!object1.equals(object2)) {
            throw new BusinessException(String.format(errorMessageTemplate, params));
        }
    }
}