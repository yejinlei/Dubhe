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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ZipUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.util.IOUtils;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.file.dto.FileDTO;
import org.dubhe.biz.file.dto.FilePageDTO;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * @description 本地文件存储接口实现类
 * @date 2021-04-23
 */
@Component(value = "hostFileStoreApiImpl")
public class HostFileStoreApiImpl implements FileStoreApi {

    private static final String FILE_SEPARATOR = File.separator;

    private static final String ZIP = ".zip";

    private static final String CHARACTER_GBK = "GBK";

    private static final String OS_NAME = "os.name";

    private static final String WINDOWS = "Windows";

    @Value("${storage.file-store-root-path}")
    private String rootDir;

    @Value("/${minio.bucketName}/")
    private String bucket;

    @Value("${storage.file-store-root-windows-path}")
    private String rootWindowsPath;

    @Override
    public String getRootDir() {
        return rootDir;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    /**
     * 校验文件或文件夹是否不存在（true为存在，false为不存在）
     *
     * @param path 文件路径
     * @return boolean
     */
    @Override
    public boolean fileOrDirIsExist(String path) {
        path = compatibleAbsolutePath(path);
        if (StringUtils.isBlank(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 校验是否是文件夹（true为文件夹，false为文件）
     *
     * @param path 文件路径
     * @return boolean
     */
    @Override
    public boolean isDirectory(String path) {
        path = compatibleAbsolutePath(path);
        if (StringUtils.isBlank(path)) {
            return false;
        }
        File file = new File(path);
        return file.isDirectory();
    }

    /**
     * 过滤路径下文件后缀名(不区分大小写)并返回符合条件的文件集合
     *
     * @param path 文件夹路径
     * @param fileSuffix 文件后缀名(不区分大小写)
     * @return List<String> 返回符合条件的文件集合
     */
    @Override
    public List<String> filterFileSuffix(String path, String fileSuffix) {
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(fileSuffix)) {
            return null;
        }
        path = compatibleAbsolutePath(path);
        File sourceFile = new File(path);
        List<String> filePaths = new ArrayList<>();
        if (!sourceFile.exists()) {
            return null;
        }
        File[] files = sourceFile.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().toLowerCase().endsWith(fileSuffix.toLowerCase())) {
                        filePaths.add(formatPath(path + FILE_SEPARATOR + file.getName()));
                    }
                }
            }
        }

        return filePaths;
    }


    /**
     * 创建指定目录
     *
     * @param dir 需要创建的目录 例如：/abc/def
     * @return boolean
     */
    @Override
    public boolean createDir(String dir) {
        dir = compatibleAbsolutePath(dir);
        try {
            File file = FileUtil.mkdir(dir);
            if (file != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "创建文件夹异常: {}", e);
            return false;
        }
    }

    /**
     * 创建多个指定目录
     *
     * @param paths
     * @return boolean
     */
    @Override
    public boolean createDirs(String... paths) {
        if (null == paths || paths.length < MagicNumConstant.ONE) {
            return true;
        }
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            if (!createDir(path)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定目录中创建文件
     *
     * @param dir      需要创建的目录 例如：/abc/def
     * @param fileName 需要创建的文件 例如：dd.txt
     * @return boolean
     */
    @Override
    public boolean createFile(String dir, String fileName) {
        try {
            File file = FileUtil.touch(dir + SymbolConstant.SLASH + fileName);
            if (file != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "创建文件异常: {}", e);
            return false;
        }
    }

    /**
     * 新建或追加文件
     *
     * @param filePath  文件绝对路径
     * @param content   文件内容
     * @param append    文件是否是追加
     * @return
     */
    @Override
    public boolean createOrAppendFile(String filePath, String content, boolean append) {
        File file = new File(filePath);
        FileOutputStream outputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file, append);
            outputStream.write(content.getBytes(CharsetUtil.defaultCharset()));
            outputStream.flush();
        } catch (IOException e) {
            LogUtil.error(LogEnum.FILE_UTIL, e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LogUtil.error(LogEnum.FILE_UTIL, e);
                }
            }
        }
        return true;
    }

    /**
     * 删除目录 或者文件
     *
     * @param dirOrFile 需要删除的目录 或者文件 例如：/abc/def  或者 /abc/def/dd.txt
     * @return boolean
     */
    @Override
    public boolean deleteDirOrFile(String dirOrFile) {
        return FileUtil.del(dirOrFile);
    }

    /**
     * 复制文件 到指定目录下  单个文件
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/dd.txt
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    @Override
    public boolean copyFile(String sourceFile, String targetPath) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourceFile = compatibleAbsolutePath(sourceFile);
        targetPath = compatibleAbsolutePath(targetPath);
        try {
            String fileName = sourceFile.substring(sourceFile.lastIndexOf(SymbolConstant.SLASH) + 1);
            File file = FileUtil.copy(sourceFile, targetPath + SymbolConstant.SLASH + fileName, true);
            if (file != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "复制文件异常: {}", e);
            return false;
        }
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath, Integer type) {
        return false;
    }

    /**
     * 复制路径下文件并重命名
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/dd.txt
     * @param targetFile 目标文件全路径
     * @return boolean
     */
    @Override
    public boolean copyFileAndRename(String sourceFile, String targetFile) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetFile)) {
            return false;
        }
        sourceFile = compatibleAbsolutePath(sourceFile);
        targetFile = compatibleAbsolutePath(targetFile);
        try {
            File copyFile = FileUtil.copy(sourceFile, targetFile, true);
            if (copyFile != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.SERVING, " failed to copy file original path: {} ,target path： {} ,exception: {}", sourceFile, targetFile, e);
            return false;
        }
    }

    /**
     * 复制路径下文件及文件夹到指定目录下  多个文件  包含目录与文件并存情况
     *
     * 通过本地文件复制方式
     *
     * @param sourcePath 需要复制的文件目录  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    @Override
    public boolean copyPath(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = compatibleAbsolutePath(sourcePath);
        targetPath = compatibleAbsolutePath(targetPath);
        try {
            return copyLocalPath(sourcePath, targetPath);
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, " failed to Copy file original path: {} ,target path： {} ,Exception: {}", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * 复制文件夹到指定目录下
     *
     * @param sourcePath 需要复制的文件夹  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd 复制成功路径/abc/dd/def*
     * @return boolean
     */
    @Override
    public boolean copyDir(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = compatibleAbsolutePath(sourcePath);
        targetPath = formatPath(compatibleAbsolutePath(targetPath) + FILE_SEPARATOR + new File(sourcePath).getName());
        try {
            return copyLocalPath(sourcePath, targetPath);
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, " failed to Copy file original path: {} ,target path： {} ,Exception: {}", sourcePath, targetPath, e);
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
                            LogUtil.error(LogEnum.FILE_UTIL, "failed to copy folder original path: {} , target path ： {} ,Exception: {}", sourcePath, targetPath, e);
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
            LogUtil.error(LogEnum.FILE_UTIL, " failed to copy file original path: {} ,target path： {} ,Exception:{} ", sourcePath, targetPath, e);
            return false;
        }
    }

    /**
     * zip解压并删除压缩文件
     * @param sourceFile zip源文件 例如：/abc/z.zip
     */
    @Override
    public boolean unzip(String sourceFile, String targetPath) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        if (!sourceFile.toLowerCase().endsWith(ZIP)) {
            return false;
        }
        //绝对路径
        String sourceAbsolutePath = getRootDir() + sourceFile;
        String targetPathAbsolutePath = getRootDir() + targetPath;
        ZipFile zipFile = null;
        InputStream in = null;
        OutputStream out = null;
        File absoluteSourceFile = new File(compatiblePath(sourceAbsolutePath));
        File targetFileDir = new File(compatiblePath(targetPathAbsolutePath));
        if (!targetFileDir.exists()) {
            boolean targetMkdir = targetFileDir.mkdirs();
            if (!targetMkdir) {
                LogUtil.error(LogEnum.FILE_UTIL, "{} failed to create target folder before decompression", sourceAbsolutePath);
            }
        }
        try {
            zipFile = new ZipFile(absoluteSourceFile);
            //判断压缩文件编码方式,并重新获取文件对象
            try {
                zipFile.close();
                zipFile = new ZipFile(absoluteSourceFile, CHARACTER_GBK);
            } catch (Exception e) {
                zipFile.close();
                zipFile = new ZipFile(absoluteSourceFile);
                LogUtil.error(LogEnum.FILE_UTIL, "{} the encoding mode of decompressed compressed file is changed to UTF-8,Exception:{}", sourceAbsolutePath, e);
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
                            LogUtil.error(LogEnum.FILE_UTIL, "failed to create folder {} while decompressing {}", fileDir, sourceAbsolutePath);
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
                                LogUtil.error(LogEnum.FILE_UTIL, "failed to create folder {} while decompressing {}", fileDir, sourceAbsolutePath);
                            }
                        }
                    }
                    in = zipFile.getInputStream((ZipArchiveEntry) entry);
                    out = new FileOutputStream(new File(targetPathAbsolutePath, entryName));
                    org.apache.commons.io.IOUtils.copyLarge(in, out);
                    in.close();
                    out.close();
                }
            }
            boolean deleteZipFile = absoluteSourceFile.delete();
            if (!deleteZipFile) {
                LogUtil.error(LogEnum.FILE_UTIL, "{} compressed file deletion failed after decompression", sourceAbsolutePath);
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.FILE_UTIL, "{} decompression failed,Exception: {}", sourceAbsolutePath, e);
            return false;
        } finally {
            //关闭未关闭的io流
            closeIoFlow(sourceAbsolutePath, zipFile, in, out);
        }
    }

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
            path = path.replace(getRootDir(), SymbolConstant.SLASH);
            path = path.replace(SymbolConstant.SLASH, FILE_SEPARATOR);
            path = rootWindowsPath + path;
        }
        return path;
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
                LogUtil.error(LogEnum.FILE_UTIL, "{} input stream shutdown failed,Exception: {}", sourceAbsolutePath, e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.FILE_UTIL, "{} output stream shutdown failed,Exception: {}", sourceAbsolutePath, e);
            }
        }
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                LogUtil.error(LogEnum.FILE_UTIL, "{} input stream shutdown failed,Exception: {}", sourceAbsolutePath, e);
            }
        }
    }

    /**
     * 解压压缩包 包含目录与子目录
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/aaa.rar
     * @return boolean
     */
    @Override
    public boolean unzip(String sourceFile) {
        try {
            File file = ZipUtil.unzip(sourceFile);
            if (file != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "解压异常: {}", e);
            return false;
        }
    }

    /**
     * 压缩 目录 或者文件 到压缩包
     *
     * @param dirOrFile 目录或者文件  例如： /abc/def/aaa.txt , /abc/def
     * @param zipPath   压缩包全路径名 例如： /abc/def/aa.zip
     * @return boolean
     */
    @Override
    public boolean zipDirOrFile(String dirOrFile, String zipPath) {
        try {
            File file = ZipUtil.zip(dirOrFile, zipPath);
            if (file != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.error(LogEnum.FILE_UTIL, "压缩异常: {}", e);
            return false;
        }
    }

    @Override
    public BufferedInputStream getInputStream(String path) {
        return FileUtil.getInputStream(path);
    }

    @Override
    public void download(String path, HttpServletResponse response) {
        if (path == null) {
            return;
        }
        FileInputStream fis = null;
        ServletOutputStream out = null;
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
            out = response.getOutputStream();
            IOUtils.copy(fis, out);
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //此处记得关闭输出Servlet流
            IoUtil.close(out);
        }
    }

    @Override
    public void filterFilePageWithPath(FilePageDTO filePageDTO) {
        if (ObjectUtil.isEmpty(filePageDTO) || StringUtils.isEmpty(filePageDTO.getFilePath())) {
            return;
        }
        File file = new File(filePageDTO.getFilePath());
        if (!file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        if (files.length > 0) {
            filePageDTO.setTotal(Long.valueOf(files.length));
            int start = (filePageDTO.getPageNum() - 1) * filePageDTO.getPageSize();
            int end = filePageDTO.getPageNum() * filePageDTO.getPageSize();
            List<FileDTO> fileDTOS = new ArrayList<>();
            for (int i = (start <= files.length ? start : files.length); i < (end <= files.length ? end : files.length); i++) {
                FileDTO fileDTO = FileDTO.builder().name(files[i].getName())
                        .path(files[i].getAbsolutePath())
                        .lastModified(new Date(files[i].lastModified()))
                        .size(files[i].length())
                        .dir(files[i].isDirectory()).build();
                fileDTOS.add(fileDTO);
            }
            filePageDTO.setRows(fileDTOS);
        }
    }
}
