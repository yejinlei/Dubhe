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
package org.dubhe.cloud.authconfig.utils;

import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.dto.JwtUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @description JWT
 * @date 2020-11-25
 */
public class JwtUtils {

    private JwtUtils(){

    }

    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    public static JwtUserDTO getCurUser(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null){
                return null;
            }
            if (authentication.getPrincipal() instanceof JwtUserDTO){
                return (JwtUserDTO) authentication.getPrincipal();
            }
        }catch (Exception e){
            LogUtil.error(LogEnum.SYS_ERR,"Jwt getCurUser error!{}",e);
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * 若用户不存在，则返回null（可根据业务统一修改）
     * @return 当前用户ID
     */
    public static Long getCurUserId(){
        JwtUserDTO jwtUserDTO = getCurUser();
        return jwtUserDTO == null?null:jwtUserDTO.getCurUserId();
    }
}
