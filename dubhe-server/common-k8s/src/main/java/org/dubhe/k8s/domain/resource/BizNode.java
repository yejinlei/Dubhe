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

import cn.hutool.core.collection.CollectionUtil;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.domain.PtBaseResult;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.enums.NodeConditionTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * @description BizNode实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizNode extends PtBaseResult<BizNode> {
    @K8sField("apiVersion")
    private String apiVersion;
    @K8sField("kind")
    private String kind;

    @K8sField("metadata:creationTimestamp")
    private String creationTimestamp;
    /**
     * node的标签
     */
    @K8sField("metadata:labels")
    private Map<String, String> labels = Maps.newHashMap();
    /**
     * node的名称
     */
    @K8sField("metadata:name")
    private String name;
    /**
     * 唯一标识
     */
    @K8sField("metadata:uid")
    private String uid;
    @K8sField("metadata:resourceVersion")
    private String resourceVersion;
    /**
     * 不可调度，为true时pod不会调度到此节点
     */
    @K8sField("spec:unschedulable")
    private boolean unschedulable;
    /**
     * 污点
     */
    @K8sField("spec:taints")
    private List<BizTaint> taints;

    /**
     * 节点可到达的地址列表，主机名和ip
     */
    @K8sField("status:addresses")
    private List<BizNodeAddress> addresses;
    /**
     * 可用于调度的节点资源
     */
    @K8sField("status:allocatable")
    private Map<String, BizQuantity> allocatable;
    /**
     * 节点的总资源
     */
    @K8sField("status:capacity")
    private Map<String, BizQuantity> capacity;
    /**
     * 当前的节点状态数组
     */
    @K8sField("status:conditions")
    private List<BizNodeCondition> conditions;
    /**
     * 节点的一些信息
     */
    @K8sField("status:nodeInfo")
    private BizNodeSystemInfo nodeInfo;

    /**
     * 是否ready
     */
    private Boolean ready;

    public BizNode setReady() {
        if (CollectionUtil.isNotEmpty(conditions)) {
            ready = conditions.stream().filter(obj -> NodeConditionTypeEnum.READY.getType().equals(obj.getType()) && K8sParamConstants.NODE_READY_TRUE.equals(obj.getStatus())).count() > 0;
        }
        return this;
    }
}
