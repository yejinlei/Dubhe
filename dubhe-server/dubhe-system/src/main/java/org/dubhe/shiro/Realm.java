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

package org.dubhe.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.dubhe.domain.dto.UserDTO;
import org.dubhe.enums.LogEnum;
import org.dubhe.exception.BusinessException;
import org.dubhe.service.UserService;
import org.dubhe.support.login.UsernamePasswordCaptchaToken;
import org.dubhe.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Description : 权限检查类
 * @Date 2020-06-01
 */
@Component
@Slf4j
public class Realm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordCaptchaToken;
    }

    /**
     * 权限
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        UserDTO user = (UserDTO) principals.getPrimaryPrincipal();
        Set<String> list = userService.queryPermissionByUserId(user.getId());
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(list);
        return info;
    }


    /**
     * 登录验证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
            throws AuthenticationException {
        UsernamePasswordCaptchaToken token = (UsernamePasswordCaptchaToken) authcToken;
        String password = new String(token.getPassword());
        String username = token.getUsername();
        UserDTO userDto = userService.findByName(username);


        if (userDto == null) {
            return null;
        }
        try {
            if (!passwordEncoder.matches(password, userDto.getPassword())) {
                return null;
            }
            AuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(userDto, password, "");
            return simpleAuthenticationInfo;
        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "Realm doGetAuthenticationInfo error:{}", e);
            throw new BusinessException(e.getMessage());
        }

    }


}

