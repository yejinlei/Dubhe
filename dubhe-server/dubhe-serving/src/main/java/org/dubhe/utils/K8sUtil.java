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

package org.dubhe.utils;

import org.dubhe.domain.dto.UserDTO;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.enums.ServingErrorEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description k8s相关工具类
 * @date 2020-09-14
 */
@Component
public class K8sUtil {

    @Resource
    private K8sNameTool k8sNameTool;
    @Resource
    private NamespaceApi namespaceApi;

    /**
     * @param currentUser 当前用户
     * @return namespace k8s的命名空间
     */
    public String getNamespace(UserDTO currentUser) {
        String namespaceStr = k8sNameTool.generateNamespace(currentUser.getId());
        BizNamespace bizNamespace = namespaceApi.get(namespaceStr);
        if (null == bizNamespace) {
            BizNamespace namespace = namespaceApi.create(namespaceStr, null);
            if (null == namespace || !namespace.isSuccess()) {
                LogUtil.error(LogEnum.SERVING, "用户{}启动k8s云端serving失败，namespace为空", currentUser.getUsername());
                throw new BusinessException(ServingErrorEnum.INTERNAL_SERVER_ERROR);
            }
        }
        return namespaceStr;
    }

    /**
     * 获取nfs绝对路径
     *
     * @param path 相对路径
     * @return String 绝对路径
     */
    public String getAbsoluteNfsPath(String path) {
        return k8sNameTool.getAbsoluteNfsPath(path);
    }

    /**
     * 生成在线服务ResourceName
     *
     * @param resourceInfo 资源备注信息（保证同业务下唯一并且命名规范）
     * @return String ResourceName
     */
    public String getResourceName(String resourceInfo) {
        return k8sNameTool.generateResourceName(BizEnum.SERVING, resourceInfo);
    }

    /**
     * 生成批量服务ResourceName
     *
     * @param resourceInfo 资源备注信息（保证同业务下唯一并且命名规范）
     * @return String ResourceName
     */
    public String getBatchResourceName(String resourceInfo) {
        return k8sNameTool.generateResourceName(BizEnum.BATCH_SERVING, resourceInfo);
    }

    /**
     * 生成ResourceName
     *
     * @param resourceInfo 资源备注信息（保证同业务下唯一并且命名规范）
     * @return String ResourceName
     */
    public String getResourceName(Number resourceInfo) {
        return k8sNameTool.generateResourceName(BizEnum.SERVING, resourceInfo.toString());
    }
}
