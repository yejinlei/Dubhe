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
package org.dubhe.datasetutil.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import io.minio.*;
import org.dubhe.datasetutil.common.base.MagicNumConstant;
import org.dubhe.datasetutil.common.config.MinioConfig;
import org.dubhe.datasetutil.common.constant.BusinessConstant;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @description Minio工具类
 * @date 2020-09-17
 */
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传文件
     *
     * @param sourceFilePath 原文件绝对路径
     * @param targetFilePath 目标文件路径
     * @throws Exception 上传异常
     */
    public void upLoadFile(String sourceFilePath, String targetFilePath) throws Exception {
        LogUtil.info(LogEnum.BIZ_DATASET, "源文件目录： 【" + sourceFilePath + "】" + " 目标目录: 【" + targetFilePath + "】");
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(minioConfig.getBucketName())
                    .object(targetFilePath)
                    .filename(sourceFilePath)
                    .contentType(FileUtil.getName(sourceFilePath))
                    .build()
            );
        } catch (IOException e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "上传文件失败: {} ", e);
        }
    }


    /**
     * 上传文件 (文件流消费后直接关闭)
     *
     * @param targetFilePath 原文件绝对路径
     * @throws Exception 上传异常
     */
    public void upLoadFileByInputStream(String targetFilePath, String filePath) throws Exception {
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(minioConfig.getBucketName())
                    .object(targetFilePath)
                    .filename(filePath)
                    .contentType(
                            contentType(
                                    FileUtil.getName(filePath)
                            )
                    ).build()
            );
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "上传文件失败: {} ", e);
        }
    }

    /**
     * 获取文件URL
     *
     * @param objectName 对象名称
     * @return String 文件路径
     */
    public String getUrl(String objectName) {
        return minioConfig.getBucketName() + BusinessConstant.FILE_SEPARATOR + objectName;
    }

    /**
     * 读取文件
     *
     * @param bucketName       桶
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     * @return String
     */
    public String readString(String bucketName, String fullFilePath) {
        try (InputStream is = minioClient.getObject(GetObjectArgs
                .builder()
                .bucket(bucketName)
                .object(fullFilePath)
                .build()
        )) {
            return IoUtil.read(is, Charset.defaultCharset());
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, "读取文本content失败: {} ", e);
            return null;
        }
    }

    private String contentType(String fileName) {
        if (fileName.endsWith("xml")) {
            return "text/xml";
        } else if (fileName.endsWith("jpg") || fileName.endsWith("jpe") || fileName.endsWith("jpeg")) {
            return "image/jpg";
        } else if (fileName.endsWith("png")) {
            return "image/png";
        } else if (fileName.endsWith("pic")) {
            return "image/pict";
        } else if (fileName.endsWith("avi")) {
            return "video/x-msvideo";
        } else if (fileName.endsWith("mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith("ogg")) {
            return "video/ogg";
        } else if (fileName.endsWith("webm")) {
            return "video/webm";
        } else if (fileName.endsWith("HTML") || fileName.endsWith("html")) {
            return "text/html";
        } else if (fileName.endsWith("DOCX") || fileName.endsWith("docx") || fileName.endsWith("DOC")
                || fileName.endsWith("doc")) {
            return "application/msword";
        } else if (fileName.endsWith("XML") || fileName.endsWith("xml")) {
            return "text/xml";
        } else if (fileName.endsWith("pdf")) {
            return "application/pdf";
        }
        return "image/jpeg";
    }

}
