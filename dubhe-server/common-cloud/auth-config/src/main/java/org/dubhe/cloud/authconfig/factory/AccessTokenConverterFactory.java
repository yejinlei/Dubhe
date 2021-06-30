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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * @description AccessTokenConverter 工厂类
 * @date 2020-11-24
 */
public class AccessTokenConverterFactory {

    private AccessTokenConverterFactory(){}


    /**
     * 获取JwtAccessTokenConverter
     * @param userDetailsService  自定义实现的用户信息获取Service
     * @return jwtAccessTokenConverter
     */
    public static JwtAccessTokenConverter getAccessTokenConverter(UserDetailsService userDetailsService){
        DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
        userAuthenticationConverter.setUserDetailsService(userDetailsService);

        DefaultAccessTokenConverter converter = new DefaultAccessTokenConverter();
        converter.setUserTokenConverter(userAuthenticationConverter);

        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(AuthConst.CLIENT_SECRET);
        jwtAccessTokenConverter.setAccessTokenConverter(converter);
        return jwtAccessTokenConverter;
    }
}
