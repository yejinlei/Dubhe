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

import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.biz.base.utils.StringUtils;

/**
 * @description 校验工具类
 * @date 2020-06-05
 */
public class ValidationUtils {
    /**
     * 校验resourceName是否符合k8s命名规范
     *
     * @param resourceName 资源名称
     * @return boolean true 成功　false 不成功
     */
    public static boolean validateResourceName(String resourceName) {
        return !(StringUtils.isEmpty(resourceName) || resourceName.length() > K8sParamConstants.RESOURCE_NAME_LENGTH || !resourceName.matches(K8sParamConstants.K8S_RESOURCE_NAME_REGEX));
    }
}
