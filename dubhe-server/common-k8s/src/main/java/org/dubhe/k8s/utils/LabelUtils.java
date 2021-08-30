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

import cn.hutool.core.util.ArrayUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.k8s.constant.K8sLabelConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * @description label 工具类
 * @date 2020-06-17
 */
public class LabelUtils {
    /**
     * 生成基础标签集，包含资源名、创建者、环境 标签
     *
     * @param resourceName 命名空间
     * @param labels 可变参数标签Map
     * @return
     */
    public static Map<String, String> getBaseLabels(String resourceName, Map<String, String>... labels) {
        Map<String, String> baseLabels = new HashMap<>(MagicNumConstant.SIXTEEN);
        baseLabels.put(K8sLabelConstants.BASE_TAG_SOURCE, resourceName);
        baseLabels.put(K8sLabelConstants.BASE_TAG_CREATE_BY, K8sLabelConstants.BASE_TAG_CREATE_BY_VALUE);
        baseLabels.put(K8sLabelConstants.PLATFORM_RUNTIME_ENV, SpringContextHolder.getActiveProfile());
        if (ArrayUtil.isNotEmpty(labels)) {
            for (Map<String, String> obj : labels) {
                if (obj != null) {
                    baseLabels.putAll(obj);
                }
            }
        }
        return baseLabels;
    }

    /**
     * 生成基础标签集，包含资源名、创建者、环境、业务 标签
     *
     * @param resourceName 命名空间
     * @param business 业务标签
     * @param labels 可变参数标签Map
     * @return
     */
    public static Map<String, String> getBaseLabels(String resourceName, String business, Map<String, String>... labels) {
        Map<String, String> labelMap = getBaseLabels(resourceName, labels);
        if (null != business) {
            labelMap.put(K8sLabelConstants.BASE_TAG_BUSINESS, business);
        }
        return labelMap;
    }

    /**
     * 生成子资源标签集，包含资源名、创建者、环境、父资源、父类型 标签
     *
     * @param resourceName 命名空间
     * @param pName 父资源名称
     * @param pKind 父类型名称
     * @param labels 可变参数标签Map
     * @return
     */
    public static Map<String, String> getChildLabels(String resourceName, String pName, String pKind, Map<String, String>... labels) {
        Map<String, String> baseLabels = getBaseLabels(resourceName, labels);
        baseLabels.put(K8sLabelConstants.BASE_TAG_P_NAME, pName);
        baseLabels.put(K8sLabelConstants.BASE_TAG_P_KIND, pKind);
        return baseLabels;
    }

    /**
     * 生成子资源标签集，包含资源名、创建者、环境、父资源、父类型、业务标签 标签
     *
     * @param resourceName 命名空间
     * @param pName 父资源名称
     * @param pKind 父类型名称
     * @param business 业务标签
     * @param labels 可变参数标签Map
     * @return
     */
    public static Map<String, String> getChildLabels(String resourceName, String pName, String pKind, String business, String taskIdentify, Map<String, String>... labels) {
        Map<String, String> labelMap = getChildLabels(resourceName, pName, pKind, labels);
        if (null != business) {
            labelMap.put(K8sLabelConstants.BASE_TAG_BUSINESS, business);
        }
        if (null != taskIdentify){
            labelMap.put(K8sLabelConstants.BASE_TAG_TASK_IDENTIFY, taskIdentify);
        }
        return labelMap;
    }

    /**
     * 添加环境标签用以分流
     *
     * @param resourceName 资源名称
     * @return Map<String, String> map
     */
    public static Map<String, String> withEnvResourceName(String resourceName) {
        Map<String, String> labels = withEnvResourceName();
        labels.put(K8sLabelConstants.BASE_TAG_SOURCE, resourceName);
        return labels;
    }

    /**
     * 添加环境标签用以分流
     *
     * @return Map<String, String> map
     */
    public static Map<String, String> withEnvResourceName() {
        Map<String, String> labels = new HashMap<>(0);
        labels.put(K8sLabelConstants.PLATFORM_RUNTIME_ENV, SpringContextHolder.getActiveProfile());
        return labels;
    }

    /**
     * 附带环境标签
     *
     * @param key 标签健
     * @param value 标签值
     * @return Map<String, String> map
     */
    public static Map<String, String> withEnvLabel(String key, String value) {
        Map<String, String> labels = withEnvResourceName();
        labels.put(key, value);
        return labels;
    }
}
