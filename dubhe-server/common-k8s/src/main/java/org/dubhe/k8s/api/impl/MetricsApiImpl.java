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

import cn.hutool.core.util.NumberUtil;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.ContainerMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.functional.StringFormat;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.PrometheusMetricBO;
import org.dubhe.k8s.domain.dto.PodQueryDTO;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.resource.BizQuantity;
import org.dubhe.k8s.domain.vo.*;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description metrics api
 * @date 2020-05-22
 */
public class MetricsApiImpl implements MetricsApi {
    private KubernetesClient client;

    @Autowired
    private PodApi podApi;
    /**
     * prometheus 地址
     */
    @Value("${k8s.prometheus.url}")
    private String k8sPrometheusUrl;
    /**
     * prometheus 查询接口
     */
    @Value("${k8s.prometheus.query}")
    private String k8sPrometheusQuery;
    /**
     * prometheus 范围查询接口
     */
    @Value("${k8s.prometheus.query-range}")
    private String k8sPrometheusQueryRange;
    /**
     * prometheus gpu指标查询参数
     */
    @Value("${k8s.prometheus.gpu-query-param}")
    private String k8sPrometheusGpuQueryParam;
    /**
     * prometheus gpu显存总量指标查询参数
     */
    @Value("${k8s.prometheus.gpu-mem-total-query-param}")
    private String k8sPrometheusGpuMemTotalQueryParam;

    /**
     * prometheus gpu显存使用量指标查询参数
     */
    @Value("${k8s.prometheus.gpu-mem-use-query-param}")
    private String k8sPrometheusGpuMemUseQueryParam;
    /**
     * prometheus cpu指标范围查询参数
     */
    @Value("${k8s.prometheus.cpu-range-query-param}")
    private String k8sPrometheusCpuRangeQueryParam;
    /**
     * prometheus 内存指标范围查询参数
     */
    @Value("${k8s.prometheus.mem-range-query-param}")
    private String k8sPrometheusMemRangeQueryParam;
    /**
     * prometheus gpu指标范围查询参数
     */
    @Value("${k8s.prometheus.gpu-range-query-param}")
    private String k8sPrometheusGpuRangeQueryParam;

    /**
     * prometheus gpu显存总量指标范围查询参数
     */
    @Value("${k8s.prometheus.gpu-mem-total-range-query-param}")
    private String k8sPrometheusGpuMemTotalRangeQueryParam;

    /**
     * prometheus gpu显存使用量指标范围查询参数
     */
    @Value("${k8s.prometheus.gpu-mem-use-range-query-param}")
    private String k8sPrometheusGpuMemUseRangeQueryParam;

    public MetricsApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
    }

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
        if (null == listMap) {
            return list;
        }
        metrics.getItems().stream().forEach(metric -> {
            /**创建集合保存PtPodsResult信息**/
            List<ContainerMetrics> containers = metric.getContainers();
            containers.stream().forEach(containerMetrics -> {
                Map<String, Quantity> usage = containerMetrics.getUsage();
                PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getNamespace(), metric.getMetadata().getName(),
                        usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                        usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                        usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                        usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                        listMap.get(metric.getMetadata().getName()).get(0).getNodeName(),
                        listMap.get(metric.getMetadata().getName()).get(0).getPhase(), null);

                List<BizContainer> containerList = listMap.get(metric.getMetadata().getName()).get(0).getContainers();
                countGpuUsed(containerList, ptContainerMetricsResult);
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
                            PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getNamespace(), metric.getMetadata().getName(),
                                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
                                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getAmount(),
                                    usage.get(K8sParamConstants.QUANTITY_MEMORY_KEY).getFormat(),
                                    podNode.get(metric.getMetadata().getName()),
                                    bizPod.getPhase(), null
                            );
                            List<BizContainer> containerList = bizPod.getContainers();
                            countGpuUsed(containerList, ptContainerMetricsResult);
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
     * 计算GPU使用数量
     *
     * @param containerList BizContainer对象
     * @param ptContainerMetricsResult 封装pod信息
     */
    private void countGpuUsed(List<BizContainer> containerList, PtPodsVO ptContainerMetricsResult) {
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
    }

    /**
     * 获取k8s resourceName 下pod当前cpu、内存用量的实时使用情况
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtPodsVO> getPodMetricsRealTime(String namespace, String resourceName) {
        List<PtPodsVO> ptPodsVOS = new ArrayList<>();
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return ptPodsVOS;
        }
        List<BizPod> pods = podApi.getListByResourceName(namespace, resourceName);
        if (CollectionUtils.isEmpty(pods)) {
            return ptPodsVOS;
        }
        List<PodMetrics> podMetricsList = client.top().pods().metrics(namespace).getItems();
        if (!CollectionUtils.isEmpty(pods)) {
            Map<String, PodMetrics> podMetricsMap = podMetricsList.stream().collect(Collectors.toMap(obj -> obj.getMetadata().getName(), obj -> obj));
            for (BizPod pod : pods) {
                List<PtPodsVO> ptPodsVOList = getPtPodsVO(pod, podMetricsMap.get(pod.getName()));
                if (!CollectionUtils.isEmpty(ptPodsVOList)) {
                    ptPodsVOS.addAll(ptPodsVOList);
                }
            }
        }
        for (PtPodsVO ptPodsVO : ptPodsVOS) {
            generateGpuUsage(ptPodsVO);
            ptPodsVO.calculationPercent();
        }
        return ptPodsVOS;
    }

    /**
     * 获取k8s pod当前cpu、内存用量的实时使用情况
     * @param namespace 命名空间
     * @param podName pod名称
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtPodsVO> getPodMetricsRealTimeByPodName(String namespace, String podName) {
        List<PtPodsVO> ptPodsVOS = new ArrayList<>();
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(podName)) {
            return ptPodsVOS;
        }
        BizPod pod = podApi.get(namespace, podName);
        if (null == pod) {
            return ptPodsVOS;
        }
        PodMetrics podMetrics = null;
        try {
            podMetrics = client.top().pods().metrics(namespace, podName);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "MetricsApiImpl.getPodMetricsRealTimeByPodName error:{}", e);
        }
        ptPodsVOS = getPtPodsVO(pod, podMetrics);
        for (PtPodsVO ptPodsVO : ptPodsVOS) {
            generateGpuUsage(ptPodsVO);
            ptPodsVO.calculationPercent();
        }
        return ptPodsVOS;
    }

    /**
     * 获取k8s pod当前cpu、内存用量的实时使用情况
     * @param namespace 命名空间
     * @param podNames pod名称列表
     * @return List<PtPodsVO> Pod资源用量结果类集合
     */
    @Override
    public List<PtPodsVO> getPodMetricsRealTimeByPodName(String namespace, List<String> podNames) {
        List<PtPodsVO> ptPodsVOS = new ArrayList<>();
        for (String podName : podNames) {
            ptPodsVOS.addAll(getPodMetricsRealTimeByPodName(namespace, podName));
        }
        return ptPodsVOS;
    }

    /**
     * 获取k8s resourceName 下pod 时间范围内cpu、内存用量的实时使用情况
     * @param podQueryDTO Pod基础信息查询入参
     * @return List<PodRangeMetricsVO> Pod监控指标 列表
     */
    @Override
    public List<PodRangeMetricsVO> getPodRangeMetrics(PodQueryDTO podQueryDTO) {
        List<PodRangeMetricsVO> podRangeMetricsVOS = new ArrayList<>();
        if (StringUtils.isEmpty(podQueryDTO.getNamespace()) || StringUtils.isEmpty(podQueryDTO.getResourceName())) {
            return podRangeMetricsVOS;
        }
        List<BizPod> pods = podApi.getListByResourceName(podQueryDTO.getNamespace(), podQueryDTO.getResourceName());
        if (CollectionUtils.isEmpty(pods)) {
            return podRangeMetricsVOS;
        }
        podQueryDTO.generateDefaultParam();
        for (BizPod pod : pods) {
            podRangeMetricsVOS.add(getPodRangeMetricsVO(pod, podQueryDTO));
        }
        return podRangeMetricsVOS;
    }

    /**
     * 根据namespace、podName获取k8s pod 时间范围内cpu、内存用量的实时使用情况
     * @param podQueryDTO Pod基础信息查询入参
     * @return List<PodRangeMetricsVO> Pod监控指标 列表
     */
    @Override
    public List<PodRangeMetricsVO> getPodRangeMetricsByPodName(PodQueryDTO podQueryDTO) {
        List<PodRangeMetricsVO> podRangeMetricsVOS = new ArrayList<>();
        if (StringUtils.isEmpty(podQueryDTO.getNamespace()) || CollectionUtils.isEmpty(podQueryDTO.getPodNames())) {
            return podRangeMetricsVOS;
        }
        List<BizPod> pods = podApi.get(podQueryDTO.getNamespace(), podQueryDTO.getPodNames());
        if (null == pods) {
            return podRangeMetricsVOS;
        }
        podQueryDTO.generateDefaultParam();
        for (BizPod pod : pods) {
            podRangeMetricsVOS.add(getPodRangeMetricsVO(pod, podQueryDTO));
        }
        return podRangeMetricsVOS;
    }

    /**
     * 组装Pod历史监控指标
     *
     * @param pod pod业务类
     * @param podQueryDTO 查询参数
     * @return PodRangeMetricsVO Pod历史监控指标 VO
     */
    private PodRangeMetricsVO getPodRangeMetricsVO(BizPod pod, PodQueryDTO podQueryDTO) {
        PodRangeMetricsVO podRangeMetricsVO = new PodRangeMetricsVO(pod.getName());
        PrometheusMetricBO cpuRangeMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQueryRange, PrometheusUtil.getQueryParamMap(k8sPrometheusCpuRangeQueryParam, pod.getName(), podQueryDTO));
        PrometheusMetricBO memRangeMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQueryRange, PrometheusUtil.getQueryParamMap(k8sPrometheusMemRangeQueryParam, pod.getName(), podQueryDTO));
        PrometheusMetricBO gpuRangeMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQueryRange, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuRangeQueryParam, pod.getName(), podQueryDTO));
        PrometheusMetricBO gpuMemTotalRangeMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQueryRange, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuMemTotalRangeQueryParam, pod.getName(), podQueryDTO));
        PrometheusMetricBO gpuMemUseRangeMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQueryRange, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuMemUseRangeQueryParam, pod.getName(), podQueryDTO));

        StringFormat cpuMetricsFormat = (value) -> {
            return value == null ? String.valueOf(MagicNumConstant.ZERO) : NumberUtil.round(Double.valueOf(value.toString()), MagicNumConstant.TWO).toString();
        };
        podRangeMetricsVO.setCpuMetrics(cpuRangeMetrics.getValues(cpuMetricsFormat));

        StringFormat memMetricsFormat = (value) -> {
            return NumberUtil.isNumber(String.valueOf(value)) ? String.valueOf(Long.valueOf(String.valueOf(value)) / MagicNumConstant.BINARY_TEN_EXP) : String.valueOf(MagicNumConstant.ZERO);
        };
        podRangeMetricsVO.setMemoryMetrics(memRangeMetrics.getValues(memMetricsFormat));
        Map<String, List<MetricsDataResultValueVO>> gpuMetricsResults = gpuRangeMetrics.getGpuMetricsResults();
        List<GpuTotalMemResultVO> gpuTotalMemResults = gpuMemTotalRangeMetrics.getGpuTotalMemResults();
        Map<String, List<MetricsDataResultValueVO>> gpuMemResults = gpuMemUseRangeMetrics.getGpuMemResults();
        List<GpuMetricsDataResultVO> gpuMetricsDataResultVOS = gpuTotalMemResults.stream().map(x -> {
                    GpuMetricsDataResultVO gpuMetricsDataResultVO = new GpuMetricsDataResultVO();
                    gpuMetricsDataResultVO.setAccId(x.getAccId()).setTotalMemValues(x.getGpuTotalMemValue());
                    if (gpuMemResults.containsKey(x.getAccId())) {
                        gpuMetricsDataResultVO.setGpuMemValues(gpuMemResults.get(x.getAccId()));
                    }
                    if (gpuMetricsResults.containsKey(x.getAccId())) {
                        gpuMetricsDataResultVO.setGpuMetricsValues(gpuMetricsResults.get(x.getAccId()));
                    }
                    return gpuMetricsDataResultVO;
                }
        ).collect(Collectors.toList());

        podRangeMetricsVO.setGpuMetrics(gpuMetricsDataResultVOS);
        return podRangeMetricsVO;
    }

    /**
     * 查询Gpu使用率
     * @param ptPodsVO pod信息
     */
    private void generateGpuUsage(PtPodsVO ptPodsVO) {
        PrometheusMetricBO prometheusMetricBO = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQuery, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuQueryParam, ptPodsVO.getPodName()));
        PrometheusMetricBO gpuMemTotalMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQuery, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuMemTotalQueryParam, ptPodsVO.getPodName()));
        PrometheusMetricBO gpuMemUseMetrics = PrometheusUtil.getQuery(k8sPrometheusUrl + k8sPrometheusQuery, PrometheusUtil.getQueryParamMap(k8sPrometheusGpuMemUseQueryParam, ptPodsVO.getPodName()));

        if (prometheusMetricBO == null || gpuMemTotalMetrics == null || gpuMemUseMetrics == null) {
            return;
        }
        List<GpuTotalMemResultVO> gpuTotalMemValue = gpuMemTotalMetrics.getGpuTotalMemValue();
        Map<String, String> gpuMemValue = gpuMemUseMetrics.getGpuMemValue();
        Map<String, Float> gpuUsage = prometheusMetricBO.getGpuUsage();

        List<GpuValueVO> gpuValueVOS = gpuTotalMemValue.stream().map(x -> {
                    GpuValueVO gpuValueVO = new GpuValueVO();
                    gpuValueVO.setAccId(x.getAccId()).setGpuTotalMemValue(x.getGpuTotalMemValue());
                    if (gpuMemValue.containsKey(x.getAccId())) {
                        gpuValueVO.setGpuMemValue(gpuMemValue.get(x.getAccId()));
                    }
                    if (gpuUsage.containsKey(x.getAccId())) {
                        gpuValueVO.setUsage(gpuUsage.get(x.getAccId()));
                    }
                    return gpuValueVO;
                }
        ).collect(Collectors.toList());
        ptPodsVO.setGpuUsagePersent(gpuValueVOS);
    }

    /**
     * assemble PtPodsVO
     * @param bizPod pod
     * @param metric 查询指标
     * @return List<PtPodsVO> pod信息列表
     */
    private List<PtPodsVO> getPtPodsVO(BizPod bizPod, PodMetrics metric) {
        List<PtPodsVO> ptPodsVOList = new ArrayList<>();
        if (metric == null) {
            return ptPodsVOList;
        }
        Map<String, ContainerMetrics> containerMetricsMap = metric.getContainers().stream().collect(Collectors.toMap(obj -> obj.getName(), obj -> obj));
        for (BizContainer container : bizPod.getContainers()) {
            Map<String, BizQuantity> request = container.getRequests();
            if (containerMetricsMap.get(container.getName()) == null) {
                continue;
            }
            Map<String, Quantity> usage = containerMetricsMap.get(container.getName()).getUsage();
            PtPodsVO ptContainerMetricsResult = new PtPodsVO(metric.getMetadata().getNamespace(), metric.getMetadata().getName(),
                    request.get(K8sParamConstants.QUANTITY_CPU_KEY) == null ? null : request.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                    usage.get(K8sParamConstants.QUANTITY_CPU_KEY).getAmount(),
                    request.get(K8sParamConstants.QUANTITY_CPU_KEY) == null ? null : request.get(K8sParamConstants.QUANTITY_CPU_KEY).getFormat(),
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
        }
        ;

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
        if (StringUtils.isEmpty(namespace)) {
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
