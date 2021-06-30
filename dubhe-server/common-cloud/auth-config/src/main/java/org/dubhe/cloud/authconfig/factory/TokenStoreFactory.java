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

import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * @description TokenStore工厂类
 * @date 2020-11-24
 */
public class TokenStoreFactory {

    private TokenStoreFactory(){

    }

    /**
     * 获取token存储
     * @param dataSource   数据库数据源
     * @return
     */
    public static JdbcTokenStore getJdbcTokenStore(DataSource dataSource){
        return new JdbcTokenStore(dataSource);
    }
}
