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

package org.dubhe.docker.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description docker-java相关配置
 * @date 2021-07-05
 */
@Getter
@Configuration
public class DubheDockerJavaConfig {
    @Value("${docker.remote-api-port}")
    private String dockerRemoteApiPort;

    @Value("${harbor.address}")
    private String harborAddress;

    @Value("${harbor.username}")
    private String harborUserName;

    @Value("${harbor.password}")
    private String harborPassword;
}
