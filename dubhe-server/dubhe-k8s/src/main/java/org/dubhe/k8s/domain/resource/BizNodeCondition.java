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
 * @description BizNodeCondition实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizNodeCondition {
    /**
     * 上次心跳时间
     */
    @K8sField("lastHeartbeatTime")
    private String lastHeartbeatTime;
    /**
     * 上一次从一种状态转换为另一种状态的时间戳
     */
    @K8sField("lastTransitionTime")
    private String lastTransitionTime;
    /**
     * 相关信息
     */
    @K8sField("message")
    private String message;
    /**
     * 原因
     */
    @K8sField("reason")
    private String reason;
    /**
     * True or False
     */
    @K8sField("status")
    private String status;
    /**
     * NetworkUnavailable:网络是否可用
     * MemoryPressure:内存压力
     * DiskPressure:磁盘压力
     * PIDPressure:PID压力
     * KubeletReady:node是否准备好
     */
    @K8sField("type")
    private String type;
}
