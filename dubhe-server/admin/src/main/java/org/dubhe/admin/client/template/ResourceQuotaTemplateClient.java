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

import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.ApplicationNameConst;
import org.dubhe.biz.base.dto.ResourceQuotaDTO;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @description 远程调用资源配额
 * @date 2021-11-16
 */
@Component
public class ResourceQuotaTemplateClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 更新 ResourceQuota
     *
     * @param resourceQuotaDTO 用户配置信息
     * @return DataResponseBody
     */
    public DataResponseBody updateResourceQuota(ResourceQuotaDTO resourceQuotaDTO, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, token);
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(resourceQuotaDTO), headers);
        ResponseEntity<DataResponseBody> responseEntity = restTemplate.postForEntity("http://" + ApplicationNameConst.SERVER_K8S + "/resourceQuota/update", httpEntity, DataResponseBody.class);
        if (HttpStatus.HTTP_OK != responseEntity.getStatusCodeValue()) {
            return null;
        }
        DataResponseBody restResult = responseEntity.getBody();
        return restResult;
    }
}