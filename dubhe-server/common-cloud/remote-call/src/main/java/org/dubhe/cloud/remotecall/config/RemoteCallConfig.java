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

import org.dubhe.biz.base.constant.AuthConst;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;

/**
 * @description 远程调用默认配置
 * @date 2020-12-11
 */
@Configuration
@EnableFeignClients(basePackages = "org.dubhe")
public class RemoteCallConfig {

    /**
     * 待处理token列表
     */
    public static final List<String> TOKEN_LIST = new LinkedList<>();

    static {
        TOKEN_LIST.add(AuthConst.AUTHORIZATION);
        TOKEN_LIST.add(AuthConst.K8S_CALLBACK_TOKEN);
        TOKEN_LIST.add(AuthConst.COMMON_TOKEN);
    }
}
