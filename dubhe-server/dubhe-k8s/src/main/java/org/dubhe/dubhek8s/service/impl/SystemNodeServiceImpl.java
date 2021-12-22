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


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.dto.QueryUserK8sResourceDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.enums.SystemNodeEnum;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.MathUtils;
import org.dubhe.biz.base.utils.RegexUtil;
import org.dubhe.biz.base.utils.SpringContextHolder;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.base.vo.DataResponseBody;
import org.dubhe.biz.base.vo.GpuAllotVO;
import org.dubhe.biz.base.vo.QueryUserResourceSpecsVO;
import org.dubhe.biz.base.vo.UserAllotResourceVO;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.cloud.authconfig.service.AdminClient;
import org.dubhe.dubhek8s.domain.dto.NodeDTO;
import org.dubhe.dubhek8s.domain.dto.PodDTO;
import org.dubhe.dubhek8s.domain.vo.GpuResourceVO;
import org.dubhe.dubhek8s.domain.vo.K8sAllResourceVO;
import org.dubhe.dubhek8s.domain.vo.NamespaceVO;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.dubhe.dubhek8s.service.SystemNodeService;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.dao.K8sNodeMapper;
import org.dubhe.k8s.domain.bo.BaseResourceBo;
import org.dubhe.k8s.domain.dto.NodeInfoDTO;
import org.dubhe.k8s.domain.dto.NodeIsolationDTO;
import org.dubhe.k8s.domain.entity.K8sNode;
import org.dubhe.k8s.domain.resource.BizNode;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizTaint;
import org.dubhe.k8s.domain.vo.GpuUsageVO;
import org.dubhe.k8s.domain.vo.PtContainerMetricsVO;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.LimitsOfResourcesEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
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
    K8sNameTool k8sNameTool;

    @Autowired
    private NodeApi nodeApi;

    @Autowired
    private PodApi podApi;

    @Autowired
    private MetricsApi metricsApi;

    @Resource
    private AdminClient adminClient;

    @Autowired
    private ResourceQuotaApi resourceQuotaApi;

    @Autowired
    private SystemNamespaceService systemNamespaceService;

    @Autowired
    private K8sNodeMapper k8sNodeMapper;

    @Autowired
    private UserContextService userContextService;

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

        Map<String, List<GpuUsageVO>> gpuUsageMap = metricsApi.getNodeGpuUsage();
        /**
         * 查询pod的指标的集合
         * <podName,PtContainerMetricsVO>
         */

        Map<String, PtContainerMetricsVO> containerMetricsMap = new HashMap<>();
        for (PtContainerMetricsVO vo : ptContainerMetricsVOList) {
            if (containerMetricsMap.get(vo.getPodName()) == null) {
                containerMetricsMap.put(vo.getPodName(), vo);
            } else {
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
            obj.setGpuUsageList(gpuUsageMap.get(obj.getName()) == null ? new ArrayList<>() : gpuUsageMap.get(obj.getName()));
            nodeDtoS.add(obj);
        }

        //备注
        List<K8sNode> k8sNodeList = k8sNodeMapper.selectList(null);
        if (!CollectionUtils.isEmpty(k8sNodeList)) {
            Map<String, K8sNode> k8sNodeMap = k8sNodeList.stream().collect(Collectors.toMap(K8sNode::getName, k -> k));
            for (NodeDTO nodeDTO : nodeDtoS) {
                if (null != k8sNodeMap.get(nodeDTO.getName())) {
                    nodeDTO.setId(k8sNodeMap.get(nodeDTO.getName()).getId());
                    nodeDTO.setRemark(k8sNodeMap.get(nodeDTO.getName()).getRemark());
                }
            }
        }
        return nodeDtoS;
    }

    /**
     * 统计k8s集群各个资源的数量
     *
     * @return K8sAllResourceVO 资源总量统计响应VO
     */
    @Override
    public K8sAllResourceVO findAllResource() {
        List<BizNode> bizNodes = nodeApi.listAll();
        //统计用户资源配额
        UserAllotResourceVO userAllotTotal = adminClient.getUserAllotTotal().getData();
        //统计所有node节点的资源总量
        K8sAllResourceVO k8sAllResourceVO = toResourceVO(bizNodes, userAllotTotal, findNodes());

        k8sAllResourceVO.setCpuAllotTotal(userAllotTotal.getCpuAllotTotal());
        k8sAllResourceVO.setMemoryAllotTotal(userAllotTotal.getMemoryAllotTotal());
        k8sAllResourceVO.setGpuAllotTotal(userAllotTotal.getGpuAllotTotal());
        k8sAllResourceVO.setGpuResourceList(userAllotTotal.getGpuAllotList());

        return k8sAllResourceVO;


    }


    /**
     * 统计所有node节点的资源总量
     *
     * @param bizNodes node节点实体类
     * @return K8sAllResourceVO 资源总量统计响应VO
     */
    private K8sAllResourceVO toResourceVO(List<BizNode> bizNodes, UserAllotResourceVO userAllotTotal, List<NodeDTO> nodeDTOs) {
        K8sAllResourceVO k8sResourceVO = new K8sAllResourceVO();
        int nodeTotal = 0;
        for (BizNode node : bizNodes) {
            toGpuResourceVo(node, userAllotTotal.getGpuAllotList());
        }
        k8sResourceVO.setGpuAverageUsage(getGpuAverageUsage());

        if (CollUtil.isNotEmpty(nodeDTOs)) {
            for (NodeDTO nodeDTO : nodeDTOs) {
                k8sResourceVO.setGpuTotal(sumAmountToInt(k8sResourceVO.getGpuTotal(), Integer.valueOf(nodeDTO.getGpuCapacity()), true));
                k8sResourceVO.setGpuUsedTotal(sumAmountToInt(k8sResourceVO.getGpuUsedTotal(), Integer.valueOf(nodeDTO.getGpuUsed()), true));
                k8sResourceVO.setCpuTotal(sumAmountToInt(k8sResourceVO.getCpuTotal(), RegexUtil.tranferRegEx(nodeDTO.getTotalNodeCpu()), true));
                k8sResourceVO.setCpuUsedTotal(sumAmountToInt(k8sResourceVO.getCpuUsedTotal(), convertCpu(RegexUtil.tranferRegEx(nodeDTO.getNodeCpu()).toString()), true));
                k8sResourceVO.setMemoryTotal(sumAmountToInt(k8sResourceVO.getMemoryTotal(), convertMemory(RegexUtil.tranferRegEx(nodeDTO.getTotalNodeMemory()).toString()), true));
                k8sResourceVO.setMemoryUsedTotal(sumAmountToInt(k8sResourceVO.getMemoryUsedTotal(), convertMemory(RegexUtil.tranferRegEx(nodeDTO.getNodeMemory()).toString()), true));
                nodeTotal++;
            }
            k8sResourceVO.setNodeTotal(nodeTotal);
        }
        return k8sResourceVO;
    }

    /**
     * 对cpu进行转换
     *
     * @param cpu 转换参数
     * @return String 转换后的值
     **/
    private static Integer convertCpu(String cpu) {
        if (StringUtils.isBlank(cpu)) {
            return null;
        }
        return (int) Math.round(Long.parseLong(cpu) * MagicNumConstant.ONE_DOUBLE / MagicNumConstant.ONE_THOUSAND);
    }

    /**
     * 对内存进行转换
     *
     * @param memory 转换参数
     * @return String 转换后的值
     **/
    private static Integer convertMemory(String memory) {
        if (StringUtils.isBlank(memory)) {
            return null;
        }
        return (int) Math.round(Long.parseLong(memory) * MagicNumConstant.ONE_DOUBLE / MagicNumConstant.BINARY_TEN_EXP);
    }

    private static void toGpuResourceVo(BizNode node, List<GpuAllotVO> gpuAllotList) {
        if (CollUtil.isNotEmpty(gpuAllotList) && StrUtil.isNotEmpty(node.getLabels().get(K8sLabelConstants.NODE_GPU_MODEL_LABEL_KEY))) {
            for (GpuAllotVO gpuAllotVO : gpuAllotList) {
                if (gpuAllotVO.getGpuModel().equals(node.getLabels().get(K8sLabelConstants.NODE_GPU_MODEL_LABEL_KEY))) {
                    Integer nodeGpuNum = 0;
                    if (node.getCapacity().get(K8sParamConstants.GPU_RESOURCE_KEY) != null){
                        nodeGpuNum = Integer.valueOf(node.getCapacity().get(K8sParamConstants.GPU_RESOURCE_KEY).getAmount());
                    }
                    gpuAllotVO.setTotal(sumAmountToInt(gpuAllotVO.getTotal(), nodeGpuNum, true));
                }
            }
        }
    }

    private float getGpuAverageUsage() {
        Map<String, List<GpuUsageVO>> gpuUsageMap = metricsApi.getNodeGpuUsage();

        List<GpuUsageVO> usageList = new ArrayList<>();
        gpuUsageMap.values().forEach(usageList::addAll);
        List<String> collect = usageList.stream().map(GpuUsageVO::getUsage).collect(Collectors.toList());
        OptionalDouble average = collect.stream().mapToDouble(Double::parseDouble).average();
        return BigDecimal.valueOf(average.getAsDouble() * 100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }


    private static Integer sumAmountToInt(Integer oriArg, Integer tarArg, boolean isAdd) {
        oriArg = oriArg == null ? 0 : oriArg;
        tarArg = tarArg == null ? 0 : tarArg;
        int amount;
        if (isAdd) {
            amount = oriArg + tarArg;
        } else {
            amount = oriArg - tarArg;
        }
        return amount;
    }

    private static Double sumAmountToDouble(Double oriArg, Double tarArg, boolean isAdd) {
        oriArg = oriArg == null ? 0.0 : oriArg;
        tarArg = tarArg == null ? 0.0 : tarArg;
        Double amount;
        if (isAdd) {
            amount = oriArg + tarArg;
        } else {
            amount = oriArg - tarArg;
        }
        return amount;
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

        if (CollectionUtils.isEmpty(userIds)) {
            return nodeDTOList;
        }
        DataResponseBody<List<UserDTO>> userDTODataResponseBody = adminClient.getUserList(userIds);
        if (userDTODataResponseBody == null || CollectionUtils.isEmpty(userDTODataResponseBody.getData())) {
            return nodeDTOList;
        }
        Map<Long, UserDTO> userDTOMap = userDTODataResponseBody.getData().stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));
        nodeDTOList.forEach(nodeDTO -> {
            if (nodeDTO.getIsolationId() != null) {
                UserDTO userDTO = userDTOMap.get(nodeDTO.getIsolationId());
                if (userDTO != null) {
                    nodeDTO.setIsolation(userDTO.getUsername());
                } else {
                    nodeDTO.setIsolation(nodeDTO.getIsolationEnv() + " 环境 用户id-" + nodeDTO.getIsolationId());
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
        if (nodeIsolationDTO == null || nodeIsolationDTO.getUserId() == null || CollectionUtils.isEmpty(nodeIsolationDTO.getNodeNames())) {
            return bizNodes;
        }
        List<BizTaint> bizTaintList = nodeApi.geBizTaintListByUserId(nodeIsolationDTO.getUserId());
        for (String nodeName : nodeIsolationDTO.getNodeNames()) {
            nodeApi.addLabel(nodeName, K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY, nodeApi.getNodeIsolationValue(nodeIsolationDTO.getUserId()));
            BizNode bizNode = nodeApi.taint(nodeName, bizTaintList);
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
        if (nodeIsolationDTO == null || CollectionUtils.isEmpty(nodeIsolationDTO.getNodeNames())) {
            return bizNodes;
        }
        if (nodeIsolationDTO.getUserId() == null) {
            for (String nodeName : nodeIsolationDTO.getNodeNames()) {
                nodeApi.deleteLabel(nodeName, K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY);
                BizNode bizNode = nodeApi.delTaint(nodeName);
                bizNodes.add(bizNode);
            }
        } else {
            List<BizTaint> bizTaintList = nodeApi.geBizTaintListByUserId(nodeIsolationDTO.getUserId());
            for (String nodeName : nodeIsolationDTO.getNodeNames()) {
                nodeApi.deleteLabel(nodeName, K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY);
                BizNode bizNode = nodeApi.delTaint(nodeName, bizTaintList);
                bizNodes.add(bizNode);
            }
        }
        return bizNodes;
    }

    /**
     * 查询用户k8s可用资源
     *
     * @param queryUserK8sResources 用户k8s可用资源查询条件
     * @return List<QueryUserResourceSpecsVO> 用户k8s可用资源
     */
    @Override
    public List<QueryUserResourceSpecsVO> queryUserK8sResource(List<QueryUserK8sResourceDTO> queryUserK8sResources) {
        List<QueryUserResourceSpecsVO> queryUserResourceSpecsVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(queryUserK8sResources)) {
            return queryUserResourceSpecsVOS;
        }
        //查询该用户CPU,MEM,GPU资源
        NamespaceVO userResource = systemNamespaceService.findNamespace(queryUserK8sResources.get(0).getUserId());
        List<GpuResourceVO> gpuResourceList = new ArrayList<>();
        if (userResource != null) {
            gpuResourceList = userResource.getGpuResourceList();
        }
        List<GpuResourceVO> finalGpuResourceList = gpuResourceList;
        queryUserResourceSpecsVOS = queryUserK8sResources.stream().map(x -> {
            QueryUserResourceSpecsVO queryUserResourceSpecsVO = new QueryUserResourceSpecsVO();
            String namespace = k8sNameTool.getNamespace(x.getUserId());
            BeanUtils.copyProperties(x, queryUserResourceSpecsVO);
            LimitsOfResourcesEnum limitsOfResources;
            //判断用户剩余额度是否可用
            limitsOfResources = resourceQuotaApi.reachLimitsOfResourcesConvert(namespace,
                    x.getCpuNum() * MagicNumConstant.ONE_THOUSAND * x.getResourcesPoolNode(),
                    x.getMemNum() * x.getResourcesPoolNode(),
                    x.getGpuNum() * x.getResourcesPoolNode(), x.getK8sLabelKey());
            if (limitsOfResources != null) {
                if (LimitsOfResourcesEnum.LIMITS_OF_CPU.equals(limitsOfResources)) {
                    return queryUserResourceSpecsVO.setValid(false).setMessage("CPU不足，请先释放部分资源或联系管理员");
                }
                if (LimitsOfResourcesEnum.LIMITS_OF_MEM.equals(limitsOfResources)) {
                    return queryUserResourceSpecsVO.setValid(false).setMessage("内存不足，请先释放部分资源或联系管理员");
                }
                if (LimitsOfResourcesEnum.LIMITS_OF_GPU.equals(limitsOfResources)) {
                    return queryUserResourceSpecsVO.setValid(false).setMessage("GPU不足，请先释放部分资源或联系管理员");
                }
            }
            if (x.getResourcesPoolType() && !CollectionUtils.isEmpty(finalGpuResourceList)) {
                for (GpuResourceVO gpuResourceVO : finalGpuResourceList) {
                    //GPU型号校验
                    boolean b = x.getGpuModel().equals(gpuResourceVO.getGpuModel()) && x.getK8sLabelKey().equals(gpuResourceVO.getK8sLabelKey());
                    //判断用户GPU剩余额度是否可用
                    boolean b1 = (x.getGpuNum() * x.getResourcesPoolNode()) > (gpuResourceVO.getHardGpu() - gpuResourceVO.getUsedGpu());
                    if (b && b1) {
                        return queryUserResourceSpecsVO.setValid(false).setMessage("GPU不足，请先释放部分资源或联系管理员");
                    }
                }
            }
            //判断k8s集群CPU,MEM,GPU资源是否可用
            LackOfResourcesEnum lack = null;
            if (x.getResourcesPoolNode() == MagicNumConstant.ONE) {
                //单节点
                BaseResourceBo bo = new BaseResourceBo();
                if (x.getResourcesPoolType()) {
                    bo.setUseGpu(true).setGpuModel(x.getGpuModel()).setK8sLabelKey(x.getK8sLabelKey()).setGpuNum(x.getGpuNum());
                }
                lack = nodeApi.isAllocatableConvert(namespace, x.getCpuNum() * MagicNumConstant.ONE_THOUSAND, x.getMemNum(),
                        bo.getUseGpu(), bo.getK8sLabelKey(), bo.getGpuModel(), bo.getGpuNum());
            } else {
                //多节点
                if (x.getResourcesPoolType()) {
                    lack = nodeApi.isOutOfTotalAllocatableGpu(x.getK8sLabelKey(), x.getGpuModel(), x.getGpuNum() * x.getResourcesPoolNode());
                }
                if (lack == null) {
                    return queryUserResourceSpecsVO.setValid(true);
                }
            }
            if (LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                queryUserResourceSpecsVO.setValid(true);
            }
            if (LackOfResourcesEnum.LACK_OF_CPU.equals(lack)) {
                queryUserResourceSpecsVO.setValid(false).setMessage("CPU不足，请先释放部分资源或联系管理员");
            }
            if (LackOfResourcesEnum.LACK_OF_MEM.equals(lack)) {
                queryUserResourceSpecsVO.setValid(false).setMessage("内存不足，请先释放部分资源或联系管理员");
            }
            if (LackOfResourcesEnum.LACK_OF_GPU.equals(lack)) {
                queryUserResourceSpecsVO.setValid(false).setMessage("GPU不足，请先释放部分资源或联系管理员");
            }
            if (LackOfResourcesEnum.LACK_OF_NODE.equals(lack)) {
                queryUserResourceSpecsVO.setValid(false).setMessage("没有可用的节点，请先释放部分资源或联系管理员");
            }

            return queryUserResourceSpecsVO;
        }).collect(Collectors.toList());
        return queryUserResourceSpecsVOS;
    }

    /**
     * k8s节点信息编辑
     *
     * @param nodeInfoDTO k8s节点信息DTO
     * @return K8sNode
     */
    @Override
    public K8sNode editNodeInfo(NodeInfoDTO nodeInfoDTO) {
        LogUtil.info(LogEnum.BIZ_K8S, "editNodeInfo nodeInfoDTO:{}", nodeInfoDTO);
        if (null == nodeInfoDTO || StringUtils.isEmpty(nodeInfoDTO.getName())) {
            return null;
        }
        LambdaQueryWrapper<K8sNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(K8sNode::getName, nodeInfoDTO.getName());
        if (null != nodeInfoDTO.getId()) {
            wrapper.eq(K8sNode::getId, nodeInfoDTO.getId());
        }
        List<K8sNode> k8sNodeList = k8sNodeMapper.selectList(wrapper);

        K8sNode res = new K8sNode(nodeInfoDTO.getName(), nodeInfoDTO.getRemark());
        UserContext user = userContextService.getCurUser();
        if (CollectionUtils.isEmpty(k8sNodeList)) {
            if (user != null) {
                res.setCreateUserId(user.getId());
                res.setUpdateUserId(user.getId());
            }
            res.setCreateTime(new Timestamp(new java.util.Date().getTime()));
            res.setUpdateTime(new Timestamp(new java.util.Date().getTime()));
            k8sNodeMapper.insert(res);
        } else {
            res = k8sNodeList.get(0);
            if (user != null) {
                res.setCreateUserId(user.getId());
            }
            res.setRemark(nodeInfoDTO.getRemark());
            res.setUpdateTime(new Timestamp(new java.util.Date().getTime()));
            k8sNodeMapper.updateById(res);
        }
        return res;
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
            fillIsolationInfo(nodeDTO, node);
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
    private static void fillIsolationInfo(NodeDTO nodeDTO, BizNode node) {
        List<BizTaint> taints = node.getTaints();
        if (CollectionUtils.isEmpty(taints)) {
            return;
        }
        for (BizTaint taint : taints) {
            String isolation = taint.getKey();
            if (K8sLabelConstants.PLATFORM_TAG_ISOLATION_KEY.equals(isolation)) {
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
    private PodDTO toPodDTO(PtContainerMetricsVO podMetrics, BizPod pod) {
        PodDTO podDTO = new PodDTO();
        podDTO.setPodName(pod.getName());
        if (podMetrics != null) {
            podDTO.setPodCpu(transfer(podMetrics.getCpuUsageAmount()) + K8sParamConstants.CPU_UNIT);
            podDTO.setPodMemory(transferMemory(podMetrics.getMemoryUsageAmount()) + K8sParamConstants.MEM_UNIT);
        } else {
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
    private static Integer transferMemory(String memory) {
        if (StringUtils.isBlank(memory)) {
            return null;
        }
        return (int) Math.floor(Integer.valueOf(memory) / MagicNumConstant.BINARY_TEN_EXP);
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
