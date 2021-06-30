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

package org.dubhe.cloud.remotecall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


/**
 * @description RestTemplate持有器(调用非nacos中注册的服务)
 * @date 2021-01-07
 */
@Component
public class RestTemplateHolder {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        RestTemplate template = restTemplateBuilder.build();

        MediaType[] mediaTypes = new MediaType[]{
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_STREAM_JSON,
                MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.APPLICATION_JSON_UTF8,
                MediaType.APPLICATION_PDF,
        };

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(mediaTypes));

        try {
            Field field = template.getClass().getDeclaredField("messageConverters");
            field.setAccessible(true);
            List<HttpMessageConverter<?>> orgConverterList = (List<HttpMessageConverter<?>>) field.get(template);

            for (int i = 0; i < orgConverterList.size(); i++) {
                if (MappingJackson2HttpMessageConverter.class.equals(orgConverterList.get(i).getClass())) {
                    orgConverterList.set(i, converter);
                }
            }

        } catch (Exception e) {
            LogUtil.error(LogEnum.SYS_ERR, "restTemplate bean instance error, exception is :【{}】", e);
        }
        this.restTemplate = template;
    }

    public RestTemplate getRestTemplate(){
        return this.restTemplate;
    }


}
