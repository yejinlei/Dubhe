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
 * @description ??????????????????????????? service ??????????????????
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
     * ??????????????????????????????
     *
     * @param userId ?????? ID
     * @return NamespaceVO ???????????? VO
     */
    @Override
    public NamespaceVO findNamespace(Long userId) {
        NamespaceVO namespaceVO = new NamespaceVO();
        String namespaceStr = k8sNameTool.generateNamespace(userId);
        Set<TaskResVO> taskResVOS = new HashSet<>();
        List<TaskResVO> taskResVOList = new ArrayList<>();


        // ???????????????????????????????????????????????? Pod
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

            // ?????? Pod?????????????????? Pod ??????????????????
            for (BizPod bizPod : sortedBizPods) {
                int podCpuAmount = 0;
                int podMemoryAmount = 0;
                int podGpuAmount = 0;
                PodResVO podResVO = new PodResVO();

                // ?????? Pod ???????????????Pod ??????????????????????????????????????? Pod ??????????????????
                for (BizContainer container : bizPod.getContainers()) {
                    Map<String, BizQuantity> limits = container.getLimits();
                    if (limits == null){
                        continue;
                    }
                    // ?????? CPU ????????????
                    Integer cpuAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_CPU_KEY);
                    if (cpuAmount != null){
                        podCpuAmount += cpuAmount;
                    }
                    // ????????????????????????
                    Integer memoryAmount = getResourceAmount(limits, K8sParamConstants.QUANTITY_MEMORY_KEY);
                    if (memoryAmount != null){
                        podMemoryAmount += memoryAmount;
                    }
                    // ?????? GPU ????????????
                    Integer gpuAmount = getResourceAmount(limits, K8sParamConstants.GPU_RESOURCE_KEY);
                    if (gpuAmount != null){
                        podGpuAmount += gpuAmount;
                    }
                }

                // ??????VO
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

        // ????????? namespace ??????????????????,??????????????????????????? Scope ???????????????
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

        // ???????????????????????????
        Map<String, BizQuantity> hard = bizResourceQuota.getHard();

        // ??????????????????????????????
        Map<String, BizQuantity> used = bizResourceQuota.getUsed();

        // ?????? VO
        namespaceVO.setHardCpu(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                .setHardMemory(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                .setHardGpu(getResourceAmount(hard, K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY))
                .setUsedCpu(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_CPU_LIMITS_KEY))
                .setUsedMemory(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_MEMORY_LIMITS_KEY))
                .setUsedGpu(getResourceAmount(used, K8sParamConstants.RESOURCE_QUOTA_GPU_LIMITS_KEY));
        return namespaceVO;
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param key ?????????Key??????????????????????????????CPU/??????/GPU???
     * @param quantityMap  ????????????????????? Map
     * @return NamespaceVO ???????????? VO
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
     * ??????????????? Pod ??????
     *
     * @param bizPods ?????? Pod ????????????
     * @return HashMap<String, List<BizPod>> ?????????????????? Pod ??????
     */
    private HashMap<String, List<BizPod>> sortBizPod(List<BizPod> bizPods, Set<TaskResVO> taskResVOS){
        HashMap<String, List<BizPod>> bizPodsMap = new HashMap();
        for (BizPod pod : bizPods) {
            String businessLabel = pod.getBusinessLabel();
            String taskIdentifyLabel = pod.getTaskIdentifyLabel();
            if (StringUtils.isNotEmpty(businessLabel) && StringUtils.isNotEmpty(taskIdentifyLabel) && CollectionUtil.isNotEmpty(redisUtils.hmget(pod.getTaskIdentifyLabel()))){
                // ???redis ????????????????????????
                Map<Object, Object> taskMap = redisUtils.hmget(pod.getTaskIdentifyLabel());
                Long taskId = (Long) taskMap.get(CACHE_TASK_ID);
                String taskName = (String) taskMap.get(CACHE_TASK_NAME);
                // ??? businessLabel + taskId + taskName ???????????????????????????
                String sortedKey = businessLabel + taskId + taskName;
                List<BizPod> sortedBizPods = bizPodsMap.get(sortedKey);
                sortedBizPods = CollectionUtil.isEmpty(sortedBizPods) ? new ArrayList<BizPod>() : sortedBizPods;
                sortedBizPods.add(pod);
                bizPodsMap.put(sortedKey, sortedBizPods);
                // ?????? TaskResVO
                TaskResVO taskResVO = new TaskResVO().setTaskName(taskName).setTaskId(taskId).setBusinessLabel(businessLabel);
                taskResVOS.add(taskResVO);
            } else {
                businessLabel = businessLabel == null ? "" : businessLabel;
                // ???????????????????????????????????? UNKNOW ??????
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
