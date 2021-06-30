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

package org.dubhe.biz.file.api.impl;

import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.dto.FilePageDTO;
import org.dubhe.biz.file.utils.NfsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.util.List;

/**
 * @description Nfs文件存储接口
 * @date 2021-04-20
 */
@Deprecated
@Component(value = "nfsFileStoreApiImpl")
public class NfsFileStoreApiImpl implements FileStoreApi {

    @Autowired
    private NfsUtil nfsUtil;

    @Value("${storage.file-store-root-path}")
    private String rootDir;

    @Value("/${minio.bucketName}/")
    private String bucket;

    @Override
    public String getRootDir(){
        return rootDir;
    }

    @Override
    public String getBucket(){
        return bucket;
    }

    @Override
    public boolean fileOrDirIsExist(String path) {
        return nfsUtil.fileOrDirIsEmpty(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return false;
    }

    @Override
    public List<String> filterFileSuffix(String path, String fileSuffix) {
        return null;
    }

    @Override
    public boolean createDir(String dir) {
        return nfsUtil.createDir(dir);
    }

    @Override
    public boolean createDirs(String... paths) {
        return nfsUtil.createDirs(true,paths);
    }

    @Override
    public boolean createFile(String dir, String fileName) {
        return nfsUtil.createFile(dir,fileName);
    }

    @Override
    public boolean createOrAppendFile(String filePath, String content, boolean append) {
        return false;
    }

    @Override
    public boolean deleteDirOrFile(String dirOrFile) {
        return nfsUtil.deleteDirOrFile(dirOrFile);
    }

    @Override
    public boolean copyFile(String sourceFile, String targetPath) {
        return nfsUtil.copyFile(sourceFile,targetPath);
    }

    @Override
    public boolean copyFile(String sourceFile, String targetPath, Integer type) {
        return false;
    }

    @Override
    public boolean copyFileAndRename(String sourceFile, String targetFile) {
        return false;
    }

    @Override
    public boolean copyPath(String sourcePath, String targetPath) {
        return nfsUtil.copyNfsPath(sourcePath, targetPath);
    }

    @Override
    public boolean copyDir(String sourcePath, String targetPath) {
        return false;
    }

    @Override
    public boolean unzip(String sourceFile, String targetPath) {
        return nfsUtil.unzip(sourceFile,targetPath);
    }

    @Override
    public boolean unzip(String sourceFile) {
        return nfsUtil.unZip(sourceFile);
    }

    @Override
    public boolean zipDirOrFile(String dirOrFile, String zipName) {
        return nfsUtil.zipDirOrFile(dirOrFile,zipName);
    }

    @Override
    public BufferedInputStream getInputStream(String path) {
        return nfsUtil.getInputStream(path);
    }

    @Override
    public void download(String path, HttpServletResponse response) {
        return;
    }

    @Override
    public void filterFilePageWithPath(FilePageDTO filePageDTO) {

    }
}
