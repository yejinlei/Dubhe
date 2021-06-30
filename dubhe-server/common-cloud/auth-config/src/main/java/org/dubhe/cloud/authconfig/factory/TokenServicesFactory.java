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
package org.dubhe.cloud.authconfig.factory;

import org.dubhe.biz.base.constant.AuthConst;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.web.client.RestTemplate;

/**
 * @description TokenServices 工厂类
 * @date 2020-11-25
 */
public class TokenServicesFactory {

    private TokenServicesFactory(){

    }

    /**
     * 获取 RemoteTokenServices
     * @param accessTokenConverter token转换器
     * @param restTemplate  rest请求模板
     * @return RemoteTokenServices
     */
    public static RemoteTokenServices getTokenServices(JwtAccessTokenConverter accessTokenConverter, RestTemplate restTemplate){
        RemoteTokenServices tokenServices = new RemoteTokenServices();
        if (accessTokenConverter != null){
            tokenServices.setAccessTokenConverter(accessTokenConverter);
        }
        // 配置异常处理器
//        restTemplate.setErrorHandler(new OAuth2ResponseErrorHandler());
        tokenServices.setRestTemplate(restTemplate);
        tokenServices.setCheckTokenEndpointUrl(AuthConst.CHECK_TOKEN_ENDPOINT_URL);
        tokenServices.setClientId(AuthConst.CLIENT_ID);
        tokenServices.setClientSecret(AuthConst.CLIENT_SECRET);
        return tokenServices;
    }

}
