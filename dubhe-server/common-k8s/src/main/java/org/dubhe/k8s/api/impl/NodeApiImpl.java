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

package org.dubhe.k8s.api.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Taint;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.BaseResourceBo;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.domain.resource.BizTaint;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.K8sTolerationEffectEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.ResourceBuildUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

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

    @Autowired
    private UserContextService userContextService;

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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.get error, param:[nodeName]={}, error:{}", nodeName, e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.listAll error:{}", e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.addLabel error, param:[nodeName]={}, [labelKey]={}, [labelValue]={}, error:{}", nodeName, labelKey, labelValue, e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.addLabels error, param:[nodeName]={}, [labels]={},error:{}", nodeName, JSON.toJSONString(labels), e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.deleteLabel error, param:[nodeName]={}, [labelKey]={},error:{}", nodeName, labelKey, e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.deleteLabelS error, param:[nodeName]={}, [labels]={},error:{}", nodeName, JSON.toJSONString(labels), e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 根据标签查询节点
     *
     * @param key 标签key
     * @param value 标签value
     * @return List<BizNode>
     */
    @Override
    public List<BizNode> getWithLabel(String key, String value) {
        try {
            List<BizNode> bizNodes = new ArrayList<>();
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                return bizNodes;
            }
            NodeList nodeList = client.nodes().withLabel(key, value).list();
            if (nodeList != null && !CollectionUtils.isEmpty(nodeList.getItems())) {
                return BizConvertUtils.toBizNodes(nodeList.getItems());
            }
            return bizNodes;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.getWithLabels error, param:[key]={} [value]={},error:{}", key, value, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据标签查询节点
     *
     * @param labels 标签
     * @return List<BizNode>
     */
    @Override
    public List<BizNode> getWithLabels(Map<String, String> labels) {
        try {
            List<BizNode> bizNodes = new ArrayList<>();
            if (CollectionUtils.isEmpty(labels)) {
                return bizNodes;
            }
            NodeList nodeList = client.nodes().withLabels(labels).list();
            if (nodeList != null && !CollectionUtils.isEmpty(nodeList.getItems())) {
                return BizConvertUtils.toBizNodes(nodeList.getItems());
            }
            return bizNodes;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.getWithLabels error, param:[labels]={},error:{}", JSON.toJSONString(labels), e);
            return new ArrayList<>();
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
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.schedulable error:{}", e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询集群资源是否充足
     *
     * @param nodeSelector 节点选择标签
     * @param taints 该资源所能容忍的污点
     * @param baseResourceBo 资源通用属性基类
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    @Override
    public LackOfResourcesEnum isAllocatable(Map<String, String> nodeSelector, List<BizTaint> taints, BaseResourceBo baseResourceBo) {
        LogUtil.info(LogEnum.BIZ_K8S, "Input nodeSelector={};taints={};cpuNum={};memNum={};gpuNum={}", JSON.toJSONString(nodeSelector), JSON.toJSONString(taints), baseResourceBo.getCpuNum()
                , baseResourceBo.getMemNum(), baseResourceBo.getGpuNum());
        NodeList list;
        try {
            list = client.nodes().list();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.isAllocatable error:{}", e);
            return LackOfResourcesEnum.LACK_OF_NODE;
        }

        List<Node> nodeItems = list.getItems();
        //根据nodeSelector筛选节点

        if (CollectionUtil.isNotEmpty(nodeSelector)) {
            for (String nodeSelectorKey : nodeSelector.keySet()) {
                String nodeSelectorValue = nodeSelector.get(nodeSelectorKey);
                nodeItems = nodeItems.stream().filter(nodeItem -> nodeSelectorValue.equals(nodeItem.getMetadata().getLabels().get(nodeSelectorKey)))
                        .collect(Collectors.toList());
            }
        }
        //根据可容忍的污点筛选节点
        if (CollectionUtil.isEmpty(taints)) {
            nodeItems = nodeItems.stream().filter(nodeItem -> CollectionUtils.isEmpty(nodeItem.getSpec().getTaints())).collect(Collectors.toList());
        } else {
            List<Node> taintNodes = new ArrayList<>();
            for (BizTaint taint : taints) {
                taintNodes.addAll(nodeItems.stream().filter(nodeItem -> doesTaintExit(nodeItem, taint)).collect(Collectors.toList()));
            }
            nodeItems = taintNodes;
        }

        if (CollectionUtils.isEmpty(nodeItems)) {
            return LackOfResourcesEnum.LACK_OF_NODE;
        }
        if (baseResourceBo.getCpuNum() != null && baseResourceBo.getCpuNum() >= MagicNumConstant.ZERO) {
            nodeItems = isCpuAllocatable(baseResourceBo.getCpuNum(), nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                LogUtil.info(LogEnum.BIZ_K8S, "isAllocatable "+ LackOfResourcesEnum.LACK_OF_CPU.getMessage()+" nodeSelector={} taints={}  baseResourceBo={}",nodeSelector,taints,baseResourceBo);
                return LackOfResourcesEnum.LACK_OF_CPU;
            }
        }

        if (baseResourceBo.getMemNum() != null && baseResourceBo.getMemNum() >= MagicNumConstant.ZERO) {
            nodeItems = isMemAllocatable(baseResourceBo.getMemNum(), nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                LogUtil.info(LogEnum.BIZ_K8S, "isAllocatable "+ LackOfResourcesEnum.LACK_OF_MEM.getMessage()+" nodeSelector={} taints={}  baseResourceBo={}",nodeSelector,taints,baseResourceBo);
                return LackOfResourcesEnum.LACK_OF_MEM;
            }
        }

        if (baseResourceBo.getGpuNum() != null && baseResourceBo.getGpuNum() > MagicNumConstant.ZERO) {
            nodeItems = isGpuAllocatable(baseResourceBo, nodeItems);
            if (CollectionUtils.isEmpty(nodeItems)) {
                LogUtil.info(LogEnum.BIZ_K8S, "isAllocatable "+ LackOfResourcesEnum.LACK_OF_GPU.getMessage()+" nodeSelector={} taints={}  baseResourceBo={}",nodeSelector,taints,baseResourceBo);
                return LackOfResourcesEnum.LACK_OF_GPU;
            }
        }

        return LackOfResourcesEnum.ADEQUATE;
    }

    /**
     * 查询集群资源是否充足
     *
     * @param baseResourceBo 单位为m 资源通用属性基类
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    @Override
    public LackOfResourcesEnum isAllocatable(BaseResourceBo baseResourceBo) {
        Toleration toleration = getNodeIsolationToleration();
        Map<String, String> nodeSelecter = new HashMap<>();

        if (StrUtil.isNotEmpty(baseResourceBo.getGpuModel())) {
            nodeSelecter.put(K8sLabelConstants.NODE_GPU_MODEL_LABEL_KEY, baseResourceBo.getGpuModel());
        }

        if (toleration == null) {
            return isAllocatable(nodeSelecter, null, baseResourceBo);
        } else {
            nodeSelecter.putAll(getNodeIsolationNodeSelector());
            return isAllocatable(nodeSelecter, geBizTaintListByUserId(), baseResourceBo);
        }
    }

    /**
     * 查询集群资源是否充足
     *
     * @param namespace 命名空间
     * @param cpuNum cpu限制 单位核 0表示不限制
     * @param memNum 内存限制 单位G 0表示不限制
     *  @param k8sLabelKey k8s GPU资源标签key值(例如：nvidia.com/gpu)
     * @param gpuNum GPU数量，0表示共享显卡，null表示不使用显卡
     * @param gpuModel gpu型号
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    @Override
    public LackOfResourcesEnum isAllocatableConvert(String namespace, Integer cpuNum, Integer memNum, Boolean useGpu, String k8sLabelKey, String gpuModel, Integer gpuNum) {
        BaseResourceBo bo = new BaseResourceBo();
        bo.setNamespace(namespace).setCpuNum(cpuNum).setMemNum(memNum).setUseGpu(useGpu).setK8sLabelKey(k8sLabelKey).setGpuModel(gpuModel).setGpuNum(gpuNum);
        return isAllocatable(bo);
    }


    /**
     * 判断是否超出总可分配gpu数
     * @param gpuNum gpu数量
     * @param gpuModel gpu型号
     * @return LackOfResourcesEnum 资源缺乏枚举类
     */
    @Override
    public LackOfResourcesEnum isOutOfTotalAllocatableGpu(String k8sLabelKey, String gpuModel, Integer gpuNum) {
        Integer remainingGpuNum = getTotalGpuNum(k8sLabelKey, gpuModel) - getAllocatedGpuNum(k8sLabelKey, gpuModel);
        if (gpuNum > remainingGpuNum) {
            return LackOfResourcesEnum.LACK_OF_GPU;
        } else {
            return LackOfResourcesEnum.ADEQUATE;
        }
    }

    /**
     * 添加污点
     *
     * @param nodeName 节点名称
     * @param bizTaintList 污点
     * @return BizNode
     */
    @Override
    public BizNode taint(String nodeName, List<BizTaint> bizTaintList) {
        try {
            if (StringUtils.isEmpty(nodeName) || org.springframework.util.CollectionUtils.isEmpty(bizTaintList)) {
                return new BizNode().errorBadRequest();
            }
            Node nodeInfo = client.nodes().withName(nodeName).get();
            if (nodeInfo == null) {
                return new BizNode().error(K8sResponseEnum.NOT_FOUND.getCode(), "节点[" + nodeName + "]不存在");
            }
            List<Taint> oldTaints = nodeInfo.getSpec().getTaints();
            for (Taint taint : oldTaints) {
                if (K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY.equals(taint.getKey())) {
                    return new BizNode().error(K8sResponseEnum.EXISTS.getCode(), "节点已被占用");
                }
            }

            Node node = client.nodes().withName(nodeName).edit().editSpec().addAllToTaints(BizConvertUtils.toTaints(bizTaintList)).endSpec().done();
            return BizConvertUtils.toBizNode(node);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.schedulable error:{}", e);
            return new BizNode().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除污点
     *
     * @param nodeName 节点名称
     * @param bizTaintList 污点
     * @return BizNode
     */
    @Override
    public BizNode delTaint(String nodeName, List<BizTaint> bizTaintList) {
        try {
            if (StringUtils.isEmpty(nodeName) || org.springframework.util.CollectionUtils.isEmpty(bizTaintList)) {
                return new BizNode().errorBadRequest();
            }
            Node nodeInfo = client.nodes().withName(nodeName).get();
            if (nodeInfo == null) {
                return new BizNode().error(K8sResponseEnum.NOT_FOUND.getCode(), "节点[" + nodeName + "]不存在");
            }
            Node node = client.nodes().withName(nodeName).edit().editSpec().removeAllFromTaints(BizConvertUtils.toTaints(bizTaintList)).endSpec().done();
            return BizConvertUtils.toBizNode(node);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.delTaint error:{}", e);
            return new BizNode().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除污点
     *
     * @param nodeName 节点名称
     * @return BizNode
     */
    @Override
    public BizNode delTaint(String nodeName) {
        try {
            if (StringUtils.isEmpty(nodeName)) {
                return new BizNode().errorBadRequest();
            }
            Node nodeInfo = client.nodes().withName(nodeName).get();
            if (nodeInfo == null) {
                return new BizNode().error(K8sResponseEnum.NOT_FOUND.getCode(), "节点[" + nodeName + "]不存在");
            }

            List<Taint> taints = nodeInfo.getSpec().getTaints();
            Taint taint = new Taint();
            for (Taint obj : taints) {
                if (K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY.equals(obj.getKey())) {
                    taint = obj;
                }
            }

            Node node = client.nodes().withName(nodeName)
                    .edit()
                    .editSpec()
                    .removeFromTaints(taint)
                    .endSpec()
                    .done();
            return BizConvertUtils.toBizNode(node);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "NodeApiImpl.delTaint error:{}", e);
            return new BizNode().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }


    /**
     * 根据id获取 node资源隔离 标志
     *
     * @param isolationId
     * @return node资源隔离 标志
     */
    @Override
    public String getNodeIsolationValue(Long isolationId) {
        return StrUtil.format(K8sLabelConstants.PLATFORM_TAG_ISOLATION_VALUE, SpringContextHolder.getActiveProfile(), isolationId);
    }

    /**
     * 获取当前用户
     *
     * @return node资源隔离 标志
     */
    @Override
    public String getNodeIsolationValue() {
        return StrUtil.format(K8sLabelConstants.PLATFORM_TAG_ISOLATION_VALUE, SpringContextHolder.getActiveProfile(), userContextService.getCurUserId());
    }

    /**
     * 获取当前用户资源隔离 Toleration
     *
     * @return Toleration
     */
    @Override
    public Toleration getNodeIsolationToleration() {
        List<BizNode> nodes = getWithLabel(K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY, getNodeIsolationValue());
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return ResourceBuildUtils.buildNoScheduleEqualToleration(K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY, getNodeIsolationValue());
    }

    /**
     * 获取当前用户 资源隔离 NodeSelector
     * @return Map<String, String>
     */
    @Override
    public Map<String, String> getNodeIsolationNodeSelector() {
        Map<String, String> nodeSelector = new HashMap<>(MagicNumConstant.TWO);
        nodeSelector.put(K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY, getNodeIsolationValue());
        return nodeSelector;
    }

    /**
     * 根据userid 生成 BizTaint 列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<BizTaint> geBizTaintListByUserId(Long userId) {
        List<BizTaint> bizTaintList = new ArrayList<>();
        BizTaint bizTaint = new BizTaint();
        bizTaint.setEffect(K8sTolerationEffectEnum.NOSCHEDULE.getEffect());
        bizTaint.setKey(K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY);
        bizTaint.setValue(getNodeIsolationValue(userId));
        bizTaintList.add(bizTaint);
        return bizTaintList;
    }

    /**
     * 根据当前用户 生成 BizTaint 列表
     *
     * @return
     */
    @Override
    public List<BizTaint> geBizTaintListByUserId() {
        return geBizTaintListByUserId(userContextService.getCurUserId());
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
            if (!CollectionUtils.isEmpty(collect)) {
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
            if (!CollectionUtils.isEmpty(collect)) {
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
     * @param baseResourceBo 资源通用属性基类
     * @param nodeItems Node集合
     * @return List<Node> Node集合
     */
    private List<Node> isGpuAllocatable(BaseResourceBo baseResourceBo, List<Node> nodeItems) {
        List<Node> nodeItemResults = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        nodeItems = nodeItems.stream().filter(node -> node.getStatus().getCapacity().containsKey(baseResourceBo.getK8sLabelKey())).collect(Collectors.toList());

        List<Pod> podItems = filterRequestGpuPod(baseResourceBo.getK8sLabelKey(), baseResourceBo.getGpuModel());
        Map<String, Integer> allocatableGpu = new HashMap();

        for (Node nodeItem : nodeItems) {
            int totalGpuAmount = 0;
            int totalGpu = Integer.parseInt(nodeItem.getStatus().getCapacity().get(baseResourceBo.getK8sLabelKey()).getAmount());
            String nodeName = nodeItem.getMetadata().getName();
            List<Pod> nodePodItems = podItems.stream().filter(pod -> pod.getSpec().getNodeName().equals(nodeName)).collect(Collectors.toList());
            for (Pod pod : nodePodItems) {
                String gpuAmount = pod.getSpec().getContainers().get(0).getResources().getLimits().get(baseResourceBo.getK8sLabelKey()).getAmount();
                totalGpuAmount = totalGpuAmount + Integer.parseInt(gpuAmount);
            }
            allocatableGpu.put(nodeName, totalGpu - totalGpuAmount);

        }
        Set<String> keySet = allocatableGpu.keySet();

        keySet.forEach(key -> {
            if (allocatableGpu.get(key) >= baseResourceBo.getGpuNum()) {
                nodeNameList.add(key);
            }
        });

        for (String nodeName : nodeNameList) {
            List<Node> collect = nodeItems.stream().filter(nodeItem -> nodeItem.getMetadata().getName().equals(nodeName)).collect(Collectors.toList());
            nodeItemResults.addAll(collect);
        }
        return nodeItemResults;
    }

    /**
     * 获取申请了gpu的pod列表
     * @return
     */
    private List<Pod> filterRequestGpuPod(String K8sLabelKey, String gpuModel) {
        PodList podList = client.pods().list();
        if (CollectionUtil.isNotEmpty(podList.getItems())) {
            return podList.getItems().stream().filter(pod ->
                    pod.getSpec().getContainers().get(0).getResources().getLimits() != null &&
                            pod.getSpec().getContainers().get(0).getResources().getLimits().containsKey(K8sLabelKey) &&
                            pod.getStatus().getPhase().equals(PodPhaseEnum.RUNNING.getPhase()) &&
                            pod.getMetadata().getLabels().containsValue(gpuModel)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 查询集群已分配gpu数量
     * @return
     */
    private Integer getAllocatedGpuNum(String k8sLabelKey, String gpuModel) {
        return filterRequestGpuPod(k8sLabelKey, gpuModel).stream().mapToInt(pod ->
                pod.getSpec().getContainers().stream().mapToInt(container ->
                        Integer.valueOf(String.valueOf(container.getResources().getLimits().get(k8sLabelKey).getAmount()))).sum())
                .sum();
    }

    /**
     * 查询集群总gpu数量
     * @return
     */
    private Integer getTotalGpuNum(String k8sLabelKey, String gpuModel) {
        return listAll().stream()
                .filter(node -> !node.isUnschedulable() && node.getLabels().containsValue(gpuModel))
                .mapToInt(node -> Integer.valueOf(String.valueOf(node.getCapacity().get(k8sLabelKey).getAmount()))).sum();
    }

    /**
     * 查询节点是否存在指定的污点
     * @return
     */
    private boolean doesTaintExit(Node node, BizTaint bizTaint) {
        List<Taint> taints = node.getSpec().getTaints().stream().filter(taint ->
                StringUtils.equalsAny(taint.getKey(), bizTaint.getKey())
                        && StringUtils.equalsAny(taint.getValue(), bizTaint.getValue()))
                .collect(Collectors.toList());
        return CollectionUtil.isNotEmpty(taints);
    }
}
