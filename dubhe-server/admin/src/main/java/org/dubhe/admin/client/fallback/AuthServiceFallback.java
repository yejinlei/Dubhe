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
package org.dubhe.admin.client.fallback;

import org.dubhe.admin.client.AuthServiceClient;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description Feign 熔断处理类
 * @date 2020-11-04
 */
@Component
public class AuthServiceFallback implements AuthServiceClient {


    /**
     * 获取token
     *
     * @param parameters 获取token请求map
     * @return token 信息
     */
    @Override
    public DataResponseBody postAccessToken(Map<String, String> parameters) {
        return DataResponseFactory.failed("call auth server postAccessToken error ");
    }

    /**
     * 退出登录
     *
     * @param accessToken token
     * @return
     */
    @Override
    public DataResponseBody logout(String accessToken) {
        return DataResponseFactory.failed("call auth server logout error ");
    }
}
