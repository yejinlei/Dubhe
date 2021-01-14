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

package org.dubhe.service.impl;


import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.domain.dto.NodeDTO;
import org.dubhe.domain.dto.PodDTO;
import org.dubhe.enums.SystemNodeEnum;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.vo.PtContainerMetricsVO;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.service.SystemNodeService;
import org.dubhe.utils.MathUtils;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


/**
 * @description SystemNodeService层实现类
 * @date 2020-06-03
 */


@Service
public class SystemNodeServiceImpl implements SystemNodeService {


    @Autowired
    private NodeApi nodeApi;

    @Autowired
    private PodApi podApi;

    @Autowired
    private MetricsApi metricsApi;

    private final static String INTERNAL_IP = "InternalIP";


    /**
     * 查询节点实体列表
     *
     * @param
     * @return List<NodeDTO>  节点实体列表
     **/
    @Override
    public List<NodeDTO> findNodes() {
        /**封装nodeDto数据的集合**/
        List<NodeDTO> nodeDtoS = new ArrayList<>(MagicNumConstant.SIXTEEN);
        /**nodeDto的name和NodeDto对应起来**/
        Map<String, NodeDTO> nodeDtoMap = new HashMap<>(MagicNumConstant.SIXTEEN);
        /**查询node指标的集合**/
        List<PtNodeMetricsVO> nodeMetricsList = metricsApi.getNodeMetrics().stream().collect(toList());
        /**查询pod的指标的集合**/
        List<PtContainerMetricsVO> containerMetrics = metricsApi.getContainerMetrics();
        /**PodName->BizPod**/
        Map<String, BizPod> bizPodList = podApi.listAll().parallelStream().filter(obj -> PodPhaseEnum.RUNNING.getPhase().equals(obj.getPhase())).collect(Collectors.toMap(BizPod::getName, obj -> obj));
        /**nodeName->BizNode**/
        Map<String, BizNode> bizNodes = nodeApi.listAll().parallelStream().collect(Collectors.toMap(BizNode::getName, obj -> obj));
        /**遍历数据将node的指标数据封装到nodeDto**/
        if (nodeMetricsList != null && bizNodes != null) {
            nodeMetricsList.forEach(nodeMetrics -> nodeDtoMap.put(nodeMetrics.getNodeName(), toNodeDTO(nodeMetrics, bizNodes)));
        }
        if (containerMetrics != null && bizPodList != null) {
            /**将pod的指标数据封装到podDto里面**/
            containerMetrics.forEach(podMetrics -> putPod(toPodDTO(podMetrics, bizPodList), nodeDtoMap));
        }
        /**将podDto数据填充到NodeDto里面**/
        for (NodeDTO obj : nodeDtoMap.values()) {
            obj.setGpuAvailable(MathUtils.reduce(obj.getGpuCapacity(), obj.getGpuUsed()));
            nodeDtoS.add(obj);
        }
        return nodeDtoS;
    }

    /**
     * 封装nodeDto对象
     *
     * @param nodeMetrics 节点参数
     * @param bizNodes    BizNode参数
     * @return NodeDTO     NodeDTO对象
     **/
    private static NodeDTO toNodeDTO(PtNodeMetricsVO nodeMetrics, Map<String, BizNode> bizNodes) {
        NodeDTO nodeDTO = new NodeDTO();
        BizNode node = bizNodes.get(nodeMetrics.getNodeName());
        if (node != null && nodeMetrics != null) {
            nodeDTO.setUid(node.getUid());
            nodeDTO.setName(node.getName());
            node.getAddresses().stream().forEach(bizNodeAddress -> {
                if (INTERNAL_IP.equals(bizNodeAddress.getType())) {
                    nodeDTO.setIp(bizNodeAddress.getAddress());
                    return;
                }
            });
            nodeDTO.setStatus(node.getReady() ? K8sParamConstants.NODE_STATUS_TRUE : K8sParamConstants.NODE_STATUS_FALSE);
            Map<String, BizQuantity> capacity = node.getCapacity();
            nodeDTO.setGpuCapacity(capacity.get(K8sParamConstants.GPU_RESOURCE_KEY) == null ? SymbolConstant.ZERO : capacity.get(K8sParamConstants.GPU_RESOURCE_KEY).getAmount());
            nodeDTO.setNodeMemory(transferMemory(nodeMetrics.getMemoryUsageAmount()) + K8sParamConstants.MEM_UNIT);
            nodeDTO.setNodeCpu(transferCpu(nodeMetrics.getCpuUsageAmount()) + K8sParamConstants.CPU_UNIT);
            nodeDTO.setTotalNodeCpu(capacity.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount());
            nodeDTO.setTotalNodeMemory(transferMemory(capacity.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount()) + K8sParamConstants.MEM_UNIT);
            node.getConditions().stream().forEach((bizNodeCondition) -> {
                if ((!(K8sParamConstants.NODE_STATUS_TRUE.equals(bizNodeCondition.getType())) && (K8sParamConstants.NODE_READY_TRUE.equalsIgnoreCase(bizNodeCondition.getStatus())))) {
                    nodeDTO.setWarning(SystemNodeEnum.findMessageByType(bizNodeCondition.getType()));
                }
            });
            return nodeDTO;
        }
        return nodeDTO;
    }

    /**
     * 封装PodDto对象
     *
     * @param podMetrics metrics 实体类
     * @param bizPodList 供业务层使用的k8s pod 集合
     * @return PodDTO  PodDTO对象
     */
    private static PodDTO toPodDTO(PtContainerMetricsVO podMetrics, Map<String, BizPod> bizPodList) {
        BizPod pod = bizPodList.get(podMetrics.getPodName());
        PodDTO podDTO = new PodDTO();
        if (pod != null && podMetrics != null) {
            podDTO.setPodName(podMetrics.getPodName());
            podDTO.setPodCpu(transfer(podMetrics.getCpuUsageAmount()) + K8sParamConstants.CPU_UNIT);
            BizQuantity bizQuantity = pod.getContainers().get(MagicNumConstant.ZERO).getLimits() == null ? null : pod.getContainers().get(MagicNumConstant.ZERO).getLimits().get(K8sParamConstants.GPU_RESOURCE_KEY);
            podDTO.setPodCard(bizQuantity == null ? SymbolConstant.ZERO : bizQuantity.getAmount());
            podDTO.setPodMemory(transferMemory(podMetrics.getMemoryUsageAmount()) + K8sParamConstants.MEM_UNIT);
            podDTO.setStatus(pod.getPhase());
            podDTO.setNodeName(pod.getNodeName());
            podDTO.setPodCreateTime(pod.getCreationTimestamp());
            return podDTO;
        }
        return podDTO;
    }

    /**
     * 将PodDto对象添加到nodeDto对象里面
     *
     * @param podDTO     pod的实体类
     * @param nodeDtoMap 节点实体类
     * @return void
     **/
    private static void putPod(PodDTO podDTO, Map<String, NodeDTO> nodeDtoMap) {
        NodeDTO nodeDTO = nodeDtoMap.get(podDTO.getNodeName());
        if (nodeDTO != null) {
            if (nodeDTO.getPods() == null) {
                nodeDTO.setPods(new ArrayList<PodDTO>(MagicNumConstant.SIXTEEN));
                nodeDTO.setGpuAvailable(nodeDTO.getGpuCapacity());
                nodeDTO.setGpuUsed(SymbolConstant.ZERO);
            }
            nodeDTO.getPods().add(podDTO);
            nodeDTO.setGpuUsed(MathUtils.add(nodeDTO.getGpuUsed(), podDTO.getPodCard()));
        }
    }

    /**
     * 对pod cpu进行转换
     *
     * @param amount 转换参数
     * @return String 转换后的值
     **/
    private static String transfer(String amount) {
        if (StringUtils.isBlank(amount)) {
            return null;
        }
        double cpuAmount = Long.valueOf(amount) * 1.0 / MagicNumConstant.MILLION;
        if (cpuAmount < 0.0) {
            return String.valueOf((int) Math.floor(cpuAmount));
        } else {
            return String.valueOf((int) Math.ceil(cpuAmount));
        }
    }

    /**
     * 对内存进行转换
     *
     * @param memory 转换参数
     * @return String 转换后的值
     **/
    private static String transferMemory(String memory) {
        if (StringUtils.isBlank(memory)) {
            return null;
        }
        return String.valueOf((int) Math.floor(Integer.valueOf(memory) / MagicNumConstant.BINARY_TEN_EXP));
    }

    /**
     * 对cpu进行转换
     *
     * @param cpu 转换参数
     * @return String 转换后的值
     **/
    private static String transferCpu(String cpu) {
        if (StringUtils.isBlank(cpu)) {
            return null;
        }
        return String.valueOf((int) Math.ceil(Long.valueOf(cpu) * 1.0 / MagicNumConstant.MILLION));
    }

}
