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
package org.dubhe.admin.client.template;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpStatus;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.constant.AuthConst;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description 模拟登录获取token
 * @date 2021-11-11
 */
@Component
public class ObtainAccessToken {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${rsa.private_key}")
    private String privateKey;

    /**
     *  模拟登录获取token
     * @return String  token
     */
    public String generateToken(String username,String userPassword) {
        String password = null;
        try {
            RSA rsa = new RSA(privateKey, null);
            password = new String(rsa.decrypt(userPassword, KeyType.PrivateKey));
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_SYS, "rsa 密钥解析失败, originPassword:{} , 密钥:{}，异常：{}", userPassword, KeyType.PrivateKey, e);
            throw new BusinessException("密钥解析失败");
        }
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", AuthConst.GRANT_TYPE);
        params.add("client_id", AuthConst.CLIENT_ID);
        params.add("client_secret", AuthConst.CLIENT_SECRET);
        params.add("username", username);
        params.add("password", password);
        params.add("scope", "all");
        HttpHeaders headers = new HttpHeaders();
        // 需求需要传参为application/x-www-form-urlencoded格式
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        ResponseEntity<DataResponseBody> responseEntity = restTemplate.postForEntity("http://" + ApplicationNameConst.SERVER_AUTHORIZATION + "/oauth/token", httpEntity, DataResponseBody.class);
        if (HttpStatus.HTTP_OK != responseEntity.getStatusCodeValue()) {
            return null;
        }
        DataResponseBody restResult = responseEntity.getBody();
        Map map = new LinkedHashMap();
        if (restResult.succeed()) {
            map = (Map) restResult.getData();
        }
        // 返回 token
        return (String) map.get("token");
    }
}