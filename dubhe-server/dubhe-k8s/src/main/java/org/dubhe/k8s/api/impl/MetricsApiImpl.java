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

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.ContainerMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.constant.StringConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.GpuMetricBO;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.vo.PtContainerMetricsVO;
import org.dubhe.k8s.domain.vo.PtNodeMetricsVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description metrics api
 * @date 2020-05-22
 */
public class MetricsApiImpl implements MetricsApi {
    private KubernetesClient client;

    @Autowired
    private PodApi podApi;

    public MetricsApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

    @Value("${k8s.prometheus.query-url}")
    private String k8sPrometheusQueryUrl;

    @Value("${k8s.prometheus.gpu-query-param}")
    private String k8sPrometheusGpuQueryParam;

    /**
     * 获取k8s所有节点当前cpu、内存用量
     *
     * @return List<PtNodeMetricsVO> NodeMetrics 结果类集合
     */
    @Override
    public List<PtNodeMetricsVO> getNodeMetrics() {
        try {
            List<PtNodeMetricsVO> list = new ArrayList<>();
            NodeMetricsList nodeMetricList = client.top().nodes().metrics();
            nodeMetricList.getItems().forEach(nodeMetrics ->
                    list.add(new PtNodeMetricsVO(nodeMetrics.getMetadata().getName(),
                            nodeMetrics.getTimestamp(),
                            nodeMetrics.getUsage().get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                            nodeMetrics.getUsage().get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                            nodeMetrics.getUsage().get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                            nodeMetrics.getUsage().get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat()
                    ))
            );
            return list;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getNodeMetrics error:{}", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 获取k8s所有Pod资源用量的实时信息
     *
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtPodsVO> getPodsMetricsRealTime() {
        /**查询所有pod的信息一些信息**/
        PodMetricsList metrics = client.top().pods().metrics();
        List<PtPodsVO> list = new ArrayList<>();
        /**将Pod和podName形成映射关系**/
        Map<String, List<BizPod>> listMap = client.pods().inAnyNamespace().list().getItems().parallelStream().map(obj -> BizConvertUtils.toBizPod(obj)).collect(Collectors.groupingBy(BizPod::getName));
        if(null == listMap) {
            return list;
        }
        metrics.getItems().stream().forEach(metric -> {
            /**创建集合保存PtPodsResult信息**/
            List<ContainerMetrics> containers = metric.getContainers();
            containers.stream().forEach(containerMetrics -> {
                Map<String, Quantity> usage = containerMetrics.getUsage();
                PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getName(),
                        usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                        usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                        usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                        usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                        listMap.get(metric.getMetadata().getName()).get(0).getNodeName(),
                        listMap.get(metric.getMetadata().getName()).get(0).getPhase(), null);

                List<BizContainer> containerList = listMap.get(metric.getMetadata().getName()).get(0).getContainers();
                for (BizContainer container : containerList) {
                    Map<String, BizQuantity> limits = container.getLimits();
                    if (limits == null) {
                        ptContainerMetricsResult.setGpuUsed(SymbolConstant.ZERO);
                    } else {
                        BizQuantity bizQuantity = limits.get(K8sParamConstants.GPU_RESOURCE_KEY);
                        String count = bizQuantity != null ? bizQuantity.getAmount() : SymbolConstant.ZERO;
                        /**将显卡数量保存起来**/
                        ptContainerMetricsResult.setGpuUsed(count);
                    }
                }
                list.add(ptContainerMetricsResult);
            });
        });
        return list;


    }

    /**
     * 获取k8s所有pod当前cpu、内存用量的实时使用情况
     *
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtPodsVO> getPodMetricsRealTime() {
        try {
            List<PtPodsVO> list = new ArrayList<>();
            Map<String, String> podNode = new HashMap<>();
            PodMetricsList metrics = client.top().pods().metrics();
            List<BizPod> bizPodList = client.pods().inAnyNamespace().list().getItems().parallelStream().map(obj -> BizConvertUtils.toBizPod(obj)).collect(Collectors.toList());
            bizPodList.stream().forEach(bizPod -> podNode.put(bizPod.getName(), bizPod.getNodeName()));
            metrics.getItems().stream().forEach(metric -> {
                for (BizPod bizPod : bizPodList) {
                    if (bizPod.getName().equals(metric.getMetadata().getName())) {
                        List<ContainerMetrics> containers = metric.getContainers();
                        containers.stream().forEach(containerMetrics ->
                        {
                            Map<String, Quantity> usage = containerMetrics.getUsage();
                            PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getName(),
                                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                                    podNode.get(metric.getMetadata().getName()),
                                    bizPod.getPhase(), null
                            );
                            List<BizContainer> containerList = bizPod.getContainers();
                            for (BizContainer container : containerList) {
                                Map<String, BizQuantity> limits = container.getLimits();
                                if (limits == null) {
                                    ptContainerMetricsResult.setGpuUsed(SymbolConstant.ZERO);
                                } else {
                                    BizQuantity bizQuantity = limits.get(K8sParamConstants.GPU_RESOURCE_KEY);
                                    String count = bizQuantity != null ? bizQuantity.getAmount() : SymbolConstant.ZERO;
                                    /**将显卡数量保存起来**/
                                    ptContainerMetricsResult.setGpuUsed(count);
                                }
                            }
                            list.add(ptContainerMetricsResult);
                        });
                    }
                }

            });

            return list;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getPodMetricsRealTime error:{}", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 获取k8s resourceName 下pod当前cpu、内存用量的实时使用情况
     * @param namespace
     * @param resourceName
     * @return
     */
    @Override
    public List<PtPodsVO> getPodMetricsRealTime(String namespace, String resourceName) {
        List<PtPodsVO> ptPodsVOS = new ArrayList<>();
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)){
            return ptPodsVOS;
        }
        List<BizPod> pods = podApi.getListByResourceName(namespace,resourceName);
        if (CollectionUtils.isEmpty(pods)){
            return ptPodsVOS;
        }
        List<PodMetrics> podMetricsList = client.top().pods().metrics(namespace).getItems();
        if (!CollectionUtils.isEmpty(pods)){
            Map<String,PodMetrics> podMetricsMap = podMetricsList.stream().collect(Collectors.toMap(obj -> obj.getMetadata().getName(), obj -> obj));
            for (BizPod pod : pods){
                List<PtPodsVO> ptPodsVOList = getPtPodsVO(pod,podMetricsMap.get(pod.getName()));
                if (!CollectionUtils.isEmpty(ptPodsVOList)){
                    ptPodsVOS.addAll(ptPodsVOList);
                }
            }
        }
        for (PtPodsVO ptPodsVO : ptPodsVOS){
            generateGpuUsage(ptPodsVO);
            ptPodsVO.calculationPercent();
        }
        return ptPodsVOS;
    }

    /**
     * 生成Gpu使用率
     * @param ptPodsVO
     */
    private void generateGpuUsage(PtPodsVO ptPodsVO){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(StringConstant.QUERY, StrUtil.format(k8sPrometheusGpuQueryParam,ptPodsVO.getPodName()));
        String metricStr = HttpUtil.get(k8sPrometheusQueryUrl,paramMap);
        if (StringUtils.isEmpty(metricStr)){
            return;
        }
        try{
            GpuMetricBO gpuMetricBO = JSON.parseObject(metricStr,GpuMetricBO.class);
            ptPodsVO.setGpuUsagePersent(gpuMetricBO.getValue());
        }catch (ClassCastException e){
            LogUtil.error(LogEnum.BIZ_K8S, "getGpuUsage parse metricStr error:{}", e);
        }
        return;
    }

    /**
     * assemble PtPodsVO
     * @param bizPod
     * @param metric
     * @return
     */
    private List<PtPodsVO> getPtPodsVO(BizPod bizPod,PodMetrics metric){
        List<PtPodsVO> ptPodsVOList = new ArrayList<>();
        if (metric == null){
            return ptPodsVOList;
        }
        Map<String,ContainerMetrics> containerMetricsMap = metric.getContainers().stream().collect(Collectors.toMap(obj -> obj.getName(), obj -> obj));
        for (BizContainer container : bizPod.getContainers()){
            Map<String, BizQuantity> request = container.getRequests();
            if (containerMetricsMap.get(container.getName()) == null){
                continue;
            }
            Map<String, Quantity> usage = containerMetricsMap.get(container.getName()).getUsage();
            PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getName(),
                    request.get(K8sParamConstants.QUANTITY_CPU_KEY) ==null ? null : request.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                    request.get(K8sParamConstants.QUANTITY_CPU_KEY) ==null ? null : request.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                    request.get(K8sParamConstants.QUANTITY_MEMORY_KEY) == null ? null : request.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                    request.get(K8sParamConstants.QUANTITY_MEMORY_KEY) == null ? null : request.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                    bizPod.getNodeName(),
                    bizPod.getPhase(), null
            );

            Map<String, BizQuantity> limits = container.getLimits();
            if (limits == null) {
                ptContainerMetricsResult.setGpuUsed(SymbolConstant.ZERO);
            } else {
                BizQuantity bizQuantity = limits.get(K8sParamConstants.GPU_RESOURCE_KEY);
                String count = bizQuantity != null ? bizQuantity.getAmount() : SymbolConstant.ZERO;
                /**将显卡数量保存起来**/
                ptContainerMetricsResult.setGpuUsed(count);
            }
            ptPodsVOList.add(ptContainerMetricsResult);
        };

        return ptPodsVOList;
    }

    /**
     * 查询命名空间下所有Pod的cpu和内存使用
     *
     * @param namespace 命名空间
     * @return List<PtContainerMetricsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtContainerMetricsVO> getContainerMetrics(String namespace) {
        if(StringUtils.isEmpty(namespace)){
            return Collections.EMPTY_LIST;
        }
        try {
            return getContainerMetrics(client.top().pods().metrics(namespace).getItems());
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getContainerMetrics error:{}", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 查询所有命名空间下所有pod的cpu和内存使用
     *
     * @return List<PtContainerMetricsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtContainerMetricsVO> getContainerMetrics() {
        try {
            return getContainerMetrics(client.top().pods().metrics().getItems());
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getContainerMetrics error:{}", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 查询所有命名空间下所有pod的cpu和内存使用
     *
     * @param pods
     * @return List<PtContainerMetricsVO> Pod资源用量结果类集合
     */
    private List<PtContainerMetricsVO> getContainerMetrics(List<PodMetrics> pods) {
        try {
            List<PtContainerMetricsVO> list = new ArrayList<>();
            pods.forEach(podMetrics ->
                    podMetrics.getContainers().forEach(containerMetrics ->
                            list.add(new PtContainerMetricsVO(podMetrics.getMetadata().getName(),
                                    containerMetrics.getName(),
                                    podMetrics.getTimestamp(),
                                    containerMetrics.getUsage().get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                                    containerMetrics.getUsage().get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                                    containerMetrics.getUsage().get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                                    containerMetrics.getUsage().get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat()))
                    )
            );
            return list;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getContainerMetrics error, param:{} error:{}", pods, e);
            return Collections.EMPTY_LIST;
        }
    }
}
