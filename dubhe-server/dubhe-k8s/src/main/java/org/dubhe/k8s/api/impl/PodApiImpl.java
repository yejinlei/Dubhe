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

package org.dubhe.k8s.api.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.enums.TrainJobStatusEnum;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.LabelBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.RegexUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description PodApi实现类
 * @date 2020-04-15
 */
public class PodApiImpl implements PodApi {

    private static final String TOKEN_REGEX = "token=[\\d|a-z]*";
    private static final String POD_URL = "http://{}:{}?{}";

    private K8sUtils k8sUtils;
    private KubernetesClient client;

    @Autowired
    private JupyterResourceApi jupyterResourceApi;
    @Autowired
    private MetricsApi metricsApi;

    @Value("${k8s.pod.metrics.grafanaUrl}")
    private String k8sPodMetricsGrafanaUrl;

    public PodApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 根据Pod名称和命名空间查询Pod
     *
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return BizPod Pod业务类
     */
    @Override
    public BizPod get(String namespace, String podName) {
        try{
            LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={};podName={}", namespace,podName);
            if (StringUtils.isEmpty(namespace)) {
                return new BizPod().baseErrorBadRequest();
            }
            Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
            BizPod bizPod = BizConvertUtils.toBizPod(pod);
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizPod);
            return bizPod;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.get error, param:[namespace]={}, [podName]={}, error:",namespace, podName, e);
            return new BizPod().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据命名空间和资源名查询Pod
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizPod Pod业务类
     */
    @Override
    public BizPod getWithResourceName(String namespace, String resourceName) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={};resourceName={}", namespace,resourceName);
            if (StringUtils.isEmpty(namespace)) {
                return new BizPod().baseErrorBadRequest();
            }
            PodList podList = client.pods().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (CollectionUtil.isEmpty(podList.getItems())) {
                return new BizPod().error(K8sResponseEnum.NOT_FOUND.getCode(), K8sResponseEnum.NOT_FOUND.getMessage());
            }
            Pod pod = podList.getItems().get(0);
            BizPod bizPod = BizConvertUtils.toBizPod(pod);
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizPod);
            return bizPod;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithResourceName error, param:[namespace]={}, [resourceName]={}, error:",namespace, resourceName, e);
            return new BizPod().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据命名空间和资源名查询Pod集合
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return List<BizPod> Pod业务类集合
     */
    @Override
    public List<BizPod> getListByResourceName(String namespace, String resourceName) {
        try{
            LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={};resourceName={}", namespace,resourceName);
            if (StringUtils.isEmpty(namespace)) {
                return Collections.EMPTY_LIST;
            }
            PodList podList = client.pods().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (CollectionUtil.isEmpty(podList.getItems())) {
                return Collections.EMPTY_LIST;
            }
            List<BizPod> bizPodList = BizConvertUtils.toBizPodList(podList.getItems());
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizPodList);
            return bizPodList;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithResourceName error, param:[namespace]={}, [resourceName]={}, error:",namespace, resourceName, e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 查询命名空间下所有Pod
     *
     * @param namespace 命名空间
     * @return List<BizPod> Pod业务类集合
     */
    @Override
    public List<BizPod> getWithNamespace(String namespace) {
        try{
            List<BizPod> bizPodList = new ArrayList<>();
            PodList podList = client.pods().inNamespace(namespace).list();
            if (CollectionUtil.isEmpty(podList.getItems())) {
                return bizPodList;
            }
            bizPodList = BizConvertUtils.toBizPodList(podList.getItems());
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizPodList);
            return bizPodList;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithNamespace error, param:[namespace]={}, error:",namespace, e);
            return Collections.EMPTY_LIST;
        }

    }

    /**
     * 查询集群所有Pod
     *
     * @return List<BizPod> Pod业务类集合
     */
    @Override
    public List<BizPod> listAll() {
        try{
            List<BizPod> bizPodList = client.pods().inAnyNamespace().list().getItems().parallelStream().map(obj -> BizConvertUtils.toBizPod(obj)).collect(Collectors.toList());
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", bizPodList);
            return bizPodList;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.listAll error:", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 根据Node分组获得所有运行中的Pod
     *
     * @return Map<String, List<BizPod>> 键为Node名称，值为Pod业务类集合
     */
    @Override
    public Map<String, List<BizPod>> listAllRuningPodGroupByNodeName() {
        try{
            List<BizPod> bizPodList = client.pods().inAnyNamespace().list().getItems().parallelStream().map(obj -> BizConvertUtils.toBizPod(obj)).collect(Collectors.toList());
            Map<String, List<BizPod>> map = bizPodList.parallelStream().filter(pod -> PodPhaseEnum.RUNNING.getPhase().equals(pod.getPhase())).collect(Collectors.groupingBy(BizPod::getNodeName));
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", map);
            return map;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.listAllRunningPodGroupByNodeName error:", e);
            return Collections.EMPTY_MAP;
        }
    }


    /**
     * 根据Node分组获取Pod信息
     *
     * @return Map<String,List<PtPodsVO>> 键为Node名称，值为Pod结果类集合
     */
    @Override
    public Map<String,List<PtPodsVO>> getPods(){
        try{
            Map<String,List<PtPodsVO>> map = metricsApi.getPodsMetricsRealTime().stream().collect(Collectors.groupingBy(PtPodsVO::getNodeName));
            LogUtil.info(LogEnum.BIZ_K8S,"Output {}", map);
            return map;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getPods error:", e);
            return Collections.EMPTY_MAP;
        }
    }


    /**
     * 获取Pod的CPU使用率,Memory使用量,GPU使用率,并用grafana展示出来
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return String podGrafanaUrl
     */
    @Override
    public String getPodMetricsGrafanaUrl(String namespace, String resourceName) {
        LogUtil.info(LogEnum.BIZ_K8S, "Starting obtain grafanaUrl of Pod, params:[namespace]={}, [resourceName]={}", namespace, resourceName);
        String podGrafanaUrl = null;
        try {
            BizPod bizPod = getWithResourceName(namespace, resourceName);
            if(bizPod.isSuccess() && bizPod.getPhase().equalsIgnoreCase(TrainJobStatusEnum.RUNNING.getMessage())){
                podGrafanaUrl = k8sPodMetricsGrafanaUrl.concat(bizPod.getName());
            }
        } catch (Exception e) {
            LogUtil.info(LogEnum.BIZ_K8S, "Failed to obtain grafanaUrl of Pod, params:[namespace]={}, [resourceName]={}, error:",
                    namespace, resourceName, e);
        }
        LogUtil.info(LogEnum.BIZ_K8S, "Obtaining grafanaUrl of Pod ended , params:[namespace]={}, [resourceName]={}, return value:{}",
                namespace, resourceName, podGrafanaUrl);
        return podGrafanaUrl;
    }

    /**
     * 根据label查询Pod集合
     *
     * @param labelBO k8s label资源 bo
     * @return List<Pod> Pod 实体类集合
     */
    @Override
    public List<Pod> list(LabelBO labelBO) {
        return client.pods().inAnyNamespace().withLabels(LabelUtils.withEnvLabel(labelBO.getKey(), labelBO.getValue())).list().getItems();
    }

    /**
     * 根据多个label查询Pod集合
     *
     * @param labelBos label资源 bo 的集合
     * @return List<Pod> Pod 实体类集合
     */
    @Override
    public List<Pod> list(Set<LabelBO> labelBos) {
        Map<String, String> labelMap = labelBos.stream().collect(Collectors.toMap(LabelBO::getKey, LabelBO::getValue));
        return client.pods().inAnyNamespace().withLabels(labelMap).list().getItems();
    }

    /**
     * 根据命名空间查询Pod集合
     *
     * @param namespace 命名空间
     * @return List<Pod> Pod 实体类集合
     */
    @Override
    public List<Pod> list(String namespace) {
        return client.pods().inNamespace(namespace).list().getItems();
    }

    /**
     * 根据命名空间和label查询Pod集合
     *
     * @param namespace 命名空间
     * @param labelBO label资源 bo
     * @return List<Pod> Pod 实体类集合
     */
    @Override
    public List<Pod> list(String namespace, LabelBO labelBO) {
        return client.pods().inNamespace(namespace).withLabels(LabelUtils.withEnvLabel(labelBO.getKey(), labelBO.getValue())).list().getItems();
    }

    /**
     * 根据命名空间和多个label查询Pod集合
     *
     * @param namespace 命名空间
     * @param labelBos label资源 bo 的集合
     * @return List<Pod> Pod 实体类集合
     */
    @Override
    public List<Pod> list(String namespace, Set<LabelBO> labelBos) {
        Map<String, String> labelMap = labelBos.stream().collect(Collectors.toMap(LabelBO::getKey, LabelBO::getValue));
        return client.pods().inNamespace(namespace).withLabels(labelMap).list().getItems();
    }



    /**
     * 根据命名空间和Pod名称查询Token信息
     *
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return String token
     */
    @Override
    public String getToken(String namespace, String podName) {
        try {
            String podLog = client.pods().inNamespace(namespace).withName(podName).getLog();
            return RegexUtil.getMatcher(podLog, TOKEN_REGEX);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getToken error params:[namespace]={}, [podName]={}, error:",namespace, podName, e);
        }
        return "";
    }

    /**
     * 根据命名空间和资源名获得Token信息
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return  String token
     */
    @Override
    public String getTokenByResourceName(String namespace, String resourceName) {
        try {
            PodList podList = client.pods().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (podList != null && CollectionUtil.isNotEmpty(podList.getItems())) {
                String podLog = client.pods().inNamespace(namespace).withName(podList.getItems().get(0).getMetadata().getName()).getLog();
                return RegexUtil.getMatcher(podLog, TOKEN_REGEX);
            }
            return "";
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getTokenByResourceName error, params:[namespace]={}, [resourceName]={}, error:",namespace, resourceName, e);
        }
        return "";
    }

    /**
     * 根据命名空间和资源名查询Notebook url
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return String validateJupyterUrl
     */
    @Override
    public String getUrlByResourceName(String namespace, String resourceName) {
        LogUtil.info(LogEnum.BIZ_K8S,"Start GetUrlByResourceName {} {}",namespace,resourceName);
        PtJupyterDeployVO info = jupyterResourceApi.get(namespace, resourceName);
        if (info != null && info.getIngressInfo() != null && CollectionUtil.isNotEmpty(info.getIngressInfo().getRules())) {
            String token = getTokenByResourceName(namespace, resourceName);
            if (StringUtils.isBlank(token)) {
                LogUtil.info(LogEnum.BIZ_K8S, "GetUrlByResourceName Jupyter Notebook token not generated,[namespace]={}, [resourceName]={}", namespace, resourceName);
                return "";
            }
            String url = StrUtil.format(POD_URL, info.getIngressInfo().getRules().get(0).getHost(), k8sUtils.getPort(), token);
            return validateJupyterUrl(url);
        }
        LogUtil.info(LogEnum.BIZ_K8S, "GetUrlByResourceName Jupyter statefulset not created,[namespace]={}, [resourceName]={}",namespace,resourceName);
        return "";
    }

    /**
     * 验证访问Notebook的url
     *
     * @param jupyterUrl 访问Notebook的url
     * @return String jupyterUrl
     */
    private String validateJupyterUrl(String jupyterUrl) {
        if (StringUtils.isBlank(jupyterUrl) || !jupyterUrl.contains(SymbolConstant.QUESTION+ K8sParamConstants.TOKEN)){
            return "";
        }
        try {
            HttpRequest httpRequest = HttpRequest.get(jupyterUrl);
            HttpResponse httpResponse = httpRequest.execute();
            if (httpResponse == null){
                LogUtil.info(LogEnum.BIZ_K8S, "ValidateJupyterUrl failed URL[{}] HttpResponse is null",jupyterUrl);
                return "";
            }
            int status = httpResponse.getStatus();
            if (HttpStatus.HTTP_OK != status && HttpStatus.HTTP_MOVED_TEMP != status){
                LogUtil.info(LogEnum.BIZ_K8S, "ValidateJupyterUrl failed URL[{}] status[{}]",jupyterUrl,status);
            }else {
                LogUtil.info(LogEnum.BIZ_K8S, "ValidateJupyterUrl success URL[{}] status[{}]",jupyterUrl,status);
                return jupyterUrl;
            }
        }catch (IORuntimeException e){
            LogUtil.info(LogEnum.BIZ_K8S, "ValidateJupyterUrl failed URL[{}], error message[{}]",jupyterUrl, e.getMessage());
        }
        return "";
    }
}
