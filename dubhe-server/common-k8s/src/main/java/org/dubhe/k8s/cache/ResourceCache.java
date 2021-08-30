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

package org.dubhe.k8s.cache;

import cn.hutool.core.collection.CollectionUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.redis.utils.RedisUtils;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.entity.K8sResource;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.service.K8sResourceService;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description k8s资源缓存
 * @date 2020-05-20
 */
public class ResourceCache {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private PodApi podApi;
    @Autowired
    private K8sResourceService k8sResourceService;

    /**podNmae/resourceName缓存失效时间 单位秒**/
    private static final Long TIME_OUT = 7*24*3600L;
    /**缓存击穿前缀**/
    @Value("K8sClient:Pod:"+"${spring.profiles.active}_cache_breakdown_")
    private String cacheBreakdownPrefix;
    /**缓存穿透标记过期时间**/
    private static final Integer CACHE_BREAKDOWN = 30;

    @Value("K8sClient:Pod:"+"${spring.profiles.active}_k8s_pod_resourcename_")
    private String resourceNamePrefix;

    @Value("K8sClient:Pod:"+"${spring.profiles.active}_k8s_pod_name_")
    private String podNamePrefix;

    /**
     * 设置资源名称到 pod名称的缓存
     *
     * @param resourceName 资源名称
     * @param podName Pod名称
     * @return boolean true 缓存 false 不缓存
     */
    public boolean cachePod(String resourceName,String podName){
        try{
            Boolean success = redisUtils.zSet(resourceNamePrefix +resourceName,TIME_OUT+ThreadLocalRandom.current().nextLong(MagicNumConstant.ZERO,MagicNumConstant.ONE_HUNDRED),podName);
            if (success){
                return redisUtils.set(podNamePrefix +podName,resourceName,TIME_OUT+ThreadLocalRandom.current().nextLong(MagicNumConstant.ZERO,MagicNumConstant.ONE_HUNDRED));
            }
            return false;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Cache exception, resourceName: {}, podName: {}, exception information: {}",resourceName,podName,e);
            return false;
        }
    }

    /**
     * 设置资源名称到 pod名称的缓存
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return boolean true 缓存 false 不缓存
     */
    public boolean cachePods(String namespace,String resourceName){
        try{
            List<BizPod> podList = podApi.getListByResourceName(namespace,resourceName);
            if (CollectionUtil.isNotEmpty(podList)){
                podList.forEach(pod->{
                    cachePod(resourceName,pod.getName());
                    k8sResourceService.create(pod);
                });
            }
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Cache exception, namespace: {}, resourceName: {}, exception information: {}",namespace,resourceName,e);
            return false;
        }
    }

    /**
     * 从缓存中取 resourceName对应的podName
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return Set<String> podName的集合
     */
    public Set<String> getPodNameByResourceName(String namespace,String resourceName){
        try{
            /**缓存穿透**/
            if (redisUtils.get(cacheBreakdownPrefix +resourceName) != null){
                return null;
            }
            Set<String> set = (Set) redisUtils.zGet(resourceNamePrefix +resourceName);
            if (CollectionUtil.isEmpty(set) && StringUtils.isNotEmpty(namespace)){
                List<BizPod> bizPods = podApi.getListByResourceName(namespace,resourceName);
                Set<String> finalSet = new HashSet<>(MagicNumConstant.ONE);
                if(CollectionUtil.isNotEmpty(bizPods)){
                    bizPods.forEach(obj-> {
                        finalSet.add(obj.getName());
                        cachePod(resourceName,obj.getName());
                    });
                }else {
                    List<K8sResource> k8sResourceList = k8sResourceService.selectByResourceName(K8sKindEnum.POD.getKind(),namespace,resourceName);
                    if (CollectionUtil.isNotEmpty(k8sResourceList)){
                        k8sResourceList.forEach(obj->{
                            finalSet.add(obj.getName());
                            cachePod(resourceName,obj.getName());
                        });
                    }else {
                        /**设置缓存穿透标记**/
                        redisUtils.set(cacheBreakdownPrefix +resourceName, cacheBreakdownPrefix +resourceName,CACHE_BREAKDOWN);
                    }
                }
                return finalSet;
            }
            return set;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Query cache exception, namespace: {}, resourceName: {}, exception information: {}",namespace,resourceName,e);
            return new HashSet<>();
        }
    }

    /**
     * 从缓存中取 podName对应的resourceName
     *
     * @param namespace 命名空间
     * @param podName Pod的名称
     * @return String resourceName的名称
     */
    public String getResourceNameByPodName(String namespace,String podName){
        try{
            /**缓存穿透**/
            if (redisUtils.get(cacheBreakdownPrefix +podName) != null){
                return null;
            }
            String resourceName = (String) redisUtils.get(podNamePrefix +podName);
            if (StringUtils.isEmpty(resourceName) && StringUtils.isNotEmpty(namespace)){
                BizPod pod = podApi.get(namespace,podName);
                if (pod != null){
                    resourceName = pod.getLabels().get(K8sLabelConstants.BASE_TAG_SOURCE);
                    cachePod(resourceName,podName);
                    return resourceName;
                }else {
                    List<K8sResource> k8sResourceList = k8sResourceService.selectByName(K8sKindEnum.POD.getKind(),namespace,podName);
                    if (CollectionUtil.isNotEmpty(k8sResourceList)){
                        resourceName = k8sResourceList.get(0).getResourceName();
                        cachePod(resourceName,podName);
                    }else {
                        redisUtils.set(cacheBreakdownPrefix +podName, cacheBreakdownPrefix +podName,CACHE_BREAKDOWN);
                    }
                    return resourceName;
                }
            }
            return resourceName;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Query cache exception, namespace: {}, podName: {}, exception information: {}",namespace,podName,e);
            return null;
        }
    }

    /**
     * 查询该 podName 缓存是否存在
     *
     * @param podName Pod的名称
     * @return boolean true 存在 false 不存在
     */
    public boolean isPodNameCached(String podName){
        String resourceName = (String) redisUtils.get(podNamePrefix +podName);
        return StringUtils.isNotEmpty(resourceName);
    }

    /**
     * 删除pod名称缓存
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return boolean true 删除成功 false 删除失败
     */
    public boolean deletePodCacheByResourceName(String namespace, String resourceName){
        try{
            if (StringUtils.isNotEmpty(namespace) && StringUtils.isNotEmpty(resourceName)){
                Set<String> podNameSet = (Set) redisUtils.zGet(resourceNamePrefix +resourceName);
                redisUtils.del(resourceNamePrefix +resourceName);
                if (!CollectionUtils.isEmpty(podNameSet)){
                    podNameSet.forEach(podName-> redisUtils.del(podNamePrefix +podName));
                }
                k8sResourceService.deleteByResourceName(K8sKindEnum.POD.getKind(),namespace,resourceName);
            }
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Delete cache exception, namespace: {}, resourceName: {}, exception information: {}",namespace,resourceName,e);
            return false;
        }
    }

    /**
     * 删除pod名称缓存
     *
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return boolean true 删除成功 false删除失败
     */
    public boolean deletePodCacheByPodName(String namespace, String podName){
        try {
            if (StringUtils.isNotEmpty(namespace) && StringUtils.isNotEmpty(podName)){
                String resourceName = (String) redisUtils.get(podNamePrefix +podName);
                redisUtils.del(podNamePrefix + podName);
                if (StringUtils.isNotEmpty(resourceName)){
                    redisUtils.del(resourceNamePrefix +resourceName);
                }
                k8sResourceService.deleteByName(K8sKindEnum.POD.getKind(),namespace,podName);
            }
            return true;
        }catch (Exception e){
            LogUtil.error(LogEnum.BIZ_K8S,"Delete cache exception, namespace: {}, podName: {}, exception information: {}",namespace,podName,e);
            return false;
        }
    }

    /**
     * 添加任务身份标识缓存
     *
     * @param taskIdentify 任务身份标识
     * @param taskId 任务 ID
     * @param taskName 任务名称
     * @param taskIdPrefix 任务 ID 前缀
     * @return boolean true 添加成功 false添加失败
     */
    public boolean addTaskCache(String taskIdentify, Long taskId, String taskName, String taskIdPrefix){
        return redisUtils.hmset(taskIdentify, new HashMap<String, Object>(){{
            put(StringConstant.CACHE_TASK_ID, taskId);
            put(StringConstant.CACHE_TASK_NAME, taskName);
        }}, NumberConstant.MONTH_SECOND) && redisUtils.set(taskIdPrefix + String.valueOf(taskId), taskIdentify, NumberConstant.MONTH_SECOND);
    }

    /**
     * 获取任务身份标识
     *
     * @param taskId 任务 ID
     * @param taskName 任务名称
     * @param taskIdPrefix 任务 ID 前缀
     * @return String 任务身份标识
     */
    public String getTaskIdentify(Long taskId, String taskName, String taskIdPrefix){
        String taskIdentify = (String) redisUtils.get(taskIdPrefix + String.valueOf(taskId));
        if (taskIdentify == null){
            taskIdentify = StringUtils.getUUID();
            redisUtils.hmset(taskIdentify, new HashMap<String, Object>(){{
                put(StringConstant.CACHE_TASK_ID, taskId);
                put(StringConstant.CACHE_TASK_NAME, taskName);
            }}, NumberConstant.MONTH_SECOND);
            redisUtils.set(taskIdPrefix + taskId, taskIdentify, NumberConstant.MONTH_SECOND);
        }
        return taskIdentify;
    }
}
