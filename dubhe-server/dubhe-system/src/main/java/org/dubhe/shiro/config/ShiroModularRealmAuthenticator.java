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

package org.dubhe.shiro.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.dubhe.enums.LogEnum;
import org.dubhe.utils.LogUtil;

import java.util.Collection;


/**
 * @Description : Shiro 扩展父类原方法，捕获原始异常
 * @Date 2020-06-01
 */
public class ShiroModularRealmAuthenticator extends ModularRealmAuthenticator {
    /**
     * 扩展父类原方法，对异常立即进行外抛
     *
     * @param realms
     * @param token
     * @return
     */
    @Override
    protected AuthenticationInfo doMultiRealmAuthentication(Collection<Realm> realms,
                                                            AuthenticationToken token) throws AuthenticationException {
        AuthenticationStrategy strategy = getAuthenticationStrategy();

        AuthenticationInfo aggregate = strategy.beforeAllAttempts(realms, token);
        AuthenticationException authenticationException = null;
        for (Realm realm : realms) {

            aggregate = strategy.beforeAttempt(realm, token, aggregate);

            if (realm.supports(token)) {

                AuthenticationInfo info = null;
                try {
                    info = realm.getAuthenticationInfo(token);
                } catch (AuthenticationException e) {
                    authenticationException = e;
                    LogUtil.error(LogEnum.SYS_ERR, "doMultiRealmAuthentication error :{} .", e);
                }

                aggregate = strategy.afterAttempt(realm, token, info, aggregate, authenticationException);

            } else {
                LogUtil.info(LogEnum.SYS_ERR, "Realm [{}] does not support token {}.  Skipping realm.", realm, token);
            }
        }
        //增加此逻辑，只有authenticationException不为null，则表示有Realm较验到了异常，
        if (authenticationException != null) {
            throw authenticationException;
        }
        aggregate = strategy.afterAllAttempts(token, aggregate);

        return aggregate;
    }
}

