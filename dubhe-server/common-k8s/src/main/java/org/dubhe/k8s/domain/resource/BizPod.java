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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * @description 供业务层使用的k8s pod
 * @date 2020-04-15
 */
@Data
@Accessors(chain = true)
public class BizPod extends PtBaseResult<BizPod> {
    private static final String CONTAINER_STATE_MESSAGE = "Pod {} {} reason ： {}, message {} ";

    @K8sField("metadata:name")
    private String name;
    /**
     * 创建此资源对象时间戳
     */
    @K8sField("metadata:creationTimestamp")
    private String creationTimestamp;
    @K8sField("metadata:labels")
    private Map<String, String> labels = Maps.newHashMap();
    @K8sField("metadata:namespace")
    private String namespace;
    @K8sField("metadata:uid")
    private String uid;
    @K8sField("spec:containers")
    private List<BizContainer> containers;
    @K8sField("spec:nodeName")
    private String nodeName;
    @K8sField("status:podIP")
    private String podIp;
    @K8sField("spec:volumes")
    private List<BizVolume> volumes;
    @K8sField("status:hostIP")
    private String hostIP;

    /**
     * Pending:待处理
     * Running:运行
     * Succeeded:pod中的所有container已成功终止，将不会重新启动
     * Failed:pod中的所有container均已终止，并且至少一个容器因故障而终止
     * Unknown:由于某种原因，无法获得Pod的状态
     */
    @K8sField("status:phase")
    private String phase;
    /**
     * Kubelet确认时间，此时间戳生成在pull images之前
     */
    @K8sField("status:startTime")
    private String startTime;
    /**
     * 状态变化时间戳
     */
    @K8sField("status:conditions")
    private List<BizPodCondition> conditions;
    /**
     * container 状态
     */
    @K8sField("status:containerStatuses")
    private List<BizContainerStatus> containerStatuses;

    /**
     * 训练结束时间
     */
    private String completedTime;

    /**
     * 获取业务标签
     */
    public String getBusinessLabel() {
        return labels.get(K8sLabelConstants.BASE_TAG_BUSINESS);
    }

    /**
     * 获取任务身份标识
     */
    public String getTaskIdentifyLabel() {
        return labels.get(K8sLabelConstants.BASE_TAG_TASK_IDENTIFY);
    }

    /**
     * 根据键获取label
     *
     * @param labelKey
     * @return
     */
    public String getLabel(String labelKey) {
        return labels.get(labelKey);
    }

    /**
     * 拼接message
     *
     * @return
     */
    public String getContainerStateMessages() {
        StringBuilder messages = new StringBuilder();
        if (containerStatuses == null) {
            return null;
        }
        containerStatuses.forEach(obj -> {
            if (obj.getTerminated() != null) {
                messages.append(StrUtil.format(CONTAINER_STATE_MESSAGE, name, phase, obj.getTerminated().getReason(), obj.getTerminated().getMessage()));
            }
            if (obj.getWaiting() != null) {
                messages.append(StrUtil.format(CONTAINER_STATE_MESSAGE, name, phase, obj.getWaiting().getReason(), obj.getWaiting().getMessage()));
            }
        });
        return messages.toString();
    }

    //获取 容器镜像id
    public String getContainerId(){
        String containerID = null;
        if (!CollectionUtils.isEmpty(containerStatuses)){
            for (BizContainerStatus bizContainerStatus : containerStatuses){
                if (StringUtils.isNotEmpty(bizContainerStatus.getContainerID())){
                    containerID = bizContainerStatus.getContainerID();
                }
            }
        }
        if (StringUtils.isNotEmpty(containerID)){
            return containerID.replace(K8sParamConstants.CONTAINER_ID_PREFIX,"");
        }
        return containerID;
    }

    public String getRealPodPhase(){
        if (PodPhaseEnum.RUNNING.getPhase().equals(phase) && !CollectionUtils.isEmpty(containerStatuses) && containerStatuses.get(MagicNumConstant.ZERO).getWaiting() != null){
            String waitingReason = containerStatuses.get(MagicNumConstant.ZERO).getWaiting().getReason();
            if(waitingReason != null && !K8sParamConstants.WAITING_REASON_CONTAINER_CREATING.equals(waitingReason)){
                return PodPhaseEnum.FAILED.getPhase();
            }
        }
        return phase;
    }
}
