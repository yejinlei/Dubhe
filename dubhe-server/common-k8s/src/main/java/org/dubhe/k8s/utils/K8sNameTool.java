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

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.enums.BizEnum;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.config.K8sNameConfig;
import org.dubhe.k8s.domain.resource.BizNamespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description k8s命名相关工具类
 * @date 2020-05-13
 */
@Component
public class K8sNameTool {
    @Autowired
    private K8sNameConfig k8sNameConfig;
    /**
     * 命名分隔符
     */
    private static final char SEPARATOR = '-';
    /**
     * 文件分隔符
     */
    private static final String K8S_FILE_SEPARATOR = "/";
    /**
     * 资源名称前缀
     */
    private static final String RESOURCE_NAME = "rn";
    /**
     * 随机长度值
     */
    private static final int RANDOM_LENGTH = 4;

    @Resource
    private NamespaceApi namespaceApi;

    /**
     * 生成 ResourceName
     *
     * @param bizEnum      业务枚举
     * @param resourceInfo 资源备注信息（保证同业务下唯一并且命名规范）
     * @return String
     */
    public String generateResourceName(BizEnum bizEnum, String resourceInfo) {
        return bizEnum.getBizCode() + SEPARATOR + RESOURCE_NAME + SEPARATOR + resourceInfo;
    }

    /**
     * 生成 Notebook的Namespace
     *
     * @param userId
     * @return namespace
     */
    public String generateNamespace(long userId) {
        return this.k8sNameConfig.getNamespace() + SEPARATOR + userId;
    }

    /**
     * 从resourceName中获取资源信息
     *
     * @param bizEnum      业务枚举
     * @param resourceName
     * @return resourceInfo
     */
    public String getResourceInfoFromResourceName(BizEnum bizEnum, String resourceName) {
        if (StringUtils.isEmpty(resourceName) || !resourceName.contains(bizEnum.getBizCode() + SEPARATOR)) {
            return null;
        }
        return resourceName.replace(bizEnum.getBizCode() + SEPARATOR + RESOURCE_NAME + SEPARATOR, SymbolConstant.BLANK);
    }

    /**
     * 从namespace 获取使用者ID
     *
     * @param namespace
     * @return Long
     */
    public Long getUserIdFromNamespace(String namespace) {
        if (StringUtils.isEmpty(namespace) || !namespace.contains(this.k8sNameConfig.getNamespace() + SEPARATOR)) {
            return null;
        }
        return Long.valueOf(namespace.replace(this.k8sNameConfig.getNamespace() + SEPARATOR, ""));
    }


    /**
     * 生成业务模块相对路径
     *
     * @param bizPathEnum 业务路径枚举
     * @return String 例如： /{biz}/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/
     */
    public String getPath(BizPathEnum bizPathEnum, long userId) {
        if (bizPathEnum == null) {
            return null;
        }
        return optimizationPath(K8S_FILE_SEPARATOR
                + bizPathEnum.getBizPath()
                + K8S_FILE_SEPARATOR
                + userId
                + K8S_FILE_SEPARATOR
                + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + RandomUtil.randomString(RANDOM_LENGTH)
                + K8S_FILE_SEPARATOR);
    }

    /**
     * 生成业务模块预置路径
     *
     * @param bizPathEnum 业务路径枚举
     * @return String 例如： /{biz}/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/
     */
    public String getPrePath(BizPathEnum bizPathEnum, long userId) {
        if (bizPathEnum == null) {
            return null;
        }
        return optimizationPath(K8S_FILE_SEPARATOR
                + bizPathEnum.getBizPath()
                + K8S_FILE_SEPARATOR
                + StringConstant.COMMON
                + K8S_FILE_SEPARATOR
                + userId
                + K8S_FILE_SEPARATOR
                + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + RandomUtil.randomString(RANDOM_LENGTH)
                + K8S_FILE_SEPARATOR);
    }


    /**
     * 去除根路径
     *
     * @param path
     * @return String
     */
    public String removeRootPath(String path) {
        if (StringUtils.isBlank(path) || !path.startsWith(k8sNameConfig.getNfsRootPath())) {
            return path;
        }
        return optimizationPath(K8S_FILE_SEPARATOR + path.replace(k8sNameConfig.getNfsRootPath(), ""));
    }

    /**
     * 路径添加bucket
     *
     * @param path
     * @return String
     */
    public String appendBucket(String path) {
        return optimizationPath(K8S_FILE_SEPARATOR
                + k8sNameConfig.getFileBucket()
                + K8S_FILE_SEPARATOR
                + path);
    }

    /**
     * 路径添加时间戳随机数
     *
     * @param path
     * @return String
     */
    public String appendTimeStampAndRandomNum(String path) {
        return optimizationPath(path
                + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + RandomUtil.randomString(RANDOM_LENGTH)
                + K8S_FILE_SEPARATOR);
    }

    /**
     * 路径根据业务转换
     *
     * @param path
     * @param sourceBizPathEnum 源业务 Path
     * @param targetBizPathEnum 目标业务 Path
     * @return String
     */
    public String convertNfsPath(String path, BizPathEnum sourceBizPathEnum, BizPathEnum targetBizPathEnum) {
        if (!validateBizPath(path, sourceBizPathEnum) || targetBizPathEnum == null) {
            return path;
        }
        return optimizationPath(path.replace(K8S_FILE_SEPARATOR + sourceBizPathEnum.getBizPath() + K8S_FILE_SEPARATOR
                , K8S_FILE_SEPARATOR + targetBizPathEnum.getBizPath() + K8S_FILE_SEPARATOR));
    }

    /**
     * 获取绝对路径
     *
     * @param path
     * @return String
     */
    public String getAbsolutePath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        return optimizationPath(k8sNameConfig.getNfsRootPath()
                + K8S_FILE_SEPARATOR
                + k8sNameConfig.getFileBucket()
                + K8S_FILE_SEPARATOR
                + path);
    }

    /**
     * 验证 path 是否是所属业务路径
     *
     * @param path
     * @param bizPathEnum
     * @return boolean
     */
    public boolean validateBizPath(String path, BizPathEnum bizPathEnum) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(path)
                && bizPathEnum != null
                && path.contains(bizPathEnum.getBizPath())
                && !path.contains(K8S_FILE_SEPARATOR + k8sNameConfig.getFileBucket() + K8S_FILE_SEPARATOR)
                && !path.contains(K8S_FILE_SEPARATOR + k8sNameConfig.getNfsRootPath() + K8S_FILE_SEPARATOR);
    }

    /**
     * 路径优化
     *
     * @param path
     * @return String
     */
    private String optimizationPath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        return path.replaceAll("///*", K8S_FILE_SEPARATOR);
    }

    /**
     * 获取k8s pod标签
     *
     * @param bizEnum
     * @return String
     */
    public String getPodLabel(BizEnum bizEnum) {
        return bizEnum == null ? null : bizEnum.getBizCode();
    }

    /**
     * 获取数据集在镜像中路径
     *
     * @return String
     */
    public String getDatasetPath() {
        return k8sNameConfig.getDatasetPath();
    }


    /**
     * 自送生成K8S名称，供K8S使用
     *
     * @return String
     */
    public String getK8sName() {
        return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT)
                + RandomUtil.randomString(RANDOM_LENGTH);
    }

    /**
     * @param curUser 当前用户
     * @return namespace k8s的命名空间
     */
    public String getNamespace(UserContext curUser) {
        String namespaceStr = this.generateNamespace(curUser.getId());
        BizNamespace bizNamespace = namespaceApi.get(namespaceStr);
        if (null == bizNamespace) {
            BizNamespace namespace = namespaceApi.create(namespaceStr, null);
            if (null == namespace || !namespace.isSuccess()) {
                LogUtil.error(LogEnum.BIZ_K8S, "用户{} namespace为空", curUser.getUsername());
            }
        }
        return namespaceStr;
    }

    /**
     * @param userId 用户id
     * @return namespace k8s的命名空间
     */
    public String getNamespace(Long userId) {
        String namespaceStr = this.generateNamespace(userId);
        BizNamespace bizNamespace = namespaceApi.get(namespaceStr);
        if (null == bizNamespace) {
            BizNamespace namespace = namespaceApi.create(namespaceStr, null);
            if (null == namespace || !namespace.isSuccess()) {
                LogUtil.error(LogEnum.BIZ_K8S, "用户{} namespace为空", userId);
            }
        }
        return namespaceStr;
    }

}
