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
package org.dubhe.auth.rest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.dubhe.biz.base.dto.Oauth2TokenDTO;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.dataresponse.factory.DataResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.Map;

/**
 * @description 授权接口类
 * @date 2020-11-03
 */
@RestController
@RequestMapping(value = "/oauth")
public class AuthController {

    @Resource
    private ConsumerTokenServices consumerTokenServices;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @ApiOperation("Oauth2获取token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "grant_type", value = "授权模式", required = true),
            @ApiImplicitParam(name = "client_id", value = "Oauth2客户端ID", required = true),
            @ApiImplicitParam(name = "client_secret", value = "Oauth2客户端秘钥", required = true),
            @ApiImplicitParam(name = "refresh_token", value = "刷新token"),
            @ApiImplicitParam(name = "username", value = "登录用户名"),
            @ApiImplicitParam(name = "password", value = "登录密码")
    })
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public  DataResponseBody<Oauth2TokenDTO> postAccessToken(@ApiIgnore Principal principal, @ApiIgnore @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenDTO oauth2TokenDto = Oauth2TokenDTO.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .tokenHead(AuthConst.ACCESS_TOKEN_PREFIX).build();

        return DataResponseFactory.success(oauth2TokenDto);
    }


    /**
     * 基于authorization_code方式授权的回调接口
     * @param code
     * @return
     */
    @GetMapping(value="/callback")
    public String hello(String code){
        return "Auth code ->  ".concat(code);
    }


    /**
     * 获取当前用户
     * @param principal
     */
    @GetMapping(value="/user")
    @ResponseBody
    public UserContext getCurUser(Principal principal){
        return userContextService.getCurUser();
    }



    /**
     * 自定义登出（请求时header还是需要Authorization信息）
     * @param accessToken token
     * @return
     */
    @DeleteMapping(value="/logout")
    public DataResponseBody<String> logout(@RequestParam("token")String accessToken){
        String token = StringUtils.substringAfter(accessToken, "Bearer ").trim();
        if (consumerTokenServices.revokeToken(token)){
            // 登出成功，自定义登出业务逻辑
            return DataResponseFactory.success();
        }
        return DataResponseFactory.failed("Logout failed!");
    }

}
