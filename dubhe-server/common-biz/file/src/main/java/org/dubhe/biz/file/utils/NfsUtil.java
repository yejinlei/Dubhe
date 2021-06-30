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
import com.emc.ecs.nfsclient.nfs.io.Nfs3File;
import com.emc.ecs.nfsclient.nfs.io.NfsFileInputStream;
import com.emc.ecs.nfsclient.nfs.io.NfsFileOutputStream;
import lombok.Getter;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.config.NfsConfig;
import org.dubhe.biz.file.exception.NfsBizException;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @description NFS Util
 * ！！！注意： 使用NFSFile 对象 业务上自行需要关闭 NFS 对象
 * @date 2020-05-12
 */
@Deprecated
@Component
@Getter
public class NfsUtil {

    private final NfsConfig nfsConfig;

    private static final String FILE_SEPARATOR = "/";

    private static final String ZIP = ".zip";

    private static final String CHARACTER_GBK = "GBK";

    private static final String CHARACTER_UTF_8 = "UTF-8";

    private static final String UNDER_LINE = "_";

    @Autowired
    private NfsPool nfsPool;


    public NfsUtil(NfsConfig nfsConfig) {
        this.nfsConfig = nfsConfig;
    }

    private String getReplaceRootPathRegex() {
        String rootPath = nfsConfig.getRootDir();
        return "^" + rootPath.substring(MagicNumConstant.ZERO, rootPath.length() - MagicNumConstant.ONE);
    }

    /**
     * 获取NFS3File 对象
     *
     * @param path 文件路径
     * @return Nfs3File
     */
    public Nfs3File getNfs3File(String path) {
        if (StringUtils.isEmpty(formatPath(path))) {
            LogUtil.error(LogEnum.NFS_UTIL, "传入的NFS3初始化路径 {}  ,无法初始化NFS3 ", path);
            throw new NfsBizException("初始化路径：" + path + " 不合法");
        }
        Nfs3File nfs3File;
        try {
            nfs3File = new Nfs3File(nfsPool.getNfs(), path);
            LogUtil.info(LogEnum.NFS_UTIL, "成功获取NFS3File对象 ： {} ", nfs3File.getName());
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "获取NFS3File对象失败 ：", e);
            throw new NfsBizException("未获取到NFS连接或者NFS连接池已满" + e.getMessage());
        }
        return nfs3File;
    }

    /**
     * 获取NFS3File 对象文件的输入流
     *
     * @param nfs3File
     * @return BufferedInputStream
     */
    public BufferedInputStream getInputStream(Nfs3File nfs3File) {
        BufferedInputStream stream = null;
        try {
            if (!nfs3File.isFile()) {
                throw new NfsBizException("此路径下查找到的对象不是文件类型，请检查文件类型是否正确！");
            }
            stream = new BufferedInputStream(new NfsFileInputStream(nfs3File));
        } catch (IOException e) {
            throw new NfsBizException("nfs获取对象输出流失败！");
        }
        return stream;
    }

    /**
     * 获取NFS3File 对象文件的输入流
     *
     * @param path 文件路径
     * @return BufferedInputStream
     */
    public BufferedInputStream getInputStream(String path) {
        Nfs3File nfs3File = getNfs3File(formatPath(path));
        if (nfs3File == null) {
            throw new NfsBizException("此路径" + path + "下没有文件可以加载！");
        }
        return getInputStream(nfs3File);
    }

    /**
     * 校验文件或文件夹是否不存在（true为不存在，false为存在）
     *
     * @param path 文件路径
     * @return boolean
     */
    public boolean fileOrDirIsEmpty(String path) {
        if (!StringUtils.isEmpty(path)) {
            path = formatPath(path.startsWith(nfsConfig.getRootDir()) ? path.replaceFirst(nfsConfig.getRootDir(), StrUtil.SLASH) : path);
            Nfs3File nfs3File = getNfs3File(path);
            try {
                if (nfs3File.exists()) {
                    return false;
                }
            } catch (IOException e) {
                LogUtil.error(LogEnum.NFS_UTIL, "判断NFS File异常: ", e);
                return true;
            } finally {
                nfsPool.revertNfs(nfs3File.getNfs());
            }
        }
        return true;

    }


    /**
     * 创建指定NFS目录
     *
     * @param dir 需要创建的目录 例如：/abc/def
     * @return boolean
     */
    public boolean createDir(String dir) {
        if (!StringUtils.isEmpty(dir)) {
            dir = formatPath(dir);
            String[] paths = dir.substring(MagicNumConstant.ONE).split(FILE_SEPARATOR);
            StringBuilder sbPath = new StringBuilder();
            for (String path : paths) {
                sbPath.append(FILE_SEPARATOR).append(path);
                Nfs3File nfs3File = getNfs3File(sbPath.toString());
                try {
                    if (!nfs3File.exists()) {
                        nfs3File.mkdirs();
                    }
                } catch (IOException e) {
                    LogUtil.error(LogEnum.NFS_UTIL, "创建NFS目录失败:", e);
                    return false;
                } finally {
                    nfsPool.revertNfs(nfs3File.getNfs());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 创建多个指定目录
     *
     * @param removeNfsRootPath
     * @param paths
     * @return boolean
     */
    private boolean createDir(boolean removeNfsRootPath, String... paths) {
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            String formatPath = path;
            if (removeNfsRootPath) {
                formatPath = formatPath.replaceAll(this.getReplaceRootPathRegex(), "");
            }
            boolean res = createDir(formatPath);
            if (!res) {
                return false;
            }
        }
        return true;
    }


    /**
     * 创建指定NFS目录
     *
     * @param paths       路径  例如：/nfs/abc/def/   /abc/def/
     * @param nfsRootPath 是否包含nfs根目录 true:包含  false:不包含
     * @return boolean
     */
    public boolean createDirs(boolean nfsRootPath, String... paths) {
        if (null == paths || paths.length < MagicNumConstant.ONE) {
            return true;
        }
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            String formatPath = path;
            if (nfsRootPath) {
                formatPath = formatPath.replaceAll(this.getReplaceRootPathRegex(), "");
            }
            if (!createDir(formatPath)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定目录NFS中创建文件
     *
     * @param dir      需要创建的目录 例如：/abc/def
     * @param fileName 需要创建的文件 例如：dd.txt
     * @return boolean
     */
    public boolean createFile(String dir, String fileName) {
        if (!StringUtils.isEmpty(dir) && !StringUtils.isEmpty(fileName)) {
            dir = formatPath(dir);
            Nfs3File nfs3File = getNfs3File(dir + FILE_SEPARATOR + fileName);
            try {
                if (!nfs3File.exists()) {
                    nfs3File.mkdirs();
                }
                nfs3File.createNewFile();
                return true;
            } catch (IOException e) {
                LogUtil.error(LogEnum.NFS_UTIL, "创建NFS文件失败: ", e);
            } finally {
                nfsPool.revertNfs(nfs3File.getNfs());
            }
        }
        return false;
    }

    /**
     * 删除NFS目录 或者文件
     *
     * @param dirOrFile 需要删除的目录 或者文件 例如：/abc/def  或者 /abc/def/dd.txt
     * @return boolean
     */
    public boolean deleteDirOrFile(String dirOrFile) {
        if (!StringUtils.isEmpty(dirOrFile)) {
            dirOrFile = formatPath(dirOrFile);
            try {
                List<Nfs3File> nfs3FileList = getNfs3File(dirOrFile).listFiles();
                //删除目录下的子文件
                if (!CollectionUtils.isEmpty(nfs3FileList)) {
                    for (Nfs3File nfs3File : nfs3FileList) {
                        if (nfs3File.isDirectory()) {
                            deleteDirOrFile(nfs3File.getPath());
                        } else if (nfs3File.isFile()) {
                            try {
                                nfs3File.delete();
                            } finally {
                                nfsPool.revertNfs(nfs3File.getNfs());
                            }
                        }
                    }
                }
                Nfs3File sourceNfsFile = getNfs3File(dirOrFile);
                try {
                    sourceNfsFile.delete();
                } finally {
                    nfsPool.revertNfs(sourceNfsFile.getNfs());
                }
                return true;
            } catch (Exception e) {
                LogUtil.error(LogEnum.NFS_UTIL, "删除NFS目录失败:", e);
            }
        }
        return false;
    }


    /**
     * 上传文件到 NFS 指定目录
     *
     * @param sourceFile 本地文件 包含路径 例如：/abc/def/gg.txt
     * @param targetDir  指定目录 例如：/abc/def
     * @return boolean
     */
    public boolean uploadFileToNfs(String sourceFile, String targetDir) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetDir)) {
            return false;
        }
        sourceFile = formatPath(sourceFile);
        targetDir = formatPath(targetDir);
        //本地文件对象
        File localFile = new File(sourceFile);
        Nfs3File nfs3File = getNfs3File(targetDir + FILE_SEPARATOR + localFile.getName());
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(localFile));
             BufferedOutputStream outputStream = new BufferedOutputStream(new NfsFileOutputStream(nfs3File))) {
            IOUtils.copyLarge(inputStream, outputStream);
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "上传失败: ", e);
        } finally {
            nfsPool.revertNfs(nfs3File.getNfs());
        }
        return false;
    }

    /**
     * 下载NFS文件到本地目录
     *
     * @param sourceFile 指定文件 例如：/abc/def/dd.txt
     * @param targetPath 目标目录 例如: /abc/dd
     * @return boolean
     */
    public boolean downFileFormNfs(String sourceFile, String targetPath) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourceFile = formatPath(sourceFile);
        targetPath = formatPath(targetPath);
        Nfs3File nfsFile = getNfs3File(sourceFile);
        if (nfsFile != null) {
            try (InputStream inputStream = new BufferedInputStream(new NfsFileInputStream(nfsFile));
                 OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(targetPath + FILE_SEPARATOR + nfsFile.getName())))) {
                IOUtils.copyLarge(inputStream, outputStream);
                return true;
            } catch (IOException e) {
                LogUtil.error(LogEnum.NFS_UTIL, "下载失败:", e);
            } finally {
                nfsPool.revertNfs(nfsFile.getNfs());
            }
        }
        return false;
    }


    /**
     * NFS 复制文件 到指定目录下  单个文件
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/dd.txt
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    public boolean copyFile(String sourceFile, String targetPath) {
        if (StringUtils.isEmpty(sourceFile) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourceFile = formatPath(sourceFile);
        targetPath = formatPath(targetPath);
        Nfs3File sourceNfsFile = null;
        Nfs3File targetNfsFileNew = null;
        try {
            sourceNfsFile = getNfs3File(sourceFile);
            targetNfsFileNew = getNfs3File(targetPath + FILE_SEPARATOR + sourceNfsFile.getName());
            if (!targetNfsFileNew.exists()) {
                createDir(targetPath);
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "创建目标文件失败： ", e);
        }
        try (InputStream inputStream = new BufferedInputStream(new NfsFileInputStream(sourceNfsFile));
             OutputStream outputStream = new BufferedOutputStream(new NfsFileOutputStream(targetNfsFileNew))) {
            targetNfsFileNew.createNewFile();
            IOUtils.copyLarge(inputStream, outputStream);
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "复制失败:", e);
            return false;
        } finally {
            nfsPool.revertNfs(sourceNfsFile.getNfs());
            nfsPool.revertNfs(targetNfsFileNew.getNfs());
        }
    }


    /**
     * NFS 复制目录到指定目录下  多个文件  包含目录与文件并存情况
     *
     * 通过NFS文件复制方式  可能存在NFS RPC协议超时情况
     *
     * @param sourcePath 需要复制的文件目录  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    public boolean copyNfsPath(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = formatPath(sourcePath);
        targetPath = formatPath(targetPath);
        try {
            Nfs3File sourceNfsFile = getNfs3File(sourcePath);
            List<Nfs3File> nfs3FileList = sourceNfsFile.listFiles();
            if (CollectionUtils.isEmpty(nfs3FileList)) {
                createDir(targetPath + sourcePath.substring(sourcePath.lastIndexOf(FILE_SEPARATOR)));
            } else {
                for (Nfs3File nfs3File : nfs3FileList) {
                    if (nfs3File.isDirectory()) {
                        String newTargetPath = nfs3File.getPath().substring(nfs3File.getPath().lastIndexOf(FILE_SEPARATOR));
                        Nfs3File newNfs3File = getNfs3File(newTargetPath);
                        try {
                            if (!newNfs3File.exists()) {
                                createDir(targetPath + newTargetPath);
                            }
                            copyNfsPath(nfs3File.getPath(), targetPath + newTargetPath);
                        } finally {
                            nfsPool.revertNfs(newNfs3File.getNfs());
                        }
                    }
                    if (nfs3File.isFile()) {
                        copyFile(sourcePath + FILE_SEPARATOR + nfs3File.getName(), targetPath);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "复制失败: ", e);
            return false;
        }
    }

    /**
     * zip解压并删除压缩文件
     * 当压缩包文件较多时，可能会因为RPC超时而解压失败
     * 该方法已废弃，请使用org.dubhe.utils.LocalFileUtil类的unzipLocalPath方法来替代
     * @param sourcePath zip源文件 例如：/abc/z.zip
     * @param targetPath 解压后的目标文件夹 例如：/abc/
     * @return boolean
     */
    @Deprecated
    public boolean unzip(String sourcePath, String targetPath) {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return false;
        }
        sourcePath = formatPath(sourcePath);
        targetPath = formatPath(targetPath);
        if (!sourcePath.toLowerCase().endsWith(ZIP)) {
            return false;
        }
        ArchiveInputStream zIn = null;
        Nfs3File sourceNfsFile = getNfs3File(sourcePath);
        try {
            zIn = new ZipArchiveInputStream(new BufferedInputStream(new NfsFileInputStream(sourceNfsFile)), CHARACTER_GBK, false, true);
            //判断压缩文件编码方式,并重新获取NFS对象流
            try {
                zIn.getNextEntry();
                zIn.close();
                zIn = new ZipArchiveInputStream(new BufferedInputStream(new NfsFileInputStream(sourceNfsFile)), CHARACTER_GBK, false, true);
            } catch (Exception e) {
                zIn.close();
                zIn = new ZipArchiveInputStream(new BufferedInputStream(new NfsFileInputStream(sourceNfsFile)), CHARACTER_UTF_8, false, true);
            }
            ZipEntry entry;
            while ((entry = (ZipEntry) zIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    createDir(targetPath + FILE_SEPARATOR + entry.getName());
                } else {
                    //若文件夹未创建则创建文件夹
                    if (entry.getName().contains(FILE_SEPARATOR)) {
                        String entryName = entry.getName();
                        String zipDirName = entryName.substring(MagicNumConstant.ZERO, entryName.lastIndexOf(FILE_SEPARATOR));
                        createDir(targetPath + FILE_SEPARATOR + zipDirName);
                    }
                    Nfs3File nfs3File = getNfs3File(targetPath + FILE_SEPARATOR + entry.getName());
                    try {
                        if (!nfs3File.exists()) {
                            nfs3File.createNewFile();
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(new NfsFileOutputStream(nfs3File));
                        IOUtils.copyLarge(zIn, bos);
                        bos.flush();
                        bos.close();
                    } finally {
                        nfsPool.revertNfs(nfs3File.getNfs());
                    }
                }
            }
            sourceNfsFile.delete();
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "解压失败: ", e);
            return false;
        } finally {
            nfsPool.revertNfs(sourceNfsFile.getNfs());
            if (zIn != null) {
                try {
                    zIn.close();
                } catch (IOException e) {
                    LogUtil.error(LogEnum.NFS_UTIL, "输入流关闭失败: ", e);
                }
            }
        }

    }

    /**
     * NFS 解压压缩包 包含目录与子目录
     *
     * @param sourcePath 需要复制的文件  例如：/abc/def/aaa.rar
     * @return boolean
     */
    public boolean unZip(String sourcePath) {
        sourcePath = formatPath(sourcePath);
        if (StringUtils.isEmpty(sourcePath)) {
            return false;
        }
        String fileDir = sourcePath.substring(MagicNumConstant.ZERO, sourcePath.lastIndexOf(FILE_SEPARATOR));
        ZipEntry zipEntry = null;
        try (ZipInputStream zipInputStream = new ZipInputStream(new NfsFileInputStream(getNfs3File(sourcePath)))) {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    createDir(fileDir + FILE_SEPARATOR + zipEntry.getName());
                    continue;
                }
                Nfs3File targetNfsFileNew = getNfs3File(fileDir + FILE_SEPARATOR + zipEntry.getName());
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new NfsFileOutputStream(targetNfsFileNew))) {
                    targetNfsFileNew.createNewFile();
                    IOUtils.copyLarge(zipInputStream, bufferedOutputStream);
                } finally {
                    nfsPool.revertNfs(targetNfsFileNew.getNfs());
                }
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "解压文件失败 : ", e);
            return false;
        }
    }

    /**
     * 压缩NFS 目录 或者文件 到压缩包
     *
     * @param dirOrFile 目录或者文件  例如： /abc/def/aaa.txt , /abc/def
     * @param zipName   压缩包名称 例如： aa,bb,cc
     * @return boolean
     */
    public boolean zipDirOrFile(String dirOrFile, String zipName) {
        Nfs3File nfs3File = getNfs3File(formatPath(dirOrFile));
        try (ZipOutputStream zipOutputStream = getFileZipOutputStream(getNfsFilePath(formatPath(dirOrFile)), zipName)) {
            zipFiles(zipOutputStream, nfs3File);
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "压缩文件失败 : ", e);
            return false;
        } finally {
            nfsPool.revertNfs(nfs3File.getNfs());
        }
    }

    /**
     * 获取NFS 文件压缩目录
     *
     * @param dirOrFile
     * @return String
     */
    private String getNfsFilePath(String dirOrFile) throws IOException {
        Nfs3File nfs3File = getNfs3File(formatPath(dirOrFile));
        if (nfs3File.isFile()) {
            nfsPool.revertNfs(nfs3File.getNfs());
            return dirOrFile.substring(MagicNumConstant.ZERO, dirOrFile.lastIndexOf(FILE_SEPARATOR));
        }
        return dirOrFile;
    }

    /**
     * 根据文件路劲 获取Zip文件流
     *
     * @param dirOrFile
     * @param zipName
     * @return ZipOutputStream
     */
    private ZipOutputStream getFileZipOutputStream(String dirOrFile, String zipName) throws IOException {
        Nfs3File targetNfsFileNew = getNfs3File(getNfsFilePath(formatPath(dirOrFile)) + FILE_SEPARATOR + zipName + ZIP);
        targetNfsFileNew.createNewFile();
        return new ZipOutputStream(new NfsFileOutputStream(targetNfsFileNew));
    }

    /**
     * 压缩文件和文件夹
     *
     * @param zipOutputStream
     * @param nfs3File
     * @return boolean
     */
    public boolean zipFiles(ZipOutputStream zipOutputStream, Nfs3File nfs3File) {
        try {
            if (nfs3File.isFile()) {
                compressZip(zipOutputStream, nfs3File, "");
            } else {
                List<Nfs3File> nfs3FileList = nfs3File.listFiles();
                if (!CollectionUtils.isEmpty(nfs3FileList)) {
                    for (Nfs3File nfs3FileChildren : nfs3FileList) {
                        zipFiles(zipOutputStream, nfs3FileChildren);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "压缩文件失败 : ", e);
            return false;
        }
    }

    /**
     * 单个文件压缩
     *
     * @param zipOutputStream
     * @param nfs3File
     */
    public void compressZip(ZipOutputStream zipOutputStream, Nfs3File nfs3File, String childPath) {
        try (InputStream inputStream = new BufferedInputStream(new NfsFileInputStream(nfs3File))) {
            if (StringUtils.isEmpty(childPath)) {
                zipOutputStream.putNextEntry(new ZipEntry(nfs3File.getName()));
            } else {
                zipOutputStream.putNextEntry(new ZipEntry(childPath + FILE_SEPARATOR + nfs3File.getName()));
            }
            byte[] buffer = new byte[1024 * 10];
            int length;
            while ((length = inputStream.read(buffer, MagicNumConstant.ZERO, buffer.length)) != -1) {
                zipOutputStream.write(buffer, MagicNumConstant.ZERO, length);
            }
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "解压单个文件异常： ", e);
        } finally {
            nfsPool.revertNfs(nfs3File.getNfs());
        }
    }

    /**
     * 复制文件夹(或文件)到另一个文件夹
     *
     * @param sourcePath 复制文件夹  /abc/def  复制文件：/abc/def/dd.txt
     * @param targetPath /abc/dd/def
     * @return boolean
     */
    public boolean copyDirs(String sourcePath, String targetPath) {
        Nfs3File sourceNfsFile = getNfs3File(formatPath(sourcePath));
        try {
            if (!sourceNfsFile.exists()) {
                LogUtil.error(LogEnum.NFS_UTIL, "sourcePath不存在, 如下{} ", sourcePath);
                return false;
            }
            if (sourceNfsFile.isFile()) {
                return copyFile(sourcePath, targetPath);
            } else if (sourceNfsFile.isDirectory()) {
                targetPath = targetPath + FILE_SEPARATOR + sourceNfsFile.getName();
                boolean bool = createDir(formatPath(targetPath));
                if (!bool) {
                    LogUtil.error(LogEnum.NFS_UTIL, "{}文件夹创建失败... ", targetPath);
                    return false;
                }
                List<Nfs3File> files = sourceNfsFile.listFiles();
                for (Nfs3File file : files) {
                    copyDirs(file.getPath(), targetPath);
                }
            }
            return true;
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "copyDirs失败, sourcePath为 , targetPath为 , 失败原因 ", sourcePath, targetPath, e);
        } finally {
            nfsPool.revertNfs(sourceNfsFile.getNfs());
        }
        return false;
    }

    /**
     * 找到倒数第二新的文件夹
     *
     * @param parentPath 父文件夹
     * @return
     */
    public String find2ndNewDir(String parentPath) {
        Nfs3File parentNfsFile = getNfs3File(formatPath(parentPath));
        try {
            if (!parentNfsFile.exists() || parentNfsFile.isFile()) {
                LogUtil.error(LogEnum.NFS_UTIL, "sourcePath不存在, 如下{} ", parentPath);
                return "";
            }
            List<Nfs3File> files = parentNfsFile.listFiles();
            List<Nfs3File> dirs = new ArrayList<>();
            for (Nfs3File file : files) {
                if (file.isDirectory()) {
                    dirs.add(file);
                }
            }
            if (dirs.size() < MagicNumConstant.TWO) {
                return "";
            }
            dirs.sort((o1, o2) -> {
                try {
                    return (int) (o2.lastModified() - o1.lastModified());
                } catch (IOException e) {
                    LogUtil.error(LogEnum.NFS_UTIL, "执行异常: {} ", e);
                    return MagicNumConstant.ZERO;
                }
            });
            return dirs.get(MagicNumConstant.ONE).getName();

        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "findSecNewDir失败, parentPath为{}, 失败原因{}", parentPath, e);
        } finally {
            nfsPool.revertNfs(parentNfsFile.getNfs());
        }
        return "";
    }

    /**
     * 重命名文件夹
     *
     * @param sourcePath 原文件夹
     * @param targetPath 目标文件夹
     * @return
     */
    public void renameDir(String sourcePath, String targetPath) {
        Nfs3File sourceNfsFile = getNfs3File(formatPath(sourcePath));
        Nfs3File targetNfsFile = getNfs3File(formatPath(targetPath));
        try {
            if (!sourceNfsFile.exists()) {
                LogUtil.error(LogEnum.NFS_UTIL, "sourcePath不存在, 如下{} ", sourcePath);
            }
            sourceNfsFile.rename(targetNfsFile);
        } catch (IOException e) {
            LogUtil.error(LogEnum.NFS_UTIL, "renameDir失败, sourcePath为{}, targetPath为{}, 失败原因{}", sourcePath, targetPath, e);
        } finally {
            nfsPool.revertNfs(sourceNfsFile.getNfs());
            nfsPool.revertNfs(targetNfsFile.getNfs());
        }
    }


    /**
     * 替换路劲中多余的 "/"
     *
     * @param path 文件路径
     * @return String
     */
    public String formatPath(String path) {
        if (!StringUtils.isEmpty(path)) {
            return path.replaceAll("///*", FILE_SEPARATOR);
        }
        return path;
    }

    public String getAbsolutePath(String relativePath) {
        return nfsConfig.getRootDir() + nfsConfig.getBucket() + relativePath;
    }

}
