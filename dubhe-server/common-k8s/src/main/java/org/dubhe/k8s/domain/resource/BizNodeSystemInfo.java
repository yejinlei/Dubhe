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

import org.dubhe.k8s.annotation.K8sField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description BizNodeSystemInfo实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizNodeSystemInfo {
    @K8sField("architecture")
    private String architecture;
    @K8sField("bootID")
    private String bootID;
    @K8sField("containerRuntimeVersion")
    private String containerRuntimeVersion;
    @K8sField("kernelVersion")
    private String kernelVersion;
    @K8sField("kubeProxyVersion")
    private String kubeProxyVersion;
    @K8sField("kubeletVersion")
    private String kubeletVersion;
    @K8sField("machineID")
    private String machineID;
    @K8sField("operatingSystem")
    private String operatingSystem;
    @K8sField("osImage")
    private String osImage;
    @K8sField("systemUUID")
    private String systemUUID;
}
