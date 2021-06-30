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

package org.dubhe.model.service.impl;

import cn.hutool.core.util.StrUtil;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.utils.PtModelUtil;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.enums.BizPathEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.utils.K8sNameTool;
import org.dubhe.model.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * @description 文件服务实现
 * @date 2021-01-20
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private K8sNameTool k8sNameTool;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    /**
     * 将临时区文件加载到相应的服务路径
     *
     * @param tempPath 临时文件目录
     * @param user     当前用户
     * @return 拷贝后的路径
     */
    @Override
    public String transfer(@NotNull String tempPath, @NotNull UserContext user) {
        //目标路径
        String sourcePath = fileStoreApi.getBucket() + tempPath;
        String destPath = k8sNameTool.getPath(BizPathEnum.MODEL, user.getId());
        String targetPath = fileStoreApi.getBucket() + destPath;

        LogUtil.info(LogEnum.BIZ_MODEL, "临时文件路径:{},目标路径:{}", sourcePath, targetPath);

        //校验path是否带有压缩文件，如有，则解压至当前文件夹并删除压缩文件
        if (tempPath.endsWith(PtModelUtil.ZIP)) {
            boolean unzip = fileStoreApi.unzip(sourcePath, targetPath);
            if (!unzip) {
                LogUtil.error(LogEnum.BIZ_MODEL, "用户{},解压模型文件失败", user.getUsername());
                throw new BusinessException("文件解压失败");
            }
        } else {
            if (fileStoreApi.isDirectory(fileStoreApi.getRootDir() + sourcePath)) {
                boolean nfsCopy = fileStoreApi.copyDir(fileStoreApi.getRootDir() + sourcePath, fileStoreApi.getRootDir() + targetPath);
                if (!nfsCopy) {
                    LogUtil.info(LogEnum.BIZ_MODEL, "用户{},文件夹拷贝失败", user.getUsername());
                    throw new BusinessException("文件夹拷贝失败");
                }
            } else {
                boolean nfsCopy = fileStoreApi.copyFile(fileStoreApi.getRootDir() + sourcePath, fileStoreApi.getRootDir() + targetPath);
                if (!nfsCopy) {
                    LogUtil.info(LogEnum.BIZ_MODEL, "用户{},文件拷贝失败", user.getUsername());
                    throw new BusinessException("文件拷贝失败");
                }
            }

        }

        return destPath;
    }

    /**
     * 文件拷贝
     *
     * @param tempPath        临时文件路径
     * @param user            当前用户
     * @param successCallback 拷贝成功回调
     * @param failCallback    拷贝失败回调
     */
    @Override
    public void copyFileAsync(String tempPath, UserContext user, Consumer<String> successCallback, Consumer<Exception> failCallback) {
        try {
            String targetPath = transfer(tempPath, user);
            LogUtil.info(LogEnum.BIZ_MODEL, "文件异步拷贝成功");
            if (successCallback != null) {
                successCallback.accept(targetPath);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_MODEL, "文件异步拷贝失败,Exception:{}", e);
            if (failCallback != null) {
                failCallback.accept(e);
            }
        }
    }

    /**
     * 验证源文件路径是否存在，若不存在直接跑出异常
     *
     * @param sourcePath 源文件路径
     */
    @Override
    public void validatePath(String sourcePath) {
        String absolutePath = getAbsolutePath(sourcePath);
        if (!fileStoreApi.fileOrDirIsExist(absolutePath)) {
            LogUtil.error(LogEnum.BIZ_MODEL, "Source path {} does not exist", sourcePath);
            throw new BusinessException("源文件或路径不存在");
        }
    }

    /**
     * 获取绝对路径
     *
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    @Override
    public String getAbsolutePath(String relativePath) {
        String absolutePath = fileStoreApi.getRootDir() + fileStoreApi.getBucket() + relativePath;
        return absolutePath.replaceAll("//", StrUtil.SLASH);
    }

    /**
     *  我的模型转预置模型 模型校验与拷贝
     *  @param sourcePath 源路径
     * @return String 目标路径
     */
    @Override
    public String convertPreset(String sourcePath, UserContext user) {
        //校验我的模型文件
        String path = fileStoreApi.getBucket() + sourcePath;
        if (!fileStoreApi.fileOrDirIsExist(fileStoreApi.getRootDir() + path)) {
            throw new BusinessException("我的模型文件或路径不存在");
        }
        //拷贝我的模型为预置模型
        //目标路径
        String destPath = k8sNameTool.getPrePath(BizPathEnum.MODEL, user.getId());
        String targetPath = fileStoreApi.getBucket() + destPath;
        boolean nfsCopy = fileStoreApi.copyPath(fileStoreApi.getRootDir() + path, fileStoreApi.getRootDir() + targetPath);
        if (!nfsCopy) {
            throw new BusinessException("文件拷贝失败");
        }
        return destPath;
    }

}
