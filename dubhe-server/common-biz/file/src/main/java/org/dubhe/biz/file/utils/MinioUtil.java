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

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSONObject;
import io.minio.CopyConditions;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.apache.commons.lang.StringUtils;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.file.dto.FileDTO;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description Minio工具类
 * @date 2020-05-09
 */
@Service
public class MinioUtil {

    @Value("${minio.url}")
    private String url;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    private MinioClient client;

    @PostConstruct
    public void init() {
        try {
            client = new MinioClient(url, accessKey, secretKey);
        } catch (InvalidEndpointException e) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "MinIO endpoint invalid. e, {}", e);
        } catch (InvalidPortException e) {
            LogUtil.warn(LogEnum.BIZ_DATASET, "MinIO endpoint port invalid. e, {}", e);
        }
    }

    /**
     * 写文件
     *
     * @param bucket       桶名称
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     * @param content      file content. can not be null
     */
    public void writeString(String bucket, String fullFilePath, String content) throws Exception {
        boolean isExist = client.bucketExists(bucket);
        if (!isExist) {
            client.makeBucket(bucket);
        }
        InputStream inputStream = IoUtil.toUtf8Stream(content);
        PutObjectOptions options = new PutObjectOptions(inputStream.available(), MagicNumConstant.NEGATIVE_ONE);
        client.putObject(bucket, fullFilePath, inputStream, options);
    }

    /**
     * 读取文件
     *
     * @param bucketName       桶
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     * @return String
     */
    public String readString(String bucketName, String fullFilePath) throws Exception {
        try (InputStream is = client.getObject(bucketName, fullFilePath)) {
            return IoUtil.read(is, Charset.defaultCharset());
        }
    }

    /**
     * 文件删除
     *
     * @param bucketName       桶
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     */
    public void del(String bucketName, String fullFilePath) throws Exception {
        Iterable<Result<Item>> items = client.listObjects(bucketName, fullFilePath);
        Set<String> files = new HashSet<>();
        for (Result<Item> item : items) {
            files.add(item.get().objectName());
        }
        Iterable<Result<DeleteError>> results = client.removeObjects(bucketName, files);
        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    /**
     * 批量删除文件
     *
     * @param bucketName      桶
     * @param objectNames 对象名称
     */
    public void delFiles(String bucketName, List<String> objectNames) throws Exception {
        Iterable<Result<DeleteError>> results = client.removeObjects(bucketName, objectNames);
        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    /**
     * 获取对象名称
     *
     * @param bucketName 桶名称
     * @param prefix     前缀
     * @return List<String> 对象名称列表
     * @throws Exception
     */
    public List<String> getObjects(String bucketName, String prefix) throws Exception {
        List<String> fileNames = new ArrayList<>();
        Iterable<Result<Item>> results = client.listObjects(bucketName, prefix);
        for (Result<Item> result : results) {
            Item item = result.get();
            fileNames.add(item.objectName());
        }
        return fileNames;
    }

    /**
     * 获取路径下文件数量
     *
     * @param bucketName 桶名称
     * @param prefix     前缀
     * @return InputStream 文件流
     * @throws Exception
     */
    public int getCount(String bucketName, String prefix) throws Exception {
        int count = NumberConstant.NUMBER_0;
        Iterable<Result<Item>> results = client.listObjects(bucketName, prefix);
        for (Result<Item> result : results) {
            count++;
        }
        return count;
    }

    /**
     * 获取文件流
     *
     * @param bucketName     桶
     * @param objectName 对象名称
     * @return InputStream 文件流
     * @throws Exception
     */
    public InputStream getObjectInputStream(String bucketName, String objectName) throws Exception {
        return client.getObject(bucketName, objectName);
    }

    /**
     * 文件夹复制
     *
     * @param bucketName      桶
     * @param sourceFiles 源文件
     * @param targetDir   目标文件夹
     */
    public void copyDir(String bucketName, List<String> sourceFiles, String targetDir) {
        sourceFiles.forEach(sourceFile -> {
            InputStream inputStream = null;
            try {
                String sourceObjectName = sourceFile;
                String targetObjectName = targetDir + "/" + StringUtils.substringAfterLast(sourceObjectName, "/");
                inputStream = client.getObject(bucketName, sourceObjectName);
                byte[] buf = new byte[512];
                int bytesRead;
                int count = MagicNumConstant.ZERO;
                while ((bytesRead = inputStream.read(buf, MagicNumConstant.ZERO, buf.length)) >= MagicNumConstant.ZERO) {
                    count += bytesRead;
                }
                PutObjectOptions options = new PutObjectOptions(count, MagicNumConstant.ZERO);
                client.putObject(bucketName, targetObjectName, client.getObject(bucketName, sourceObjectName), options);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "MinIO file copy exception, {}", e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LogUtil.error(LogEnum.BIZ_DATASET, "MinIO file read stream closed failed, {}", e);
                }
            }
        });
    }

    /**
     * minio拷贝操作
     *
     * @param bucketName        桶名
     * @param sourceFiles   需要复制的标注文件名
     * @param targetDir     目标文件夹路径
     */
    public void copyObject(String bucketName, List<String> sourceFiles, String targetDir) {
        CopyConditions copyConditions = new CopyConditions();
        sourceFiles.forEach(sourceFile -> {
            try {
                String targetName = targetDir + "/" + StringUtils.substringAfterLast(sourceFile, "/");
                client.copyObject(bucketName, targetName, null, null, bucketName, sourceFile, null, copyConditions);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "MinIO file copy failed, {}", e);
            }
        });
    }

    /**
     * 获取文件列表
     *
     * @param bucketName 桶
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return List<FileDTO> 文件列表
     */
    public List<FileDTO> fileList(String bucketName, String prefix, boolean recursive) {
        List<FileDTO> result = new ArrayList<>();
        Iterable<Result<Item>> items = client.listObjects(bucketName, prefix, false);
        for (Result<Item> resultItem : items) {
            try {
                Item item = resultItem.get();
                FileDTO fileDto = FileDTO.builder().dir(item.isDir()).size(item.size()).path(item.objectName())
                        .name(item.objectName().substring(item.objectName().lastIndexOf("/") + 1, item.objectName().length()))
                        .build();
                if(!item.isDir()) {
                    fileDto.setLastModified(Date.from(item.lastModified().toInstant()));
                }
                result.add(fileDto);
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, "get file list error {}", e);
            }
        }
        return result;
    }

    /**
     * 生成一个给HTTP PUT请求用的presigned URL。浏览器/移动端的客户端可以用这个URL进行上传，
     * 即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return String
     */
    public String getEncryptedPutUrl(String bucketName, String objectName, Integer expires) {
        if (StringUtils.isEmpty(objectName)) {
            throw new BusinessException("object name cannot be empty");
        }
        try {
            return client.presignedPutObject(bucketName, objectName, expires);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_DATASET, e.getMessage());
            throw new BusinessException("MinIO an error occurred, please contact the administrator");
        }
    }

    /**
     * 生成给HTTP PUT请求用的presigned URLs。浏览器/移动端的客户端可以用这个URL进行上传，
     * 即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天
     *
     * @param bucketName  存储桶名称
     * @param objectNames 存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return String
     */
    public JSONObject getEncryptedPutUrls(String bucketName,String objectNames, Integer expires) {
        List<String> filePaths = JSONObject.parseObject(objectNames, List.class);
        List<String> urls = new ArrayList<>();
        filePaths.stream().forEach(filePath->{
            if (StringUtils.isEmpty(filePath)) {
                throw new BusinessException("filePath cannot be empty");
            }
            try {
                urls.add(client.presignedPutObject(bucketName, filePath, expires));
            } catch (Exception e) {
                LogUtil.error(LogEnum.BIZ_DATASET, e.getMessage());
                throw new BusinessException("MinIO an error occurred, please contact the administrator");
            }
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("preUrls",urls);
        jsonObject.put("bucketName", bucketName);
        return jsonObject;
    }

}
