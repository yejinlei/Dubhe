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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description docker client 工厂类
 * @date 2021-07-05
 */
@Component
public class DockerClientFactory {
    @Autowired
    private DubheDockerJavaConfig dubheDockerJavaConfig;

    /**
     * 创建连接
     *
     * @param host ip或域名
     * @return DockerClient
     */
    public DockerClient getDockerClient(String host){
        try{
            DockerClientConfig custom = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://"+host+ SymbolConstant.COLON + dubheDockerJavaConfig.getDockerRemoteApiPort())
                    .withDockerTlsVerify(false)
                    .build();
            return DockerClientBuilder.getInstance(custom).build();
        }catch (Exception e){
            LogUtil.error(LogEnum.TERMINAL, "DockerClientFactory getDockerClient error:{}",e.getMessage(), e);
            return null;
        }
    }
}