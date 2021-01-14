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

package org.dubhe.dto;

import lombok.Data;

/**
 * @description 全局请求日志信息
 * @date 2020-08-13
 */
@Data
public class GlobalRequestRecordDTO {
    /**
     * 客户主机地址
     */
    private String clientHost;
    /**
     * 请求地址
     */
    private String uri;
    /**
     * 授权信息
     */
    private String authorization;
    /**
     * 用户名
     */
    private String username;
    /**
     * form参数
     */
    private String params;
    /**
     * 返回值类型
     */
    private String contentType;
    /**
     * 返回状态
     */
    private Integer status;
    /**
     * 时间耗费
     */
    private Long timeCost;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求体body参数
     */
    private String requestBody;
    /**
     * 返回值json数据
     */
    private String responseBody;

}
