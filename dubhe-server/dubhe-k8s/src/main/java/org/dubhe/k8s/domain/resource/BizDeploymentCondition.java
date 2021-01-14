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

/**
 * @description BizDeploymentCondition实体类
 * @date 2020-05-28
 */
@Data
@Accessors(chain = true)
public class BizDeploymentCondition {
    @K8sField("lastTransitionTime")
    private String lastTransitionTime;
    @K8sField("message")
    private java.lang.String message;
    @K8sField("reason")
    private java.lang.String reason;
    @K8sField("status")
    private String status;
    @K8sField("type")
    private String type;
}
