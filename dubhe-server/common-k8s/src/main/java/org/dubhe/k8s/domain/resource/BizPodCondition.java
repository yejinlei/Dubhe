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
 * @description BizPodCondition实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizPodCondition {
    /**
     * Pod上一次从一种状态转换为另一种状态的时间戳
     */
    @K8sField("lastTransitionTime")
    private String lastTransitionTime;
    /**
     * Pod状态信息
     */
    @K8sField("message")
    private String message;
    /**
     * True or False
     */
    @K8sField("status")
    private String status;
    /**
     * PodScheduled：已将Pod调度到一个节点；
     * Ready：该Pod能够处理请求，应将其添加到所有匹配服务的负载平衡池中；
     * Initialized：所有初始化容器已成功启动；
     * ContainersReady：容器中的所有容器均已准备就绪。
     */
    @K8sField("type")
    private String type;
}
