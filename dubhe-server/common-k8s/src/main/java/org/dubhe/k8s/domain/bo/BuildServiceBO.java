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

package org.dubhe.k8s.domain.bo;

import io.fabric8.kubernetes.api.model.ServicePort;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description 构建 Service
 * @date 2020-09-11
 */
@Data
@Accessors(chain = true)
public class BuildServiceBO {
    private String namespace;
    private String name;
    private Map<String, String> labels;
    private Map<String, String> selector;
    private List<ServicePort> ports;
    private String type;

    public BuildServiceBO(String namespace, String name, Map<String, String> labels, Map<String, String> selector){
        this.namespace = namespace;
        this.name = name;
        this.labels = labels;
        this.selector = selector;
    }

    public BuildServiceBO(String namespace, String name, Map<String, String> labels, Map<String, String> selector,String type){
        this.namespace = namespace;
        this.name = name;
        this.labels = labels;
        this.selector = selector;
        this.type = type;
    }

    /**
     * 添加端口
     * @param port
     */
    public void addPort(ServicePort port){
        if (ports == null){
            ports = new ArrayList<>();
        }
        ports.add(port);
    }
}
