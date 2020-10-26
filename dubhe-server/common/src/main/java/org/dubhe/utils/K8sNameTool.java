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

package org.dubhe.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.dubhe.config.K8sNameConfig;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.enums.BizEnum;
import org.dubhe.enums.BizNfsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

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
     * @param bizNfsEnum 业务NFS路径枚举
     * @return String 例如： /{biz}/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/
     */
    public String getNfsPath(BizNfsEnum bizNfsEnum, long userId) {
        if (bizNfsEnum == null) {
            return null;
        }
        return optimizationPath(K8S_FILE_SEPARATOR
                + bizNfsEnum.getBizNfsPath()
                + K8S_FILE_SEPARATOR
                + userId
                + K8S_FILE_SEPARATOR
                + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + RandomUtil.randomString(RANDOM_LENGTH)
                + K8S_FILE_SEPARATOR);
    }

    /**
     * 去除NFS根路径
     *
     * @param nfsPath
     * @return String
     */
    public String removeNfsRootPath(String nfsPath) {
        if (StringUtils.isBlank(nfsPath) || !nfsPath.startsWith(k8sNameConfig.getNfsRootPath())) {
            return nfsPath;
        }
        return optimizationPath(K8S_FILE_SEPARATOR + nfsPath.replace(k8sNameConfig.getNfsRootPath(), ""));
    }

    /**
     * 路径添加bucket
     *
     * @param nfsPath
     * @return String
     */
    public String appendBucket(String nfsPath) {
        return optimizationPath(K8S_FILE_SEPARATOR
                + k8sNameConfig.getFileBucket()
                + K8S_FILE_SEPARATOR
                + nfsPath);
    }

    /**
     * 路径添加时间戳随机数
     *
     * @param nfsPath
     * @return String
     */
    public String appendTimeStampAndRandomNum(String nfsPath) {
        return optimizationPath(nfsPath
                + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + RandomUtil.randomString(RANDOM_LENGTH)
                + K8S_FILE_SEPARATOR);
    }

    /**
     * nfs路径根据业务转换
     *
     * @param nfsPath
     * @param sourceBizNfsEnum 源业务 NFS Path
     * @param targetBizNfsEnum 目标业务 NFS Path
     * @return String
     */
    public String convertNfsPath(String nfsPath, BizNfsEnum sourceBizNfsEnum, BizNfsEnum targetBizNfsEnum) {
        if (!validateBizNfsPath(nfsPath, sourceBizNfsEnum) || targetBizNfsEnum == null) {
            return nfsPath;
        }
        return optimizationPath(nfsPath.replace(K8S_FILE_SEPARATOR + sourceBizNfsEnum.getBizNfsPath() + K8S_FILE_SEPARATOR
                , K8S_FILE_SEPARATOR + targetBizNfsEnum.getBizNfsPath() + K8S_FILE_SEPARATOR));
    }

    /**
     * 获取NFS绝对路径
     *
     * @param nfsPath
     * @return String
     */
    public String getAbsoluteNfsPath(String nfsPath) {
        if (StringUtils.isBlank(nfsPath)) {
            return nfsPath;
        }
        return optimizationPath(k8sNameConfig.getNfsRootPath()
                + K8S_FILE_SEPARATOR
                + k8sNameConfig.getFileBucket()
                + K8S_FILE_SEPARATOR
                + nfsPath);
    }

    /**
     * 验证 nfsPath 是否是所属业务路径
     *
     * @param nfsPath
     * @param bizNfsEnum
     * @return boolean
     */
    public boolean validateBizNfsPath(String nfsPath, BizNfsEnum bizNfsEnum) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(nfsPath)
                && bizNfsEnum != null
                && nfsPath.contains(bizNfsEnum.getBizNfsPath())
                && !nfsPath.contains(K8S_FILE_SEPARATOR + k8sNameConfig.getFileBucket() + K8S_FILE_SEPARATOR)
                && !nfsPath.contains(K8S_FILE_SEPARATOR + k8sNameConfig.getNfsRootPath() + K8S_FILE_SEPARATOR);
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

}
