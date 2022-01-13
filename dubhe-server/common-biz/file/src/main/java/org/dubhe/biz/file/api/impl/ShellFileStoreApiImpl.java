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
import org.dubhe.biz.file.enums.CopyTypeEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * @description 通过shell指令存储文件接口实现类
 * @date 2021-05-06
 */
@Deprecated
@Component(value = "shellFileStoreApiImpl")
public class ShellFileStoreApiImpl implements FileStoreApi {

    /**
     * 服务暴露的IP地址
     */
    @Value("${storage.file-store}")
    private String ip;

    /**
     * 文件存储服务器用户名
     */
    @Value("${data.server.userName}")
    private String userName;

    /**
     * 拷贝源路径下文件或文件夹命令
     */
    public static final String COPY_COMMAND = "ssh %s@%s \"mkdir -p %s && cp -rf %s %s && echo success\"";
    /**
     * 拷贝多文件夹下文件命令
     */
    public static final String COPY_DIR_COMMAND = "ssh %s@%s \"mkdir -p %s && cp -rf %s* %s && echo success\"";

    /**
     * 删除服务器无效文件(大文件)
     * 示例：rsync --delete-before -d /空目录 /需要回收的源目录
     */
    public static final String DEL_COMMAND = "mkdir -p %s; rsync --delete-before -d %s %s; rmdir %s %s";

    /**
     * 拷贝文件并重命名
     */
    public static final String COPY_RENAME_COMMAND = "ssh %s@%s \"cp -rf %s %s && echo success\"";

    /**
     * 文件复制
     * rsync -avP --exclude={'dir'} sourcePath targetPath 将原路径复制到目标路径下，过滤dir目录
     * 示例：rsync -avP --exclude={'V0001'} /root/test/ /root/test2/
     */
    public static final String COPY_AVP_COMMAND = "ssh %s@%s rsync -avP --exclude={'%s'} %s %s";

    /**
     * 修改文件夹名称
     * mv  sourcePathName targetPathName 将原目录名称修改为目标目录名称
     * 示例：mv /root/test2/versionFile/V0002 /root/test2/versionFile/V0001
     */
    public static final String UPDATE_NAME_COMMAND = "ssh %s@%s mv %s %s";

    @Value("${storage.file-store-root-path}")
    private String rootDir;

    @Value("/${minio.bucketName}/")
    private String bucket;

    @Override
    public String getRootDir() {
        return rootDir;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public boolean fileOrDirIsExist(String path) {
        return false;
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
        return false;
    }

    @Override
    public boolean createDirs(String... paths) {
        return false;
    }

    @Override
    public boolean createFile(String dir, String fileName) {
        return false;
    }

    @Override
    public boolean createOrAppendFile(String filePath, String content, boolean append) {
        return false;
    }

    @Override
    public boolean deleteDirOrFile(String dirOrFile) {
        return false;
    }

    @Override
    public boolean copyFile(String sourceFile, String targetPath) {
        return false;
    }

    /**
     * 使用shell拷贝文件或路径
     *
     * @param sourcePath 需要复制的文件或路径  例如：/abc/def/cc.txt or /abc/def*
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @param type CopyTypeEnum的
     * @return boolean
     */
    @Override
    public boolean copyFile(String sourcePath, String targetPath, Integer type) {
        //绝对路径
        String sourceAbsolutePath = formatPath(getRootDir() + sourcePath);
        String targetPathAbsolutePath = formatPath(getRootDir() + targetPath);
        String[] command;
        if (CopyTypeEnum.COPY_FILE.getKey().equals(type)) {
            command = new String[]{"/bin/sh", "-c", String.format(COPY_COMMAND, userName, ip, targetPathAbsolutePath, sourceAbsolutePath, targetPathAbsolutePath)};
        } else {
            command = new String[]{"/bin/sh", "-c", String.format(COPY_DIR_COMMAND, userName, ip, targetPathAbsolutePath, sourceAbsolutePath, targetPathAbsolutePath)};
        }
        boolean flag = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            if (isCopySuccess(process)) {
                flag = true;
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.FILE_UTIL, "copy file failed, filePath:{}, targetPath:{}", sourcePath, targetPath, e);
        }
        return flag;
    }

    /**
     * 判断拷贝结果
     *
     * @param process
     * @return
     */
    public boolean isCopySuccess(Process process) {
        try (InputStream stream = process.getInputStream();
             InputStreamReader iReader = new InputStreamReader(stream);
             BufferedReader bReader = new BufferedReader(iReader)) {
            String line;
            while (Objects.nonNull(line = bReader.readLine())) {
                boolean temp = line.contains("success");
                if (temp) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "Read stream failed : {}", e);
        }
        return false;
    }

    /**
     * 拷贝文件并重命名
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/aa.py
     * @param targetFile 需要放置的目标目录 例如：/abc/dd/bb.py
     * @return
     */
    @Override
    public boolean copyFileAndRename(String sourceFile, String targetFile) {
        //绝对路径
        String sourceAbsolutePath = formatPath(rootDir + sourceFile);
        String targetPathAbsolutePath = formatPath(rootDir + targetFile);
        String[] command = new String[]{"/bin/sh", "-c", String.format(COPY_RENAME_COMMAND, userName, ip, sourceAbsolutePath, targetPathAbsolutePath)};
        boolean flag = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            if (isCopySuccess(process)) {
                flag = true;
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.FILE_UTIL, "copy file failed, filePath:{}, targetPath:{}, because:{}", sourceFile, targetFile, e);
        }
        return flag;
    }

    @Override
    public boolean copyPath(String sourcePath, String targetPath) {
        return false;
    }

    @Override
    public boolean copyDir(String sourcePath, String targetPath) {
        return false;
    }

    @Override
    public boolean unzip(String sourceFile, String targetPath) {
        return false;
    }

    @Override
    public boolean unzip(String sourceFile) {
        return false;
    }

    @Override
    public boolean zipDirOrFile(String dirOrFile, String zipPath) {
        return false;
    }

    @Override
    public BufferedInputStream getInputStream(String path) {
        return null;
    }

    @Override
    public void download(String path, HttpServletResponse response) {

    }

    @Override
    public void filterFilePageWithPath(FilePageDTO filePageDTO) {

    }
}