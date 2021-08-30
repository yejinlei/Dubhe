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
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.dubhek8s.domain.vo.PodResVO;
import org.dubhe.dubhek8s.domain.vo.TaskResVO;
import org.dubhe.dubhek8s.service.SystemNamespaceService;
import org.dubhe.dubhek8s.domain.vo.NamespaceVO;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.resource.BizResourceQuota;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.K8sNameTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dubhe.biz.base.constant.StringConstant.CACHE_TASK_ID;
import static org.dubhe.biz.base.constant.StringConstant.CACHE_TASK_NAME;

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
    ResourceCache resourceCache;
    
    @Autowired
    RedisUtils redisUtils;

    @Value("${user.config.cpu-limit}")
    private Integer cpuLimit;

    @Value("${user.config.memory-limit}")
    private Integer memoryLimit;

    @Value("${user.config.gpu-limit}")
    private Integer gpuLimit;

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
                    if (limits == null){
                        continue;
                    }
                    // 获取 CPU 资源占用
                    Integer cpuAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_CPU_KEY);
                    if (cpuAmount != null){
                        podCpuAmount += cpuAmount;
                    }
                    // 获取内存资源占用
                    Integer memoryAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_MEMORY_KEY);
                    if (memoryAmount != null){
                        podMemoryAmount += memoryAmount;
                    }
                    // 获取 GPU 资源占用
                    Integer gpuAmount = getResourceAmount(limits, K8sParamConstants.GPU_RESOURCE_KEY);
                    if (gpuAmount != null){
                        podGpuAmount += gpuAmount;
                    }
                }

                // 封装VO
                podResVO.setPodName(bizPod.getName())
                        .setPodCpu(podCpuAmount)
                        .setPodMemory(podMemoryAmount)
                        .setPodCard(podGpuAmount)
                        .setStatus(bizPod.getPhase());
                if (CollectionUtil.isNotEmpty(bizPod.getContainerStatuses())
                        && null != bizPod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting()){
                    podResVO.setStatus(bizPod.getContainerStatuses().get(MagicNumConstant.ZERO).getWaiting().getReason());
                }
                podResVOS.add(podResVO);
            }
            taskResVO = taskResVO.setPodResVOS(podResVOS);
            taskResVOList.add(taskResVO);
        }


        namespaceVO.setTasks(taskResVOList);

        // 查询该 namespace 下的资源配额,过滤得到无指定特定 Scope 的资源配额
        List<BizResourceQuota> resourceQuotas = resourceQuotaApi.list(namespaceStr).stream().filter(bizResourceQuota ->
                namespaceStr.equals(bizResourceQuota.getName()) || CollectionUtils.isEmpty(bizResourceQuota.getMatchExpressions())).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(resourceQuotas)){
            namespaceVO.setHardCpu(cpuLimit)
                    .setHardMemory(memoryLimit * 1024)
                    .setHardGpu(gpuLimit)
                    .setUsedCpu(0)
                    .setUsedMemory(0)
                    .setUsedGpu(0);
            return namespaceVO;
        }

        BizResourceQuota bizResourceQuota = resourceQuotas.get(0);

        // 获取资源配额总资源
        Map<String, BizQuantity> hard = bizResourceQuota.getHard();

        // 获取资源配额已用资源
        Map<String, BizQuantity> used = bizResourceQuota.getUsed();

        // 封装 VO
        namespaceVO.setHardCpu(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                .setHardMemory(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                .setHardGpu(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY))
                .setUsedCpu(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                .setUsedMemory(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                .setUsedGpu(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY));
        return namespaceVO;
    }

    /**
     * 查询各资源（配额的或已用的或总共的）数量
     *
     * @param key 不同的Key代表不同的资源信息（CPU/内存/GPU）
     * @param quantityMap  存放资源信息的 Map
     * @return NamespaceVO 命名空间 VO
     */
    private Integer getResourceAmount(Map<String, BizQuantity> quantityMap, String key){
        BizQuantity quantity = quantityMap.get(key);
        if (quantity != null){
            if ("Gi".equals(quantity.getFormat())){
                return Integer.valueOf(quantity.getAmount()) * 1024;
            } else if ("Ti".equals(quantity.getFormat())){
                return Integer.valueOf(quantity.getAmount()) * 1024 * 1024;
            } else if ("m".equals(quantity.getFormat())){
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
     * @return HashMap<String, List<BizPod>> 分类后的业务 Pod 对象
     */
    private HashMap<String, List<BizPod>> sortBizPod(List<BizPod> bizPods, Set<TaskResVO> taskResVOS){
        HashMap<String, List<BizPod>> bizPodsMap = new HashMap();
        for (BizPod pod : bizPods) {
            String businessLabel = pod.getBusinessLabel();
            String taskIdentifyLabel = pod.getTaskIdentifyLabel();
            if (StringUtils.isNotEmpty(businessLabel) && StringUtils.isNotEmpty(taskIdentifyLabel) && CollectionUtil.isNotEmpty(redisUtils.hmget(pod.getTaskIdentifyLabel()))){
                // 从redis 获取任务缓存信息
                Map<Object, Object> taskMap = redisUtils.hmget(pod.getTaskIdentifyLabel());
                Long taskId = (Long) taskMap.get(CACHE_TASK_ID);
                String taskName = (String) taskMap.get(CACHE_TASK_NAME);
                // 以 businessLabel + taskId + taskName 为键，标识一个任务
                String sortedKey = businessLabel + taskId + taskName;
                List<BizPod> sortedBizPods = bizPodsMap.get(sortedKey);
                sortedBizPods = CollectionUtil.isEmpty(sortedBizPods) ? new ArrayList<BizPod>() : sortedBizPods;
                sortedBizPods.add(pod);
                bizPodsMap.put(sortedKey, sortedBizPods);
                // 封装 TaskResVO
                TaskResVO taskResVO = new TaskResVO().setTaskName(taskName).setTaskId(taskId).setBusinessLabel(businessLabel);
                taskResVOS.add(taskResVO);
            } else {
                businessLabel = businessLabel == null ? "" : businessLabel;
                // 缓存不存在任务处理，添加 UNKNOW 分类
                String sortedKey = businessLabel + 0L + UNKNOW;
                List<BizPod> sortedBizPods = bizPodsMap.get(sortedKey);
                sortedBizPods = CollectionUtil.isEmpty(sortedBizPods) ? new ArrayList<BizPod>() : sortedBizPods;
                sortedBizPods.add(pod);
                bizPodsMap.put(sortedKey, sortedBizPods);
                TaskResVO taskResVO = new TaskResVO().setTaskName(UNKNOW).setTaskId(0L).setBusinessLabel(businessLabel);
                taskResVOS.add(taskResVO);
            }
        }
        return bizPodsMap;
    }

}
