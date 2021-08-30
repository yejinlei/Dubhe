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

package org.dubhe.k8s.domain.resource;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sField;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @description Kubernetes Service
 * @date 2020-09-09
 */
@Data
@Accessors(chain = true)
public class BizService {
    @K8sField("metadata:name")
    private String name;

    @K8sField("metadata:namespace")
    private String namespace;

    @K8sField("metadata:uid")
    private String uid;

    @K8sField("spec:ports")
    private List<BizServicePort> ports;

    public BizServicePort getServicePortByTargetPort(Integer targetPort){
        if (CollectionUtils.isEmpty(ports) || targetPort == null){
            return null;
        }
        for (BizServicePort port : ports) {
            if (port.getTargetPort() != null && port.getTargetPort().equals(targetPort)){
                return port;
            }
        }
        return null;
    }
}
