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

package org.dubhe.k8s.utils;

import cn.hutool.core.collection.CollectionUtil;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.Job;
import org.dubhe.k8s.domain.resource.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description Biz转换工具类，统一在此进行fabric8 POJO到Biz的转换，以保证各处转换结果一致
 * @date 2020-06-02
 * */
public class BizConvertUtils {
    private static final String COMPLETED = "Completed";

    /**
     * 将Pod List转为 PodBiz List
     *
     * @param podList Pod的集合
     * @return List<BizPod> BizPod的集合
     */
    public static List<BizPod> toBizPodList(List<Pod> podList) {
        return podList.parallelStream().map(obj -> toBizPod(obj)).collect(Collectors.toList());
    }

    /**
     * 将Pod 转为 PodBiz，并设置completedTime
     *
     * @param pod Pod对象
     * @return BizPod BizPod对象
     */
    public static BizPod toBizPod(Pod pod) {
        if (pod == null) {
            return null;
        }
        BizPod bizPod = MappingUtils.mappingTo(pod, BizPod.class);
        List<ContainerStatus> containerStatus = pod.getStatus().getContainerStatuses();
        if (!CollectionUtil.isEmpty(containerStatus)) {
            containerStatus.forEach(item -> {
                if (item.getState().getTerminated() != null && COMPLETED.equals(item.getState().getTerminated().getReason())) {
                    bizPod.setCompletedTime(item.getState().getTerminated().getFinishedAt());
                }
            });
        }
        return bizPod;
    }

    /**
     * 将List<Deployment> 转为 List<BizDeployment>
     *
     * @param deploymentList Deployment集合
     * @return List<BizDeployment> BizDeployment集合
     */
    public static List<BizDeployment> toBizDeploymentList(List<Deployment> deploymentList) {
        return deploymentList.parallelStream().map(obj -> toBizDeployment(obj)).collect(Collectors.toList());
    }

    /**
     * 将Deployment 转为 BizDeployment
     *
     * @param deployment Deployment对象
     * @return BizDeployment BizDeployment对象
     */
    public static BizDeployment toBizDeployment(Deployment deployment) {
        return MappingUtils.mappingTo(deployment, BizDeployment.class);
    }

    /**
     * 将List<Job> 转为 List<BizJob>
     *
     * @param jobList Job的集合
     * @return List<BizJob> BizJob的集合
     */
    public static List<BizJob> toBizJobList(List<Job> jobList) {
        return jobList.parallelStream().map(obj -> toBizJob(obj)).collect(Collectors.toList());
    }

    /**
     * 将Job 转为 BizJob
     *
     * @param job Job对象
     * @return BizJob BizJob对象
     */
    public static BizJob toBizJob(Job job) {
        return MappingUtils.mappingTo(job, BizJob.class);
    }

    /**
     * 将Namespace 转为 BizNamespace
     *
     * @param namespace 命名空间
     * @return BizNamespace BizNamespace命名空间
     */
    public static BizNamespace toBizNamespace(Namespace namespace) {
        return MappingUtils.mappingTo(namespace, BizNamespace.class);
    }

    /**
     * 将Node 转为 BizNode
     *
     * @param node Node对象
     * @return BizNode BizNode对象
     */
    public static BizNode toBizNode(Node node) {
        return MappingUtils.mappingTo(node, BizNode.class);
    }

    /**
     * 将PersistentVolumeClaim 转为 BizPersistentVolumeClaim
     *
     * @param persistentVolumeClaim
     * @return BizPersistentVolumeClaim PersistentVolumeClaim实体类
     */
    public static BizPersistentVolumeClaim toBizPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        return MappingUtils.mappingTo(persistentVolumeClaim, BizPersistentVolumeClaim.class);
    }

    /**
     * 将ResourceQuota 转为 BizResourceQuota
     *
     * @param resourceQuota
     * @return BizResourceQuota resourceQuota业务类
     */
    public static BizResourceQuota toBizResourceQuota(ResourceQuota resourceQuota) {
        return MappingUtils.mappingTo(resourceQuota, BizResourceQuota.class);
    }

    /**
     * 将LimitRange 转为 BizLimitRange
     *
     * @param limitRange
     * @return BizLimitRange BizLimitRange实体类
     */
    public static BizLimitRange toBizLimitRange(LimitRange limitRange) {
        return MappingUtils.mappingTo(limitRange, BizLimitRange.class);
    }
}
