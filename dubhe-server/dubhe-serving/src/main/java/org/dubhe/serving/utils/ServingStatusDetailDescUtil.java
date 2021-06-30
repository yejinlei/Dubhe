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
package org.dubhe.serving.utils;

import org.apache.commons.lang.StringUtils;
import org.dubhe.biz.base.constant.SymbolConstant;

/**
 * @description serving异常详情信息工具类
 * @date 2021-06-16
 */
public class ServingStatusDetailDescUtil {

    public static final String CONTAINER_INFORMATION ="容器信息";
    public static final String BULK_SERVICE_CONTAINER_INFORMATION = "批量服务容器信息";
    public static final String DEPLOYMENT_OF_BATCH_SERVICE_SINGLE_NODE_EXCEPTION = "批量服务部署 单节点异常";
    public static final String DEPLOYMENT_OF_BATCH_SERVICE_MULTI_NODE_EXCEPTION = "批量服务部署 多节点异常";
    public static final String BULK_SERVICE_DELETE_EXCEPTION = "批量服务删除异常";
    public static final String CONTAINER_DEPLOYMENT_EXCEPTION = " 容器部署异常";
    public static final String CONTAINER_DELETION_EXCEPTION = "容器删除异常";
    public static final String CLOUD_SERVICE_UPDATE_EXCEPTION = "云端服务更新异常";


    /**
     * 创建异常详情信息的key
     * @param desc 异常点描述
     * @param name 异常对象名
     * @return 异常详情信息key
     */
    public static String getServingStatusDetailKey(String desc, String name) {
        if (StringUtils.isBlank(desc)) {
            return null;
        }
        StringBuilder descBuilder = new StringBuilder(desc);
        descBuilder.append("：").append(name);
        return descBuilder.toString();
    }

    /**
     *通过模型名称和版本号拼接成唯一名称标识
     * @param modelName 模型名称
     * @param version 版本号
     * @return 唯一名称
     * @throws NullPointerException
     */
    public static String getUniqueName(String modelName, String version) throws NullPointerException {
        StringBuilder uniqueName = new StringBuilder(modelName);
        if (StringUtils.isNotEmpty(version)) {
            uniqueName.append(SymbolConstant.HYPHEN).append(version);
        }
        return uniqueName.toString();
    }

}
