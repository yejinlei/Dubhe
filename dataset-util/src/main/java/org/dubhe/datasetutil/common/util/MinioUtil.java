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

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.dubhe.datasetutil.common.config.MinioConfig;
import org.dubhe.datasetutil.common.enums.LogEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

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
     * @param objectName 对象名称
     * @param inputStream 文件流
     * @throws Exception 上传异常
     */
    public void upLoadFile(String objectName, InputStream inputStream) throws Exception {
        LogUtil.info(LogEnum.BIZ_DATASET,"文件上传名称为: 【" + objectName + "】");
        PutObjectOptions options = new PutObjectOptions(inputStream.available(), -1);
        minioClient.putObject(minioConfig.getBucketName(), objectName, inputStream, options);
    }

    /**
     * 获取文件URL
     * 
     * @param objectName 对象名称
     * @return String 文件路径
     */
    public String getUrl(String objectName) {
        return minioConfig.getBucketName() + "/" + objectName;
    }

}
