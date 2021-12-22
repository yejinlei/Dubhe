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

import cn.hutool.core.collection.CollectionUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.dto.NamespaceDeleteDTO;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.dubhek8s.domain.vo.GpuResourceVO;
import org.dubhe.dubhek8s.domain.vo.NamespaceVO;
import org.dubhe.dubhek8s.domain.vo.PodResVO;
import org.dubhe.dubhek8s.domain.vo.TaskResVO;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.dubhe.k8s.api.NamespaceApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.dao.K8sTaskIdentifyMapper;
import org.dubhe.k8s.domain.entity.K8sGpuConfig;
import org.dubhe.k8s.domain.entity.K8sTaskIdentify;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizResourceQuota;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.service.K8sGpuConfigService;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.dubhe.biz.base.constant.StringConstant.CACHE_TASK_ID;
import static org.dubhe.biz.base.constant.StringConstant.CACHE_TASK_NAME;
import static org.dubhe.biz.base.constant.SymbolConstant.BLANK;

/**
 * @description 查询命名空间状态的 service 层接口实现类
 * @date 2021-7-14
 */
@Service
public class SystemNamespaceServiceImpl implements SystemNamespaceService {
    @Autowired
    K8sNameTool k8sNameTool;

    @Autowired
    ResourceQuotaApi resourceQuotaApi;

    @Autowired
    PodApi podApi;

    @Autowired
    NodeApi nodeApi;

    @Autowired
    ResourceCache resourceCache;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    private K8sGpuConfigService k8sGpuConfigService;

    @Autowired
    private K8sTaskIdentifyMapper k8sTaskIdentifyMapper;

    @Autowired
    private NamespaceApi namespaceApi;

    private static final String UNKNOW = "unknown";

    /**
     * 查询命名空间资源信息
     *
     * @param userId 用户 ID
     * @return NamespaceVO 命名空间 VO
     */
    @Override
    public NamespaceVO findNamespace(Long userId) {
        NamespaceVO namespaceVO = new NamespaceVO();
        String namespaceStr = k8sNameTool.generateNamespace(userId);
        Set<TaskResVO> taskResVOS = new HashSet<>();
        List<TaskResVO> taskResVOList = new ArrayList<>();
        Integer hardGpuNum = 0;
        Integer usedGpuNum = 0;
        try {
            //获取用户的资源配额
            List<K8sGpuConfig> gpuConfigList = k8sGpuConfigService.findGpuConfig(namespaceStr);

            List<K8sGpuConfig> k8sLabelKeyList = gpuConfigList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(gpuconfig -> gpuconfig.getK8sLabelKey()))), ArrayList::new));
            // 获取该命名空间下的所有占用资源的 Pod
            List<BizPod> bizPodList = podApi.getWithNamespace(namespaceStr).parallelStream()
                    .filter(obj -> !PodPhaseEnum.SUCCEEDED.getPhase().equals(obj.getPhase())).collect(Collectors.toList());
            HashMap<String, List<BizPod>> sortedBizPodsMap = sortBizPod(bizPodList, taskResVOS);
            Set<String> keys = sortedBizPodsMap.keySet();
            for (String key : keys) {
                List<PodResVO> podResVOS = new ArrayList<>();
                List<TaskResVO> sortedTaskResVOS = taskResVOS.stream().filter(taskResVO ->
                        key.equals(taskResVO.getBusinessLabel() + taskResVO.getTaskId() + taskResVO.getTaskName())).collect(Collectors.toList());
                TaskResVO taskResVO = sortedTaskResVOS.get(0);
                List<BizPod> sortedBizPods = sortedBizPodsMap.get(key);

                // 遍历 Pod，得到每一个 Pod 资源占用信息
                for (BizPod bizPod : sortedBizPods) {
                    int podCpuAmount = 0;
                    int podMemoryAmount = 0;
                    int podGpuAmount = 0;
                    PodResVO podResVO = new PodResVO();

                    // 遍历 Pod 中的容器，Pod 中容器资源占用之和就是一个 Pod 的资源占用量
                    for (BizContainer container : bizPod.getContainers()) {
                        Map<String, BizQuantity> limits = container.getLimits();
                        if (limits == null) {
                            continue;
                        }
                        // 获取 CPU 资源占用
                        Integer cpuAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_CPU_KEY);
                        if (cpuAmount != null) {
                            podCpuAmount += cpuAmount;
                        }
                        // 获取内存资源占用
                        Integer memoryAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_MEMORY_KEY);
                        if (memoryAmount != null) {
                            podMemoryAmount += memoryAmount;
                        }
                        // 获取 GPU 资源占用

                        for (K8sGpuConfig gpuConfig : k8sLabelKeyList) {
                            Integer gpuAmount = getResourceAmount(limits, gpuConfig.getK8sLabelKey());
                            if (gpuAmount != null) {
                                podGpuAmount += gpuAmount;
                            }
                        }

                    }

                    //获取当前pod运行节点的GPU型号
                    String nodeName = bizPod.getNodeName();
                    String gpuModel = BLANK;
                    if (StringUtils.isNotEmpty(nodeName)) {
                        gpuModel = nodeApi.get(nodeName).getLabels().get(K8sLabelConstants.NODE_GPU_MODEL_LABEL_KEY);
                    }

                    // 封装VO
                    podResVO.setPodName(bizPod.getName())
                            .setPodCpu(podCpuAmount)
                            .setPodMemory(podMemoryAmount)
                            .setPodCard(podGpuAmount)
                            .setStatus(bizPod.getPhase())
                            .setGpuModel(gpuModel);
                    if (CollectionUtil.isNotEmpty(bizPod.getContainerStatuses())
                            && null != bizPod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting()) {
                        podResVO.setStatus(bizPod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting().getReason());
                    }
                    podResVOS.add(podResVO);

                }
                taskResVO = taskResVO.setPodResVOS(podResVOS);

                taskResVOList.add(taskResVO);
            }


            namespaceVO.setTasks(taskResVOList);

            List<GpuResourceVO> gpuResourceVOList = new ArrayList<>();
            for (K8sGpuConfig gpuConfig : gpuConfigList) {
                Integer usedGpu = 0;
                GpuResourceVO gpuResourceVO = new GpuResourceVO();
                for (TaskResVO taskResVO : taskResVOList) {
                    for (PodResVO podResVO : taskResVO.getPodResVOS()) {
                        if (gpuConfig.getGpuModel().equals(podResVO.getGpuModel())) {
                            usedGpu += podResVO.getPodCard();
                        }
                    }
                }
                gpuResourceVO.setGpuModel(gpuConfig.getGpuModel())
                        .setK8sLabelKey(gpuConfig.getK8sLabelKey())
                        .setHardGpu(gpuConfig.getGpuLimit())
                        .setUsedGpu(usedGpu);
                hardGpuNum += gpuConfig.getGpuLimit();
                usedGpuNum += usedGpu;
                gpuResourceVOList.add(gpuResourceVO);
            }


            // 查询该 namespace 下的资源配额,过滤得到无指定特定 Scope 的资源配额
            List<BizResourceQuota> resourceQuotas = resourceQuotaApi.list(namespaceStr).stream().filter(bizResourceQuota ->
                    namespaceStr.equals(bizResourceQuota.getName()) || CollectionUtils.isEmpty(bizResourceQuota.getMatchExpressions())).collect(Collectors.toList());

            if (CollectionUtil.isEmpty(resourceQuotas)) {
                return null;
            }

            BizResourceQuota bizResourceQuota = resourceQuotas.get(0);

            // 获取资源配额总资源
            Map<String, BizQuantity> hard = bizResourceQuota.getHard();

            // 获取资源配额已用资源
            Map<String, BizQuantity> used = bizResourceQuota.getUsed();

            // 封装 VO
            namespaceVO.setHardCpu(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                    .setHardMemory(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                    .setHardGpu(hardGpuNum)
                    .setUsedCpu(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                    .setUsedMemory(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                    .setUsedGpu(usedGpuNum)
                    .setGpuResourceList(gpuResourceVOList);
            return namespaceVO;
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "SystemNamespaceServiceImpl.findNamespace error, param:[userId]={}, error:{}", userId, e);
            return namespaceVO;
        }
    }

    /**
     * 查询各资源（配额的或已用的或总共的）数量
     *
     * @param key 不同的Key代表不同的资源信息（CPU/内存/GPU）
     * @param quantityMap  存放资源信息的 Map
     * @return NamespaceVO 命名空间 VO
     */
    private Integer getResourceAmount(Map<String, BizQuantity> quantityMap, String key) {
        BizQuantity quantity = quantityMap.get(key);
        if (quantity != null) {
            if ("Gi".equals(quantity.getFormat())) {
                return Integer.valueOf(quantity.getAmount()) * 1024;
            } else if ("Ti".equals(quantity.getFormat())) {
                return Integer.valueOf(quantity.getAmount()) * 1024 * 1024;
            } else if ("m".equals(quantity.getFormat())) {
                return Integer.valueOf(quantity.getAmount()) / 1000;
            }
            return Integer.valueOf(quantity.getAmount());
        }
        return null;
    }

    /**
     * 按照任务对 Pod 分类
     *
     * @param bizPods 业务 Pod 对象集合
     * @return HashMap<String, List < BizPod>> 分类后的业务 Pod 对象
     */
    private HashMap<String, List<BizPod>> sortBizPod(List<BizPod> bizPods, Set<TaskResVO> taskResVOS) {
        HashMap<String, List<BizPod>> bizPodsMap = new HashMap();
        for (BizPod pod : bizPods) {
            String businessLabel = pod.getBusinessLabel();
            String taskIdentifyLabel = pod.getTaskIdentifyLabel();
            if (StringUtils.isNotEmpty(businessLabel) && StringUtils.isNotEmpty(taskIdentifyLabel)) {
                Long taskId = null;
                String taskName = null;
                if (CollectionUtil.isNotEmpty(redisUtils.hmget(pod.getTaskIdentifyLabel()))) {
                    // 从redis 获取任务缓存信息
                    Map<Object, Object> taskMap = redisUtils.hmget(pod.getTaskIdentifyLabel());
                    taskId = (Long) taskMap.get(CACHE_TASK_ID);
                    taskName = (String) taskMap.get(CACHE_TASK_NAME);
                } else if (k8sTaskIdentifyMapper.selectById(taskIdentifyLabel) != null) {
                    // 从数据库中 获取任务信息
                    K8sTaskIdentify taskIdentify = k8sTaskIdentifyMapper.selectById(taskIdentifyLabel);
                    taskId = taskIdentify.getTaskId();
                    taskName = taskIdentify.getTaskName();
                }

                // 以 businessLabel + taskId + taskName 为键，标识一个任务
                String sortedKey = businessLabel + taskId + taskName;
                List<BizPod> sortedBizPods = bizPodsMap.get(sortedKey);
                sortedBizPods = CollectionUtil.isEmpty(sortedBizPods) ? new ArrayList<BizPod>() : sortedBizPods;
                sortedBizPods.add(pod);
                bizPodsMap.put(sortedKey, sortedBizPods);
                // 封装 TaskResVO
                TaskResVO taskResVO = new TaskResVO().setTaskName(taskName).setTaskId(taskId).setBusinessLabel(businessLabel);
                taskResVOS.add(taskResVO);
            }
        }
        return bizPodsMap;
    }

    /**
     *  删除用户namespace
     * @param namespaceDeleteDTO 用户DTO
     */
    @Override
    public void deleteNamespace(NamespaceDeleteDTO namespaceDeleteDTO) {
        Set<Long> ids = namespaceDeleteDTO.getIds();
        if (CollectionUtil.isNotEmpty(ids)) {
            //删除k8s资源配置
            List<String> namespaces = ids.stream().map(x -> k8sNameTool.generateNamespace(x)).collect(Collectors.toList());
            k8sGpuConfigService.delete(namespaces);
            //删除用户namespace
            for (Long id : ids) {
                namespaceApi.delete(k8sNameTool.generateNamespace(id));
            }
        }

    }

}
