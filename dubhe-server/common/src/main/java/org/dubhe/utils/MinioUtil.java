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

package org.dubhe.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param bucket       桶
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     * @return String
     */
    public String readString(String bucket, String fullFilePath) throws Exception {
        try (InputStream is = client.getObject(bucket, fullFilePath)) {
            return IoUtil.read(is, Charset.defaultCharset());
        }
    }

    /**
     * 文件删除
     *
     * @param bucket       桶
     * @param fullFilePath 文件存储的全路径，包括文件名，非'/'开头. e.g. dataset/12/annotation/test.txt
     */
    public void del(String bucket, String fullFilePath) throws Exception {
        Iterable<Result<Item>> items = client.listObjects(bucket, fullFilePath);
        Set<String> files = new HashSet<>();
        for (Result<Item> item : items) {
            files.add(item.get().objectName());
        }
        Iterable<Result<DeleteError>> results = client.removeObjects(bucket, files);
        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    /**
     * 批量删除文件
     *
     * @param bucket       桶
     * @param objectNames  对象名称
     */
    public void delFiles(String bucket,List<String> objectNames) throws Exception{
        Iterable<Result<DeleteError>> results = client.removeObjects(bucket, objectNames);
        for (Result<DeleteError> result : results) {
            result.get();
        }
    }

    /**
     * 获取对象名称
     *
     * @param bucketName  桶名称
     * @param prefix      前缀
     * @return
     * @throws Exception
     */
    public List<String> getObjects(String bucketName, String prefix)throws Exception{
        List<String> fileNames = new ArrayList<>();
        Iterable<Result<Item>> results = client.listObjects(bucketName, prefix);
        for(Result<Item> result:results){
            Item item = result.get();
            fileNames.add(item.objectName());
        }
        return fileNames;
    }

    /**
     * 获取文件流
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return
     * @throws Exception
     */
    public InputStream getObjectInputStream(String bucket,String objectName)throws Exception{
        return client.getObject(bucket, objectName);
    }

    /**
     * 文件夹复制
     *
     * @param bucket      桶
     * @param sourceFiles 源文件
     * @param targetDir   目标文件夹
     */
    public void copyDir(String bucket, List<String> sourceFiles, String targetDir) {
        sourceFiles.forEach(sourceFile -> {
            InputStream inputStream = null;
            try {
                String sourceObjectName = sourceFile;
                String targetObjectName = targetDir + "/" + StringUtils.substringAfterLast(sourceObjectName, "/");
                inputStream = client.getObject(bucket, sourceObjectName);
                byte[] buf = new byte[512];
                int bytesRead;
                int count = MagicNumConstant.ZERO;
                while ((bytesRead = inputStream.read(buf, MagicNumConstant.ZERO, buf.length)) >= MagicNumConstant.ZERO) {
                    count += bytesRead;
                }
                PutObjectOptions options = new PutObjectOptions(count, MagicNumConstant.ZERO);
                client.putObject(bucket, targetObjectName, client.getObject(bucket, sourceObjectName), options);
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

    @Data
    @Service
    public class MinioWebTokenBody {

        @Value("${minioweb.GetToken.url}")
        private String tokenUrl;
        @Value("${minioweb.GetToken.param.id}")
        private int id;
        @Value("${minioweb.GetToken.param.jsonrpc}")
        private String jsonrpc;
        @Value("${minioweb.GetToken.param.method}")
        private String method;
        @Value("${minioweb.zip.url}")
        private String zipUrl;

        /**
         * 生成文件下载请求参数方法
         *
         * @param bucketName  桶名称
         * @param prefix      前缀
         * @param objects     对象名称
         * @return MinioDownloadDto 下载请求参数
         */
        public MinioDownloadDto getDownloadParam(String bucketName, String prefix, List<String> objects, String zipName) {
            String paramTemplate = "{\"id\":%d,\"jsonrpc\":\"%s\",\"params\":{\"username\":\"%s\",\"password\":\"%s\"},\"method\":\"%s\"}";
            String downloadBodyTemplate = "{\"bucketName\":\"%s\",\"prefix\":\"%s\",\"objects\":[%s]}";
            String param = String.format(paramTemplate, id, jsonrpc, accessKey, secretKey, method);
            String result = HttpRequest.post(url + tokenUrl).contentType("application/json").body(param).execute().body();
            String token = JSONObject.parseObject(result).getJSONObject("result").getString("token");
            return new MinioDownloadDto(token, String.format(downloadBodyTemplate, bucketName, prefix, getStrFromList(objects)), zipName);
        }

        public String getStrFromList(List<String> objects) {
            List<String> result = new ArrayList<>();
            objects.stream().forEach(s -> {
                result.add("\"" + s + "\"");
            });
            return StringUtils.join(result, ",");
        }

    }

    @ApiModel
    @Data
    public class MinioDownloadDto {
        @ApiModelProperty("下载压缩包请求token")
        private String token;
        @ApiModelProperty("下载压缩包请求参数")
        private String body;
        @ApiModelProperty("下载压缩包请求需要的header")
        private Map<String, Object> headers;
        @ApiModelProperty("下载压缩包文件名称")
        private String zipName;

        public MinioDownloadDto() {
        }

        public MinioDownloadDto(String token, String body, String zipName) {
            this.token = token;
            this.body = body;
            this.zipName = zipName;
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "text/plain;charset=UTF-8");
            this.headers = headers;
        }

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

}
