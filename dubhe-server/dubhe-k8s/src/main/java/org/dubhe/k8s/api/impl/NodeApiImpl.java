/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.k8s.api.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description NodeApi实现类
 * @date 2020-04-21
 */
public class NodeApiImpl implements NodeApi {

    @Autowired
    private MetricsApi metricsApi;

    private KubernetesClient client;

    public NodeApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    /**
     * 根据节点名称查询节点信息
     *
     * @param nodeName 节点名称
     * @return BizNode Node 业务类
     */
    @Override
    public BizNode get(String nodeName) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={}", nodeName);
            if (StringUtils.isEmpty(nodeName)) {
                return null;
            }
            Node node = client.nodes().withName(nodeName).get();
            BizNode bizNode = BizConvertUtils.toBizNode(node);
            LogUtil.info(LogEnum.BIZ_K8S, "Output {}", bizNode);
            return bizNode;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.get error, param:[nodeName]={}, error:", nodeName, e);
            return new BizNode().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询所有节点信息
     *
     * @return List<BizNode> Node 业务类集合
     */
    @Override
    public List<BizNode> listAll() {
        try {
            NodeList nodes = client.nodes().list();
            if (nodes == null || CollectionUtils.isEmpty(nodes.getItems())) {
                return Collections.EMPTY_LIST;
            }
            List<BizNode> bizNodeList = nodes.getItems().parallelStream().map(obj -> BizConvertUtils.toBizNode(obj).setReady()).collect(Collectors.toList());
            LogUtil.info(LogEnum.BIZ_K8S, "Output {}", bizNodeList);
            return bizNodeList;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.listAll error:", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 给节点添加单个标签
     *
     * @param nodeName 节点名称
     * @param labelKey 标签的键
     * @param labelValue 标签的值
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult addLabel(String nodeName, String labelKey, String labelValue) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={};labelKey={};labelValue={}", nodeName, labelKey, labelValue);
            if (StringUtils.isEmpty(nodeName) || StringUtils.isEmpty(labelKey)) {
                return new PtBaseResult().baseErrorBadRequest();
            }
            client.nodes().withName(nodeName).edit().editMetadata().addToLabels(labelKey, labelValue).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.addLabel error, param:[nodeName]={}, [labelKey]={}, [labelValue]={}, error:", nodeName, labelKey, labelValue, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 给节点添加多个标签
     *
     * @param nodeName 节点名称
     * @param labels 标签Map
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult addLabels(String nodeName, Map<String, String> labels) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={};labels={}", nodeName, labels);
            if (StringUtils.isEmpty(nodeName) || CollectionUtils.isEmpty(labels)) {
                return new PtBaseResult().baseErrorBadRequest();
            }
            client.nodes().withName(nodeName).edit().editMetadata().addToLabels(labels).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.addLabels error, param:[nodeName]={}, [labels]={},error:",nodeName, JSON.toJSONString(labels), e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除节点单个标签
     *
     * @param nodeName 节点名称
     * @param labelKey 标签的键
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult deleteLabel(String nodeName, String labelKey) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={};labelKey={}", nodeName, labelKey);
            if (StringUtils.isEmpty(nodeName) || StringUtils.isEmpty(labelKey)) {
                return new PtBaseResult().baseErrorBadRequest();
            }
            client.nodes().withName(nodeName).edit().editMetadata().removeFromLabels(labelKey).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.deleteLabel error, param:[nodeName]={}, [labelKey]={},error:",nodeName, labelKey, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除节点的多个标签
     *
     * @param nodeName 节点名称
     * @param labels 标签键集合
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult deleteLabels(String nodeName, Set<String> labels) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={};labels={}", nodeName, labels);
            if (StringUtils.isEmpty(nodeName) || CollectionUtils.isEmpty(labels)) {
                return new PtBaseResult().baseErrorBadRequest();
            }
            Map<String, String> map = new HashMap<>();
            Iterator<String> it = labels.iterator();
            while (it.hasNext()) {
                map.put(it.next(), null);
            }
            client.nodes().withName(nodeName).edit().editMetadata().removeFromLabels(map).endMetadata().done();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.deleteLabelS error, param:[nodeName]={}, [labels]={},error:", nodeName, JSON.toJSONString(labels), e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 设置节点是否可调度
     *
     * @param nodeName 节点名称
     * @param schedulable 参数true或false
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult schedulable(String nodeName, boolean schedulable) {
        LogUtil.info(LogEnum.BIZ_K8S, "Input nodeName={};schedulable={}", nodeName, schedulable);
        if (StringUtils.isEmpty(nodeName)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            Node node = client.nodes().withName(nodeName).get();
            if (node == null) {
                return K8sResponseEnum.NOT_FOUND.toPtBaseResult();
            }
            node.getSpec().setUnschedulable(schedulable);
            client.nodes().withName(nodeName).replace(node);
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.schedulable error:", e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询集群资源是否充足
     *
     * @param cpuNum 单位为m 1核等于1000m
     * @param memNum 单位为Mi 1Mi等于1024Ki
     * @param gpuNum 单位为显卡，即"1"表示1张显卡
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    @Override
    public LackOfResourcesEnum isAllocatable(Integer cpuNum, Integer memNum, Integer gpuNum) {
        LogUtil.info(LogEnum.BIZ_K8S, "Input cpuNum={};memNum={};gpuNum={}", cpuNum, memNum, gpuNum);
        NodeList list = client.nodes().list();
        List<Node> nodeItems = list.getItems();
        nodeItems = nodeItems.stream().filter(nodeItem -> CollectionUtils.isEmpty(nodeItem.getSpec().getTaints())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodeItems)) {
            return LackOfResourcesEnum.LACK_OF_NODE;
        }
        if (cpuNum != null && cpuNum >= MagicNumConstant.ZERO) {
            nodeItems = isCpuAllocatable(cpuNum, nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                return LackOfResourcesEnum.LACK_OF_CPU;
            }
        }

        if (memNum != null && memNum >= MagicNumConstant.ZERO) {
            nodeItems = isMemAllocatable(memNum, nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                return LackOfResourcesEnum.LACK_OF_MEM;
            }
        }

        if (gpuNum != null && gpuNum >= MagicNumConstant.ZERO) {
            nodeItems = isGpuAllocatable(gpuNum, nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                return LackOfResourcesEnum.LACK_OF_GPU;
            }
        }

        return LackOfResourcesEnum.ADEQUATE;
    }


    /**
     * 查询节点内存资源是否可分配
     *
     * @param memNum 单位为Mi 1Mi等于1024Ki
     * @param nodeItems Node集合
     * @return List<Node> Node集合
     */
    private List<Node> isMemAllocatable(int memNum, List<Node> nodeItems) {
        List<Node> nodeItemResults = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        List<PtNodeMetricsVO> nodeMetrics = metricsApi.getNodeMetrics();

        nodeItems.forEach(nodeItem -> {
            String nodeName = nodeItem.getMetadata().getName();
            List<PtNodeMetricsVO> collect = nodeMetrics.stream().filter(nodeMetric -> nodeMetric.getNodeName().equals(nodeName)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                String memAmount = collect.get(0).getMemoryUsageAmount();
                int memCapacity = Integer.parseInt(nodeItem.getStatus().getCapacity().get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount()) / MagicNumConstant.ONE_THOUSAND;
                int memAmountInt = Integer.parseInt(memAmount) / MagicNumConstant.BINARY_TEN_EXP;
                if (memCapacity - memAmountInt > memNum) {
                    nodeNameList.add(nodeName);
                }
            }

        });

        nodeNameList.forEach(nodeName -> {
            List<Node> collect = nodeItems.stream().filter(nodeItem -> nodeItem.getMetadata().getName().equals(nodeName)).collect(Collectors.toList());
            nodeItemResults.addAll(collect);
        });

        return nodeItemResults;
    }


    /**
     * 查询节点Cpu资源是否可分配
     *
     * @param cpuNum 单位为m 1核等于1000m
     * @param nodeItems Node集合
     * @return List<Node> Node集合
     */
    private List<Node> isCpuAllocatable(int cpuNum, List<Node> nodeItems) {

        List<Node> nodeItemResults = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        List<PtNodeMetricsVO> nodeMetrics = metricsApi.getNodeMetrics();

        nodeItems.forEach(nodeItem -> {
            String nodeName = nodeItem.getMetadata().getName();
            List<PtNodeMetricsVO> collect = nodeMetrics.stream().filter(nodeMetric -> nodeMetric.getNodeName().equals(nodeName)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)){
                String cpuAmount = collect.get(0).getCpuUsageAmount();
                int cpuCapacity = Integer.parseInt(nodeItem.getStatus().getCapacity().get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount()) * MagicNumConstant.ONE_THOUSAND;
                int cpuAmountInt = (int) (Long.parseLong(cpuAmount) / MagicNumConstant.ONE_THOUSAND / MagicNumConstant.ONE_THOUSAND);
                if (cpuCapacity - cpuAmountInt >= cpuNum) {
                    nodeNameList.add(nodeName);
                }
            }
        });

        nodeNameList.forEach(nodeName -> {
            List<Node> collect = nodeItems.stream().filter(nodeItem -> nodeItem.getMetadata().getName().equals(nodeName)).collect(Collectors.toList());
            nodeItemResults.addAll(collect);
        });

        return nodeItemResults;

    }


    /**
     * 查询节点Gpu资源是否可分配
     *
     * @param gpuNum 单位为显卡，即"1"表示1张显卡
     * @param nodeItems Node集合
     * @return List<Node> Node集合
     */
    private List<Node> isGpuAllocatable(int gpuNum, List<Node> nodeItems) {
        List<Node> nodeItemResults = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        nodeItems = nodeItems.stream().filter(node -> node.getStatus().getCapacity().containsKey(K8sParamConstants.GPU_RESOURCE_KEY)).collect(Collectors.toList());

        PodList podList = client.pods().list();
        List<Pod> podItems = podList.getItems();
        podItems = podItems.stream().filter(pod ->
                pod.getSpec().getContainers().get(0).getResources().getLimits() != null
                        && pod.getSpec().getContainers().get(0).getResources().getLimits().containsKey(K8sParamConstants.GPU_RESOURCE_KEY)
                        && pod.getStatus().getPhase().equals(PodPhaseEnum.RUNNING.getPhase())).collect(Collectors.toList());
        Map<String, Integer> allocatableGpu = new HashMap();

        for (Node nodeItem : nodeItems) {
            int totalGpuAmount = 0;
            int totalGpu = Integer.parseInt(nodeItem.getStatus().getCapacity().get(K8sParamConstants.GPU_RESOURCE_KEY).getAmount());
            String nodeName = nodeItem.getMetadata().getName();
            List<Pod> nodePodItems = podItems.stream().filter(pod -> pod.getSpec().getNodeName().equals(nodeName)).collect(Collectors.toList());
            for (Pod pod : nodePodItems) {
                String gpuAmount = pod.getSpec().getContainers().get(0).getResources().getLimits().get(K8sParamConstants.GPU_RESOURCE_KEY).getAmount();
                totalGpuAmount = totalGpuAmount + Integer.parseInt(gpuAmount);
            }
            allocatableGpu.put(nodeName, totalGpu - totalGpuAmount);

        }
        Set<String> keySet = allocatableGpu.keySet();

        keySet.forEach(key -> {
            if (allocatableGpu.get(key) >= gpuNum) {
                nodeNameList.add(key);
            }
        });

        for (String nodeName : nodeNameList) {
            List<Node> collect = nodeItems.stream().filter(nodeItem -> nodeItem.getMetadata().getName().equals(nodeName)).collect(Collectors.toList());
            nodeItemResults.addAll(collect);
        }
        return nodeItemResults;
    }
}
