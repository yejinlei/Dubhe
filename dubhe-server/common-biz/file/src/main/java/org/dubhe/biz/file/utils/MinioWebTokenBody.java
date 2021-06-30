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

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.dubhe.biz.file.dto.MinioDownloadDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description Minio web访问token实体
 * @date 2020-05-09
 */
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
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.url}")
    private String url;

    /**
     * 生成文件下载请求参数方法
     *
     * @param bucketName 桶名称
     * @param prefix     前缀
     * @param objects    对象名称
     * @return MinioDownloadDto 下载请求参数
     */
    public MinioDownloadDTO getDownloadParam(String bucketName, String prefix, List<String> objects, String zipName) {
        String paramTemplate = "{\"id\":%d,\"jsonrpc\":\"%s\",\"params\":{\"username\":\"%s\",\"password\":\"%s\"},\"method\":\"%s\"}";
        String downloadBodyTemplate = "{\"bucketName\":\"%s\",\"prefix\":\"%s\",\"objects\":[%s]}";
        String param = String.format(paramTemplate, id, jsonrpc, accessKey, secretKey, method);
        String result = HttpRequest.post(url + tokenUrl).contentType("application/json").body(param).execute().body();
        String token = JSONObject.parseObject(result).getJSONObject("result").getString("token");
        return new MinioDownloadDTO(token, String.format(downloadBodyTemplate, bucketName, prefix, getStrFromList(objects)), zipName);
    }

    public String getStrFromList(List<String> objects) {
        List<String> result = new ArrayList<>();
        objects.stream().forEach(s -> {
            result.add("\"" + s + "\"");
        });
        return StringUtils.join(result, ",");
    }

}