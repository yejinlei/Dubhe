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
package org.dubhe.biz.base.constant;

/**
 * @description 授权常量类
 * @date 2020-11-05
 */
public class AuthConst {
    private AuthConst() {

    }

    /**
     * 授权token名称 Header
     */
    public final static String AUTHORIZATION = "Authorization";
    /**
     * 授权token名称 Params
     */
    public final static String ACCESS_TOKEN = "access_token";
    /**
     * token前缀
     */
    public final static String ACCESS_TOKEN_PREFIX = "Bearer ";
    /**
     * 客户端安全码
     * $2a$10$RUYBRsyV2jpG7pvg/VNus.YHVebzfRen3RGeDe1LVEIJeHYe2F1YK
     */
    public final static String CLIENT_SECRET = "dubhe-secret";
    /**
     * 客户端安全码
     */
    public final static String CLIENT_ID = "dubhe-client";
    /**
     * 授权中心token校验地址
     */
    public final static String CHECK_TOKEN_ENDPOINT_URL = "http://" + ApplicationNameConst.SERVER_AUTHORIZATION + "/oauth/check_token";
    /**
     * 默认匿名访问路径
     */
    public final static String[] DEFAULT_PERMIT_PATHS = {"/swagger**/**", "/webjars/**", "/v2/api-docs/**", "/doc.html/**",
            "/users/findUserByUsername", "/auth/login", "/auth/code",
            "/datasets/files/annotations/auto","/datasets/versions/**/convert/finish", "/datasets/enhance/finish",
            "/auth/getCodeBySentEmail","/auth/userRegister","/ws/**",
            StringConstant.RECYCLE_CALL_URI+"**"
    };

    /**
     * k8s 回调token key
     */
    public static final String K8S_CALLBACK_TOKEN = "k8sCallbackToken";

    /**
     * 通用授权token key
     */
    public static final String COMMON_TOKEN = "commonToken";

    /**
     * 授权模式
     */
    public static final String GRANT_TYPE = "password";
}
