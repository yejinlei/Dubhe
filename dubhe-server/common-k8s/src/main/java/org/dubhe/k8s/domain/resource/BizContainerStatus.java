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
 * @description BizContainerStatus实体类
 * @date 2020-06-02
 */
@Data
@Accessors(chain = true)
public class BizContainerStatus {
    /**
     * Details about a terminated container
     */
    @K8sField("state:terminated")
    private BizContainerStateTerminated terminated;

    @K8sField("lastState:terminated")
    private BizContainerLastStateTerminated lastStateTerminated;

    /**
     * Details about a waiting container
     */
    @K8sField("state:waiting")
    private BizContainerStateWaiting waiting;

    @K8sField("containerID")
    private String containerID;

    @K8sField("restartCount")
    private Integer restartCount;
}
