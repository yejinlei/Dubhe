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
package org.dubhe.cloud.authconfig.service.impl;

import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.cloud.authconfig.dto.JwtUserDTO;
import org.dubhe.cloud.authconfig.utils.JwtUtils;
import org.springframework.stereotype.Service;

/**
 * @description OAuth2 当前信息获取实现类
 * @date 2020-12-07
 */
@Service(value = "oAuth2UserContextServiceImpl")
public class OAuth2UserContextServiceImpl implements UserContextService {

    @Override
    public UserContext getCurUser() {
        JwtUserDTO jwtUserDTO = JwtUtils.getCurUser();
        return jwtUserDTO == null ? null : jwtUserDTO.getUser();
    }

    @Override
    public Long getCurUserId() {
        UserContext userContext = getCurUser();
        return userContext == null ? null : userContext.getId();
    }
}
