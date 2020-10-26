/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

package org.onebrain.operator.config;

import cn.hutool.core.util.StrUtil;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.onebrain.operator.context.KubeContext;
import org.onebrain.operator.properties.KubeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description k8s配置类
 * @date 2020-09-23
 */
@Configuration
@EnableConfigurationProperties(KubeProperties.class)
public class KubeConfig {

    @Autowired
    private KubeProperties kubeProperties;

    /**
     * 注册k8s配置
     * @return
     */
    @Bean
    public KubeContext kubeContext() {
        if (kubeProperties == null) {
            return null;
        }

        final String configSource = kubeProperties.getKubeconfig();
        if(StrUtil.isEmpty(configSource)){
            return null;
        }
        return new KubeContext(kubeProperties);
    }

    /**
     * 注册k8s客户端
     * @param kubeContext k8s配置
     * @return
     */
    @Bean
    public KubernetesClient kubernetesClient(KubeContext kubeContext){
        return kubeContext.getClient();
    }
}
