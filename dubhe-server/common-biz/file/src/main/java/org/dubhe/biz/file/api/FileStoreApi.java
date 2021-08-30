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

package org.dubhe.biz.file.api;

import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.dto.FilePageDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;

/**
 * @description 文件存储接口
 * @date 2021-04-19
 */
public interface FileStoreApi {

    /**
     * 获取根路径
     *
     * @return String 根路径 默认为空
     */
    default String getRootDir() {
        return "";
    }

    /**
     * 获取bucket
     *
     * @return String bucket 默认为空
     */
    default String getBucket() {
        return "";
    }

    /**
     * 替换路径中多余的 "/"
     *
     * @param path
     * @return String
     */
    default String formatPath(String path) {
        if (!StringUtils.isEmpty(path)) {
            return path.replaceAll("///*", "/");
        }
        return path;
    }

    /**
     *  绝对路径兼容
     * @param sourcePath 源路径
     * @return String
     */
    default String compatibleAbsolutePath(String sourcePath) {
        return formatPath(sourcePath.startsWith(getRootDir()) ? sourcePath : getRootDir() + sourcePath);
    }


    /**
     * 校验文件或文件夹是否不存在（true为存在，false为不存在）
     *
     * @param path 文件路径
     * @return boolean
     */
    boolean fileOrDirIsExist(String path);

    /**
     * 校验是否是文件夹（true为文件夹，false为文件）
     *
     * @param path 文件路径
     * @return boolean
     */
    boolean isDirectory(String path);

    /**
     * 过滤路径下文件后缀名(不区分大小写)并返回符合条件的文件集合
     *
     * @param path 文件夹路径
     * @param fileSuffix 文件后缀名(不区分大小写)
     * @return List<String> 返回符合条件的文件集合
     */
    List<String> filterFileSuffix(String path,String fileSuffix);

    /**
     * 创建指定目录
     *
     * @param dir 需要创建的目录 例如：/abc/def
     * @return boolean
     */
    boolean createDir(String dir);

    /**
     * 创建多个指定目录
     *
     * @param paths
     * @return boolean
     */
    boolean createDirs(String... paths);

    /**
     * 指定目录中创建文件
     *
     * @param dir      需要创建的目录 例如：/abc/def
     * @param fileName 需要创建的文件 例如：dd.txt
     * @return boolean
     */
    boolean createFile(String dir, String fileName);

    /**
     * 新建或追加文件
     *
     * @param filePath  文件绝对路径
     * @param content   文件内容
     * @param append    文件是否是追加
     * @return
     */
    boolean createOrAppendFile(String filePath, String content, boolean append);

    /**
     * 删除目录 或者文件
     *
     * @param dirOrFile 需要删除的目录 或者文件 例如：/abc/def  或者 /abc/def/dd.txt
     * @return boolean
     */
    boolean deleteDirOrFile(String dirOrFile);

    /**
     * 复制文件 到指定目录下  单个文件
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/dd.txt
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    boolean copyFile(String sourceFile, String targetPath);

    /**
     * 使用shell拷贝文件或路径
     *
     * @param sourcePath 需要复制的文件或路径  例如：/abc/def/cc.txt or /abc/def*
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @param type CopyTypeEnum的
     * @return boolean
     */
    boolean copyFile(String sourcePath, String targetPath, Integer type);

    /**
     * 复制路径下文件并重命名
     *
     * @param sourceFile 源文件
     * @param targetFile 目标文件全路径
     * @return boolean
     */
    boolean copyFileAndRename(String sourceFile, String targetFile);

    /**
     * 复制路径下文件及文件夹到指定目录下  多个文件  包含目录与文件并存情况
     *
     * @param sourcePath 需要复制的文件目录  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd
     * @return boolean
     */
    boolean copyPath(String sourcePath, String targetPath);

    /**
     * 复制文件夹到指定目录下
     *
     * @param sourcePath 需要复制的文件夹  例如：/abc/def
     * @param targetPath 需要放置的目标目录 例如：/abc/dd 复制成功路径/abc/dd/def*
     * @return boolean
     */
    boolean copyDir(String sourcePath, String targetPath);

    /**
     * zip解压并删除压缩文件
     * @param sourceFile zip源文件 例如：/abc/z.zip
     * @param targetPath 解压后的目标文件夹 例如：/abc/
     * @return boolean
     */
    boolean unzip(String sourceFile, String targetPath);

    /**
     * 解压压缩包 包含目录与子目录
     *
     * @param sourceFile 需要复制的文件  例如：/abc/def/aaa.zip
     * @return boolean
     */
    boolean unzip(String sourceFile);

    /**
     * 压缩 目录 或者文件 到压缩包
     *
     * @param dirOrFile 目录或者文件  例如： /abc/def/aaa.txt , /abc/def
     * @param zipPath   压缩包全路径名 例如： /abc/def/aa.zip
     * @return boolean
     */
    boolean zipDirOrFile(String dirOrFile, String zipPath);

    /**
     * 获取NFS3File 对象文件的输入流
     *
     * @param path 文件路径
     * @return BufferedInputStream
     */
    BufferedInputStream getInputStream(String path);

    /**
     * 下载
     *
     * @param path 文件路径
     * @param response HTTP返回
     */
    void download(String path, HttpServletResponse response);

    /**
     * 分页查询指定路径下的文件列表(需要支持分页)
     *
     * @param filePageDTO 查询以及相应参数实体
     */
    void filterFilePageWithPath(FilePageDTO filePageDTO);

}
