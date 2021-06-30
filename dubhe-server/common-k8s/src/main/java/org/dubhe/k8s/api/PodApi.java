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

package org.dubhe.k8s.api;

import io.fabric8.kubernetes.api.model.Pod;
import org.dubhe.k8s.domain.bo.LabelBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtPodsVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description k8s中资源为Pod的操作接口
 * @date 2020-07-03
 */
public interface PodApi {

    /**
     * 根据Pod名称和命名空间查询Pod
     *
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return BizPod Pod业务类
     */
    BizPod get(String namespace, String podName);

    /**
     * 根据Pod名称列表和命名空间查询Pod列表
     *
     * @param namespace 命名空间
     * @param podNames Pod名称
     * @return List<BizPod> Pod业务类列表
     */
    List<BizPod> get(String namespace, List<String> podNames);
    /**
     * 根据命名空间和资源名查询Pod
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizPod Pod业务类
     */

    BizPod getWithResourceName(String namespace, String resourceName);
    /**
     * 根据命名空间和资源名查询Pod集合
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return List<BizPod> Pod业务类集合
     */
    List<BizPod> getListByResourceName(String namespace, String resourceName);

    /**
     * 查询命名空间下所有Pod
     *
     * @param namespace 命名空间
     * @return List<BizPod> Pod业务类集合
     */
    List<BizPod> getWithNamespace(String namespace);

    /**
     * 查询集群所有Pod
     *
     * @return List<BizPod> Pod业务类集合
     */
    List<BizPod> listAll();

    List<BizPod> findByDtName(String dtName);
    /**
     * 根据Node分组获得所有运行中的Pod
     *
     * @return Map<String, List<BizPod>> 键为Node名称，值为Pod业务类集合
     */
    Map<String, List<BizPod>> listAllRuningPodGroupByNodeName();

    /**
     * 根据Node分组获取Pod信息
     *
     * @return Map<String,List<PtPodsVO>> 键为Node名称，值为Pod结果类集合
     */
    Map<String,List<PtPodsVO>> getPods();

    /**
     * 根据label查询Pod集合
     *
     * @param labelBO k8s label资源 bo
     * @return List<Pod> Pod 实体类集合
     */
    List<Pod> list(LabelBO labelBO);

    /**
     * 根据多个label查询Pod集合
     *
     * @param labelBos label资源 bo 的集合
     * @return List<Pod> Pod 实体类集合
     */
    List<Pod> list(Set<LabelBO> labelBos);

    /**
     * 根据命名空间查询Pod集合
     *
     * @param namespace 命名空间
     * @return List<Pod> Pod 实体类集合
     */
    List<Pod> list(String namespace);

    /**
     * 根据命名空间和label查询Pod集合
     *
     * @param namespace 命名空间
     * @param labelBO label资源 bo
     * @return List<Pod> Pod 实体类集合
     */
    List<Pod> list(String namespace, LabelBO labelBO);

    /**
     * 根据命名空间和多个label查询Pod集合
     *
     * @param namespace 命名空间
     * @param labelBos label资源 bo 的集合
     * @return List<Pod> Pod 实体类集合
     */
    List<Pod> list(String namespace, Set<LabelBO> labelBos);


    /**
     * 根据命名空间和Pod名称查询Token信息
     *
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return String token
     */
    String getToken(String namespace, String podName);

    /**
     * 根据命名空间和资源名获得Token信息
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return  String token
     */
    String getTokenByResourceName(String namespace, String resourceName);

    /**
     * 根据命名空间和资源名查询Notebook url
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return String validateJupyterUrl 验证Jupyte的url值
     */
    String getUrlByResourceName(String namespace, String resourceName);




}
