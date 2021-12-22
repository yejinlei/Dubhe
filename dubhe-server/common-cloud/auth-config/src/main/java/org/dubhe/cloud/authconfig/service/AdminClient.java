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
package org.dubhe.cloud.authconfig.service;

import feign.Param;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.UserConfigSaveDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotResourceVO;
import org.dubhe.biz.base.vo.UserConfigVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description admin远程服务调用类
 * @date 2020-12-10
 */
@FeignClient(value = ApplicationNameConst.SERVER_ADMIN,fallback = AdminClientFallback.class)
public interface AdminClient {

    /**
     * 根据用户名称获取用户信息
     *
     * @param username 用户名称
     * @return  用户信息
     */
    @GetMapping(value = "/users/findUserByUsername")
    DataResponseBody<UserContext> findUserByUsername(@RequestParam(value = "username") String username);

    @GetMapping(value = "/users/findById")
    DataResponseBody<UserDTO> getUsers(@RequestParam(value = "userId") Long userId);

    @GetMapping(value = "/users/findByIds")
    DataResponseBody<List<UserDTO>> getUserList(@RequestParam(value = "ids") List<Long> ids);

    /**
     * 更改用户配置
     *
     * @param userConfigSaveDTO 用户配置DTO
     */
    @PutMapping(value = "/users/setUserConfig")
    DataResponseBody setUserConfig(@Param("userConfigSaveDTO") UserConfigSaveDTO userConfigSaveDTO);

    /**
     * 获取用户配置
     *
     * @param userId 用户ID
     */
    @GetMapping(value = "/users/getUserConfig")
    DataResponseBody<UserConfigVO> getUserConfig(@RequestParam(value = "userId") Long userId);

    /**
     * 获取用户资源配额总量
     *
     * @return UserAllotResourceVO 用户资源配额
     */
    @GetMapping(value = "/users/userAllot")
    DataResponseBody<UserAllotResourceVO> getUserAllotTotal();

}
