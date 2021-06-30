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
package org.dubhe.biz.file.utils;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.file.enums.CopyTypeEnum;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;

/**
 * @description 本地文件操作工具类
 * @date 2020-08-19
 */
@Component
@Getter
public class LocalFileUtil {

    @Autowired
    private NfsConfig nfsConfig;

    private static final String FILE_SEPARATOR = File.separator;

    private static final String ZIP = ".zip";

    private static final String CHARACTER_GBK = "GBK";

    private static final String OS_NAME = "os.name";

    private static final String WINDOWS = "Windows";

    /**
     * nfs服务暴露的IP地址
     */
    @Value("${storage.file-store}")
    private String nfsIp;

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
     * 拷贝文件并重命名
     */
    public static final String COPY_RENAME_COMMAND = "ssh %s@%s \"cp -rf %s %s && echo success\"";

    @Value("${storage.file-store-root-path}")
    private String nfsRootPath;

    @Value("${storage.file-store-root-windows-path}")
    private String nfsRootWindowsPath;

    /**
     * windows 与 linux 的路径兼容
     *
     * @param path linux下的路径
     * @return path 兼容windows后的路径
     */
    private String compatiblePath(String path) {
        if (path == null) {
            return null;
        }
        if (System.getProperties().getProperty(OS_NAME).contains(WINDOWS)) {
            path = path.replace(nfsRootPath, StrUtil.SLASH);
            path = path.replace(StrUtil.SLASH, FILE_SEPARATOR);
            path = nfsRootWindowsPath + path;
        }
        return path;
    }


    /**
     * 本地解压zip包并删除压缩文件
     *
     * @param sourcePath zip源文件 例如：/abc/z.zip
     * @param targetPath 解压后的目标文件夹 例如：/abc/
     * @return boolean
     */
    public boolean unzipLocalPath(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        if (!sourcePath.toLowerCase().endsWith(ZIP)) {
            return false;
        }
        //绝对路径
        String sourceAbsolutePath = nfsConfig.getRootDir() + sourcePath;
        String targetPathAbsolutePath = nfsConfig.getRootDir() + targetPath;
        ZipFile zipFile = null;
        InputStream in = null;
        OutputStream out = null;
        File sourceFile = new File(compatiblePath(sourceAbsolutePath));
        File targetFileDir = new File(compatiblePath(targetPathAbsolutePath));
        if (!targetFileDir.exists()) {
            boolean targetMkdir = targetFileDir.mkdirs();
            if (!targetMkdir) {
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}failed to create target folder before decompression", sourceAbsolutePath);
            }
        }
        try {
            zipFile = new ZipFile(sourceFile);
            //判断压缩文件编码方式,并重新获取文件对象
            try {
                zipFile.close();
                zipFile = new ZipFile(sourceFile, CHARACTER_GBK);
            } catch (Exception e) {
                zipFile.close();
                zipFile = new ZipFile(sourceFile);
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}the encoding mode of decompressed compressed file is changed to UTF-8:{}", sourceAbsolutePath, e);
            }
            ZipEntry entry;
            Enumeration enumeration = zipFile.getEntries();
            while (enumeration.hasMoreElements()) {
                entry = (ZipEntry) enumeration.nextElement();
                String entryName = entry.getName();
                File fileDir;
                if (entry.isDirectory()) {
                    fileDir = new File(targetPathAbsolutePath + entry.getName());
                    if (!fileDir.exists()) {
                        boolean fileMkdir = fileDir.mkdirs();
                        if (!fileMkdir) {
                            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "failed to create folder {} while decompressing {}", fileDir, sourceAbsolutePath);
                        }
                    }
                } else {
                    //若文件夹未创建则创建文件夹
                    if (entryName.contains(FILE_SEPARATOR)) {
                        String zipDirName = entryName.substring(MagicNumConstant.ZERO, entryName.lastIndexOf(FILE_SEPARATOR));
                        fileDir = new File(targetPathAbsolutePath + zipDirName);
                        if (!fileDir.exists()) {
                            boolean fileMkdir = fileDir.mkdirs();
                            if (!fileMkdir) {
                                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "failed to create folder {} while decompressing {}", fileDir, sourceAbsolutePath);
                            }
                        }
                    }
                    in = zipFile.getInputStream((ZipArchiveEntry) entry);
                    out = new FileOutputStream(new File(targetPathAbsolutePath, entryName));
                    IOUtils.copyLarge(in, out);
                    in.close();
                    out.close();
                }
            }
            boolean deleteZipFile = sourceFile.delete();
            if (!deleteZipFile) {
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}compressed file deletion failed after decompression", sourceAbsolutePath);
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}decompression failed: {}", sourceAbsolutePath, e);
            return false;
        } finally {
            //关闭未关闭的io流
            closeIoFlow(sourceAbsolutePath, zipFile, in, out);
        }

    }

    /**
     * 关闭未关闭的io流
     *
     * @param sourceAbsolutePath 源路径
     * @param zipFile            压缩文件对象
     * @param in                 输入流
     * @param out                输出流
     */
    private void closeIoFlow(String sourceAbsolutePath, ZipFile zipFile, InputStream in, OutputStream out) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}input stream shutdown failed: {}", sourceAbsolutePath, e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}output stream shutdown failed: {}", sourceAbsolutePath, e);
            }
        }
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "{}input stream shutdown failed: {}", sourceAbsolutePath, e);
            }
        }
    }


    /**
     * 复制单个文件到指定目录下  单个文件
     *
     * @param sourcePath 需要复制的文件  例如：/abc/def/cc.txt
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    private boolean copyLocalFile(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = formatPath(sourcePath);
        targetPath = formatPath(targetPath);
        try (InputStream input = new FileInputStream(sourcePath);
             FileOutputStream output = new FileOutputStream(targetPath)) {
            FileCopyUtils.copy(input, output);
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, " failed to copy file original path: {} ,target path： {} ,copyLocalFile:{} ", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * NFS 复制目录到指定目录下  多个文件  包含目录与文件并存情况
     *
     * 通过本地文件复制方式
     *
     * @param sourcePath 需要复制的文件目录  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    public boolean copyPath(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = formatPath(sourcePath);
        targetPath = formatPath(targetPath);
        try {
            return copyLocalPath(nfsConfig.getRootDir() + sourcePath, nfsConfig.getRootDir() + targetPath);
        } catch (Exception e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, " failed to Copy file original path: {} ,target path： {} ,copyPath: {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 复制文件 到指定目录下  多个文件  包含目录与文件并存情况
     *
     * @param sourcePath 需要复制的文件目录  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    private boolean copyLocalPath(String sourcePath, String targetPath) {
        if (!StringUtils.isEmpty(sourcePath) && !StringUtils.isEmpty(targetPath)) {
            sourcePath = formatPath(sourcePath);
            if (sourcePath.endsWith(FILE_SEPARATOR)) {
                sourcePath = sourcePath.substring(MagicNumConstant.ZERO, sourcePath.lastIndexOf(FILE_SEPARATOR));
            }
            targetPath = formatPath(targetPath);
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) {
                File[] files = sourceFile.listFiles();
                if (files != null && files.length != 0) {
                    for (File file : files) {
                        try {
                            if (file.isDirectory()) {
                                File fileDir = new File(targetPath + FILE_SEPARATOR + file.getName());
                                if (!fileDir.exists()) {
                                    fileDir.mkdirs();
                                }
                                copyLocalPath(sourcePath + FILE_SEPARATOR + file.getName(), targetPath + FILE_SEPARATOR + file.getName());
                            }
                            if (file.isFile()) {
                                File fileTargetPath = new File(targetPath);
                                if (!fileTargetPath.exists()) {
                                    fileTargetPath.mkdirs();
                                }
                                copyLocalFile(file.getAbsolutePath(), targetPath + FILE_SEPARATOR + file.getName());
                            }
                        } catch (Exception e) {
                            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "failed to copy folder original path: {} , target path ： {} ,copyLocalPath: {}", sourcePath, targetPath, e);
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 替换路径中多余的 "/"
     *
     * @param path
     * @return String
     */
    public String formatPath(String path) {
        if (!StringUtils.isEmpty(path)) {
            return path.replaceAll("///*", FILE_SEPARATOR);
        }
        return path;
    }

    /**
     * 拷贝文件
     *
     * @param sourcePath 需要复制的文件  例如：/abc/def/cc.txt
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return
     */
    public boolean copyFile(String sourcePath, String targetPath, Integer type) {
        //绝对路径
        String sourceAbsolutePath = formatPath(nfsConfig.getRootDir() + sourcePath);
        String targetPathAbsolutePath = formatPath(nfsConfig.getRootDir() + targetPath);
        String[] command;
        if (CopyTypeEnum.COPY_FILE.getKey().equals(type)) {
            command = new String[]{"/bin/sh", "-c", String.format(COPY_COMMAND, userName, nfsIp, targetPathAbsolutePath, sourceAbsolutePath, targetPathAbsolutePath)};
        } else {
            command = new String[]{"/bin/sh", "-c", String.format(COPY_DIR_COMMAND, userName, nfsIp, targetPathAbsolutePath, sourceAbsolutePath, targetPathAbsolutePath)};
        }
        boolean flag = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            if (isCopySuccess(process)) {
                flag = true;
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "copy file failed, filePath:{}, targetPath:{}", sourcePath, targetPath, e);
        }
        return flag;
    }

    /**
     * 拷贝文件并重命名
     *
     * @param sourcePath 需要复制的文件  例如：/abc/def/aa.py
     * @param targetPath 需要放置的目标目录 例如：/abc/dd/bb.py
     * @return
     */
    public boolean copyFileAndRename(String sourcePath, String targetPath) {
        //绝对路径
        String sourceAbsolutePath = formatPath(nfsConfig.getRootDir() + sourcePath);
        String targetPathAbsolutePath = formatPath(nfsConfig.getRootDir() + targetPath);
        String[] command = new String[]{"/bin/sh", "-c", String.format(COPY_RENAME_COMMAND, userName, nfsIp, sourceAbsolutePath, targetPathAbsolutePath)};
        boolean flag = false;
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            if (isCopySuccess(process)) {
                flag = true;
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "copy file failed, filePath:{}, targetPath:{}", sourcePath, targetPath, e);
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
        boolean flag = false;
        try (InputStream stream = process.getInputStream();
             InputStreamReader iReader = new InputStreamReader(stream);
             BufferedReader bReader = new BufferedReader(iReader)) {
            String line;
            while (Objects.nonNull(line = bReader.readLine())) {
                boolean temp = line.contains("success");
                if (temp) {
                    flag = true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.LOCAL_FILE_UTIL, "Read stream failed : {}", e);
        }
        return flag;
    }

    /**
     * 路径是否存在
     * @param path 路径
     * @return     true 存在 false 不存在
     */
    public boolean isExist(String path) {
        if(StringUtils.isBlank(path)){
            return false;
        }
        File file = new File(path);
        return file.exists();
    }
}