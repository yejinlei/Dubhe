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

import org.dubhe.biz.base.dto.UserConfigSaveDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.UserAllotResourceVO;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description admin远程服务调用类熔断
 * @date 2020-12-10
 */
@Component
public class AdminClientFallback implements AdminClient {
    @Override
    public DataResponseBody findUserByUsername(String username) {
        return DataResponseFactory.failed("call admin server findUserByUsername error");
    }

    @Override
    public DataResponseBody<UserDTO> getUsers(Long userId) {
        return DataResponseFactory.failed("call user controller to get user error");
    }

    @Override
    public DataResponseBody<List<UserDTO>> getUserList(List<Long> ids) {
        return DataResponseFactory.failed("call user controller to get users error");
    }

    @Override
    public DataResponseBody setUserConfig(UserConfigSaveDTO userConfigCreateOrUpdateDTO) {
        return DataResponseFactory.failed("call admin server setUserConfig error");
    }

    @Override
    public DataResponseBody getUserConfig(Long userId) {
        return DataResponseFactory.failed("call admin server getUserConfig error");
    }


    @Override
    public DataResponseBody<UserAllotResourceVO> getUserAllotTotal() {
        return DataResponseFactory.failed("call admin server getUserAllotTotal error ");
    }
}
