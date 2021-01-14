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

package org.dubhe.k8s.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @description k8s connection properties
 * @date 2020-04-09
 */
@Data
@ConfigurationProperties("k8s")
public class ClusterProperties {

    private String url;

    private String nfs;

    private String host;

    private String port;

    private String kubeconfig;

    /**
     * 需使用Resource
     */
    private Resource clientCrt;

    private Resource clientKey;

    private Resource caCrt;
}
