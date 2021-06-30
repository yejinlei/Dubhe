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
package org.dubhe.admin.client;

import org.dubhe.admin.client.fallback.AuthServiceFallback;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.Oauth2TokenDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @description feign调用demo
 * @date 2020-11-04
 */
@FeignClient(value = ApplicationNameConst.SERVER_AUTHORIZATION,fallback = AuthServiceFallback.class)
public interface AuthServiceClient {

    /**
     * 获取token
     *
     * @param parameters 获取token请求map
     * @return token 信息
     */
    @PostMapping(value = "/oauth/token")
    DataResponseBody<Oauth2TokenDTO> postAccessToken(@RequestParam Map<String, String> parameters);

    /**
     * 登出
     * @param accessToken token
     * @return
     */
    @DeleteMapping(value="/oauth/logout")
    DataResponseBody<String> logout(@RequestParam("token")String accessToken);

}
