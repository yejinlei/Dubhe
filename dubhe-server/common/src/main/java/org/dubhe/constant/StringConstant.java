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

package org.dubhe.constant;

/**
 * @description 字符串constant
 * @date 2020-05-14
 */
public final class StringConstant {

    public static final String MSIE = "MSIE";
    public static final String MOZILLA = "Mozilla";
    public static final String REQUEST_METHOD_GET = "GET";

    /**
     * 公共字段
     */
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String UPDATE_USER_ID = "updateUserId";
    public static final String CREATE_USER_ID = "createUserId";
    public static final String ORIGIN_USER_ID = "originUserId";
    public static final String DELETED = "deleted";
    public static final String UTF8 = "utf-8";
    public static final String JSON_REQUEST = "application/json";
    public static final String K8S_CALLBACK_URI = "/api/k8s/callback/pod";
    public static final String MULTIPART = "multipart/form-data";

    /**
     * 测试环境
     */
    public static final String PROFILE_ACTIVE_TEST = "test";

    private StringConstant() {
    }
}
