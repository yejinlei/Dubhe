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

package org.dubhe.auth.exception;

import org.apache.commons.lang3.StringUtils;
import org.dubhe.biz.base.exception.FeignException;
import org.dubhe.cloud.authconfig.dto.JwtUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @description 抛出自定义中文错误信息
 * @date 2020-12-21
 */
@Service
public class AuthenticationProviderImpl implements AuthenticationProvider {
    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        JwtUserDTO userDTO;
        try {
            userDTO = (JwtUserDTO) userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("Load balancer does not have available server for client")) {
                throw new BadCredentialsException("用户服务不可用！", e);
            } else if (e instanceof FeignException) {
                throw new BadCredentialsException("用户服务" + ((FeignException) e).getResponseBody().getMsg(), e);
            } else {
                throw new BadCredentialsException("用户信息不存在！", e);
            }
        }
        if (!Objects.isNull(userDTO.getUser()) && !userDTO.getUser().getEnabled()) {
            throw new BadCredentialsException("帐号已锁定！");
        }
        if (!new BCryptPasswordEncoder().matches(password, userDTO.getPassword())) {
            throw new BadCredentialsException("密码错误！");
        }
        return new UsernamePasswordAuthenticationToken(userDTO, userDTO.getPassword(), null);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.equals(aClass);
    }
}