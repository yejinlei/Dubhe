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

package org.dubhe.dubhek8s.service.impl;


import com.alibaba.fastjson.JSON;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.enums.SystemNodeEnum;
import org.dubhe.biz.base.utils.MathUtils;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.dubhek8s.domain.dto.NodeDTO;
import org.dubhe.dubhek8s.domain.dto.PodDTO;
import org.dubhe.dubhek8s.service.SystemNodeService;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.dto.NodeIsolationDTO;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizTaint;
import org.dubhe.k8s.domain.vo.PtContainerMetricsVO;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    @Resource
    private AdminClient adminClient;

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
        List<PtContainerMetricsVO> ptContainerMetricsVOList = metricsApi.getContainerMetrics();
        /**
         * 查询pod的指标的集合
         * <podName,PtContainerMetricsVO>
         */

        Map<String,PtContainerMetricsVO> containerMetricsMap = new HashMap<>();
        for (PtContainerMetricsVO vo : ptContainerMetricsVOList){
            if (containerMetricsMap.get(vo.getPodName()) == null){
                containerMetricsMap.put(vo.getPodName(),vo);
            }else {
                containerMetricsMap.get(vo.getPodName()).addCpuUsageAmount(vo.getCpuUsageAmount());
                containerMetricsMap.get(vo.getPodName()).addMemoryUsageAmount(vo.getMemoryUsageAmount());
            }
        }
        List<BizPod> bizPodList = podApi.listAll().parallelStream().filter(obj -> !PodPhaseEnum.SUCCEEDED.getPhase().equals(obj.getPhase())).collect(Collectors.toList());
        /**nodeName->BizNode**/
        Map<String, BizNode> bizNodes = nodeApi.listAll().parallelStream().collect(Collectors.toMap(BizNode::getName, obj -> obj));
        /**遍历数据将node的指标数据封装到nodeDto**/
        if (nodeMetricsList != null && bizNodes != null) {
            nodeMetricsList.forEach(nodeMetrics -> nodeDtoMap.put(nodeMetrics.getNodeName(), toNodeDTO(nodeMetrics, bizNodes)));
        }
        if (bizPodList != null) {
            /**将pod的指标数据封装到podDto里面**/
            bizPodList.forEach(pod -> putPod(toPodDTO(containerMetricsMap.get(pod.getName()), pod), nodeDtoMap));
        }
        /**将podDto数据填充到NodeDto里面**/
        for (NodeDTO obj : nodeDtoMap.values()) {
            obj.setGpuAvailable(MathUtils.reduce(obj.getGpuCapacity(), obj.getGpuUsed()));
            nodeDtoS.add(obj);
        }
        return nodeDtoS;
    }

    /**
     * 查询节点封装的数据和隔离信息
     *
     * @param
     * @return List<NodeDTO> NodeDTO集合
     */
    @Override
    public List<NodeDTO> findNodesIsolation() {
        String curEnv = SpringContextHolder.getActiveProfile();
        List<NodeDTO> nodeDTOList = findNodes();
        List<Long> userIds = nodeDTOList.stream()
                .filter(nodeDTO -> StringUtils.isNotEmpty(nodeDTO.getIsolationEnv()) && nodeDTO.getIsolationEnv().equals(curEnv))
                .map(NodeDTO::getIsolationId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userIds)){
            return nodeDTOList;
        }
        DataResponseBody<List<UserDTO>> userDTODataResponseBody = adminClient.getUserList(userIds);
        if (userDTODataResponseBody == null || CollectionUtils.isEmpty(userDTODataResponseBody.getData())){
            return nodeDTOList;
        }
        Map<Long,UserDTO> userDTOMap = userDTODataResponseBody.getData().stream().collect(Collectors.toMap(UserDTO::getId,Function.identity()));
        nodeDTOList.forEach(nodeDTO -> {
            if (nodeDTO.getIsolationId() != null){
                UserDTO userDTO = userDTOMap.get(nodeDTO.getIsolationId());
                if (userDTO != null){
                    nodeDTO.setIsolation(userDTO.getUsername());
                }else {
                    nodeDTO.setIsolation(nodeDTO.getIsolationEnv()+" 环境 用户id-"+nodeDTO.getIsolationId());
                }
            }
        });
        return nodeDTOList;
    }

    /**
     * k8s节点添加资源隔离
     *
     * @param nodeIsolationDTO k8s节点资源隔离DTO
     * @return boolean
     */
    @Override
    public List<BizNode> addNodeIisolation(NodeIsolationDTO nodeIsolationDTO) {
        List<BizNode> bizNodes = new ArrayList<>();
        if (nodeIsolationDTO == null || nodeIsolationDTO.getUserId() == null || CollectionUtils.isEmpty(nodeIsolationDTO.getNodeNames())){
            return bizNodes;
        }
        List<BizTaint> bizTaintList =  nodeApi.geBizTaintListByUserId(nodeIsolationDTO.getUserId());
        for (String nodeName : nodeIsolationDTO.getNodeNames()){
            nodeApi.addLabel(nodeName,K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY,nodeApi.getNodeIsolationValue(nodeIsolationDTO.getUserId()));
            BizNode bizNode = nodeApi.taint(nodeName,bizTaintList);
            bizNodes.add(bizNode);
        }
        return bizNodes;
    }

    /**
     * k8s节点删除资源隔离
     *
     * @param nodeIsolationDTO k8s节点资源隔离DTO
     * @return boolean
     */
    @Override
    public List<BizNode> delNodeIisolation(NodeIsolationDTO nodeIsolationDTO) {
        List<BizNode> bizNodes = new ArrayList<>();
        if (nodeIsolationDTO == null || CollectionUtils.isEmpty(nodeIsolationDTO.getNodeNames())){
            return bizNodes;
        }
        if (nodeIsolationDTO.getUserId() == null){
            for (String nodeName : nodeIsolationDTO.getNodeNames()){
                nodeApi.deleteLabel(nodeName,K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY);
                BizNode bizNode = nodeApi.delTaint(nodeName);
                bizNodes.add(bizNode);
            }
        }else {
            List<BizTaint> bizTaintList = nodeApi.geBizTaintListByUserId(nodeIsolationDTO.getUserId());
            for (String nodeName : nodeIsolationDTO.getNodeNames()){
                nodeApi.deleteLabel(nodeName,K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY);
                BizNode bizNode = nodeApi.delTaint(nodeName,bizTaintList);
                bizNodes.add(bizNode);
            }
        }
        return bizNodes;
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
            fillIsolationInfo(nodeDTO,node);
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
     * 填充资源隔离信息
     *
     * @param nodeDTO 节点实体类
     * @param node 节点
     */
    private static void fillIsolationInfo(NodeDTO nodeDTO,BizNode node){
        List<BizTaint> taints = node.getTaints();
        if (CollectionUtils.isEmpty(taints)){
            return;
        }
        for (BizTaint taint : taints){
            String isolation =  taint.getKey();
            if (K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY.equals(isolation)){
                String[] isolationInfo = taint.getValue().split(K8sLabelConstants.PLATFORM_TAG_ISOLATION_VALUE_SPLIT);
                nodeDTO.setIsolationEnv(isolationInfo[0]);
                nodeDTO.setIsolationId(Long.valueOf(isolationInfo[1]));
            }
        }
    }

    /**
     * 封装PodDto对象
     *
     * @param podMetrics metrics 实体类
     * @param pod 供业务层使用的k8s pod
     * @return PodDTO
     */
    private PodDTO toPodDTO(PtContainerMetricsVO podMetrics, BizPod pod){
        PodDTO podDTO = new PodDTO();
        podDTO.setPodName(pod.getName());
        if (podMetrics != null){
            podDTO.setPodCpu(transfer(podMetrics.getCpuUsageAmount()) + K8sParamConstants.CPU_UNIT);
            podDTO.setPodMemory(transferMemory(podMetrics.getMemoryUsageAmount()) + K8sParamConstants.MEM_UNIT);
        }else {
            podDTO.setPodCpu(SymbolConstant.ZERO + K8sParamConstants.CPU_UNIT);
            podDTO.setPodMemory(SymbolConstant.ZERO + K8sParamConstants.MEM_UNIT);
        }

        BizQuantity bizQuantity = pod.getContainers().get(MagicNumConstant.ZERO).getLimits() == null ? null : pod.getContainers().get(MagicNumConstant.ZERO).getLimits().get(K8sParamConstants.GPU_RESOURCE_KEY);
        podDTO.setPodCard(bizQuantity == null ? SymbolConstant.ZERO : bizQuantity.getAmount());

        podDTO.setStatus(pod.getPhase());
        podDTO.setNodeName(pod.getNodeName());
        podDTO.setPodCreateTime(pod.getCreationTimestamp());
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
        double cpuAmount = Long.valueOf(amount) * MagicNumConstant.ONE_DOUBLE / MagicNumConstant.MILLION;
        if (cpuAmount < MagicNumConstant.ZERO_DOUBLE) {
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
        return String.valueOf((int) Math.ceil(Long.valueOf(cpu) * MagicNumConstant.ONE_DOUBLE / MagicNumConstant.MILLION));
    }

}
