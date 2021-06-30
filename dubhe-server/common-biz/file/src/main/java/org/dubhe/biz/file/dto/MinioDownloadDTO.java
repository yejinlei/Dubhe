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

package org.dubhe.biz.file.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @description minio下载参数实体
 * @date 2021-06-24
 */
@Data
public class MinioDownloadDTO {
    /**
     * 下载压缩包请求token
     */
    private String token;
    /**
     * 下载压缩包请求参数
     */
    private String body;
    /**
     * 下载压缩包请求需要的header
     */
    private Map<String, Object> headers;
    /**
     * 下载压缩包文件名称
     */
    private String zipName;

    public MinioDownloadDTO() {
    }

    public MinioDownloadDTO(String token, String body, String zipName) {
        this.token = token;
        this.body = body;
        this.zipName = zipName;
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain;charset=UTF-8");
        this.headers = headers;
    }

}
