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

import java.util.List;
import java.util.Map;

/**
 * @description BizContainer对象
 * @date 2020-04-15
 */
@Data
@Accessors(chain = true)
public class BizContainer {
    @K8sField("image")
    private String image;
    @K8sField("imagePullPolicy")
    private String imagePullPolicy;
    @K8sField("name")
    private String name;
    @K8sField("volumeMounts")
    private List<BizVolumeMount> volumeMounts;
    @K8sField("resources:limits")
    private Map<String, BizQuantity> limits;
    @K8sField("resources:requests")
    private Map<String, BizQuantity> requests;
}
