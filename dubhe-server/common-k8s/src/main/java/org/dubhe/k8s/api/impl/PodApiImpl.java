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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.RegexUtil;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.utils.IOUtil;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.JupyterResourceApi;
import org.dubhe.k8s.api.MetricsApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.LabelBO;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterDeployVO;
import org.dubhe.k8s.domain.vo.PtPodsVO;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.PodPhaseEnum;
import org.dubhe.k8s.listener.DefaultPodExecListener;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.get error, param:[namespace]={}, [podName]={}, error:{}",namespace, podName, e);
            return new BizPod().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据Pod名称列表和命名空间查询Pod列表
     *
     * @param namespace 命名空间
     * @param podNames Pod名称
     * @return List<BizPod> Pod业务类列表
     */
    @Override
    public List<BizPod> get(String namespace, List<String> podNames) {
        try{
            List<BizPod> bizPodList = new ArrayList<>();
            LogUtil.info(LogEnum.BIZ_K8S,"Input namespace={};podNames={}", namespace,podNames);
            if (StringUtils.isEmpty(namespace)) {
                return bizPodList;
            }
            PodList podList = client.pods().inNamespace(namespace).list();
            if (podList == null || CollectionUtils.isEmpty(podList.getItems())){
                return bizPodList;
            }
            for (Pod pod : podList.getItems()){
                if (podNames.contains(pod.getMetadata().getName())){
                    bizPodList.add(BizConvertUtils.toBizPod(pod));
                }
            }
            return bizPodList;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.get error, param:[namespace]={}, [podNames]={}, error:{}",namespace, podNames, e);
            return new ArrayList<>();
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithResourceName error, param:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName, e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithResourceName error, param:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName, e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getWithNamespace error, param:[namespace]={}, error:{}",namespace, e);
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
     *根据dtname查询pod信息
     *
     * @param dtname 自定义dt的名称
     * @return List<BizPod> pod业务类集合
     */
    @Override
    public List<BizPod> findByDtName(String dtname) {
        List<Pod> items = client.pods().list().getItems();
        LogUtil.info(LogEnum.BIZ_K8S,"Output {}",items);
        List<BizPod> bizPods = new ArrayList<>();
        if (!(CollectionUtil.isEmpty(items))) {
            items.stream().forEach(pod -> {
                Map<String, String> labels = pod.getMetadata().getLabels();
                if (labels != null) {
                    String dtName = labels.get("dt-name");
                    if (dtName != null) {
                        if (dtName.equals(dtname)) {
                            bizPods.add(BizConvertUtils.toBizPod(pod));
                        }
                    }
                }
            });
        }
        return bizPods;
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.listAllRunningPodGroupByNodeName error:{}", e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getPods error:{}", e);
            return Collections.EMPTY_MAP;
        }
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
     * 根据resourceName查询Pod集合
     * @param resourceName
     * @return
     */
    @Override
    public List<Pod> listByResourceName(String resourceName) {
        try{
            return client.pods().inAnyNamespace().withLabel(K8sLabelConstants.BASE_TAG_SOURCE, resourceName).list().getItems();
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.listByResourceName error:{}", e);
            return new ArrayList<>();
        }
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getToken error params:[namespace]={}, [podName]={}, error:{}",namespace, podName, e);
        }
        return "";
    }


    /**
     * 根据resourceName 获取pod对应k8s中labels
     *
     * @param resourceName 资源名称
     * @return Map<String, String> map
     */
    @Override
    public Map<String, String> getLabels(String resourceName){
        return LabelUtils.withEnvResourceName(resourceName);
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
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.getTokenByResourceName error, params:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName, e);
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
      * 拷贝文件到pod
      * @param namespace 命名空间
      * @param podName pod名称
      * @param containerName 容器名称
      * @param file 文件
      * @param targetDir 目标路径
      */
    @Override
    public void copyToPod(String namespace, String podName, String containerName, File file, String targetDir) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "PodApiImpl.copyToPod params:[namespace]={}, [podName]={},[containerName]={},[file]={},[targetDir]={}",namespace, podName,containerName,file.getAbsolutePath(),targetDir);
            client.pods().inNamespace(namespace).withName(podName)
                 .inContainer(containerName)
                 .file(targetDir)
                 .upload(file.toPath());
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.copyToPod error, params:[namespace]={}, [podName]={},[containerName]={},[file]={},[targetDir]={}, error:{}",namespace, podName,containerName,file.getAbsolutePath(),targetDir, e);
        }
    }

    /**
      * 同步执行
      * @param namespace 命名空间
      * @param podName pod名称
      * @param containerName 容器名称
      * @param cmd 命令
      */
    @Override
    public void exec(String namespace, String podName, String containerName, String cmd) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "PodApiImpl.exec params:[namespace]={}, [podName]={},[containerName]={},[cmd]={}",namespace, podName,containerName,cmd);
            final CountDownLatch execLatch = new CountDownLatch(1);
            ExecWatch execWatch = client.pods().inNamespace(namespace).withName(podName).inContainer(containerName)
                 .redirectingOutput()
                 .withTTY() //不展示输出
                 .usingListener(new DefaultPodExecListener(namespace, podName, containerName, execLatch))
                 .exec("sh", "-c", cmd);
            execLatch.await();
         } catch (InterruptedException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.exec error,params:[namespace]={}, [podName]={},[containerName]={},[cmd]={},error:{}",namespace, podName,containerName,cmd,e);
         }
    }

    /**
     * 设置pod间 ssh免密登录
     * @param podList pod 列表
     */
    @Override
    public void sshAuthentication(List<Pod> podList) {
        if (CollectionUtils.isEmpty(podList) || podList.size() == MagicNumConstant.ONE) {
            return;
        }
        LogUtil.info(LogEnum.BIZ_K8S, "PodApiImpl.sshAuthentication params:[podList]={}", podList.stream().map(p->p.getMetadata().getName()).collect(Collectors.toList()));
        File tempDir = Files.createTempDir();
        try (
                InputStream isRsa = getClass().getClassLoader().getResourceAsStream("key/id_rsa");
                InputStream isRsaPub = getClass().getClassLoader().getResourceAsStream("key/id_rsa.pub")
        ) {
            //id_rsa
            File tempIdRsa = FileUtil.createTempFile(tempDir);
            IOUtil.copy(isRsa, tempIdRsa);
            //id_rsa.pub
            File tempIdRsaPub = FileUtil.createTempFile(tempDir);
            IOUtil.copy(isRsaPub, tempIdRsaPub);
            List<String> pubLines = FileUtil.readLines(tempIdRsaPub, StringConstant.UTF8);
            String pubKeyContent = pubLines.get(0);
            //按机器修改id_rsa.pub, 并组装一个大而全的authorized_keys
            List<File> idRsaPubFiles = Lists.newArrayList();
            File tempAuthorizedKeys = FileUtil.createTempFile(tempDir);
            List<String> pubKeys = Lists.newArrayList();
            for (int i = 0; i < podList.size(); i++) {
                Pod podInfo = podList.get(i);
                String podPubKeyContent = pubKeyContent.replace("{{ip}}", podInfo.getStatus().getPodIP());
                File tempIdRsaPubOnPod = FileUtil.createTempFile(tempDir);
                FileUtil.writeLines(Collections.singletonList(podPubKeyContent), tempIdRsaPubOnPod, StringConstant.UTF8);
                idRsaPubFiles.add(tempIdRsaPubOnPod);
                pubKeys.add(podPubKeyContent);
            }
            FileUtil.writeLines(pubKeys, tempAuthorizedKeys, StringConstant.UTF8);

            //获得所有pod, 上传三个文件
            for (int i = 0; i < podList.size(); i++) {
                Pod pod = podList.get(i);
                String containerName = pod.getSpec().getContainers().get(MagicNumConstant.ZERO).getName();
                String namespace = pod.getMetadata().getNamespace();
                //上传id_rsa
                copyToPod(namespace, pod.getMetadata().getName(), containerName, tempIdRsa, "/root/.ssh/id_rsa");
                //上传id_rsa.pub
                File tempIdRsaPubOnPod = idRsaPubFiles.get(i);
                copyToPod(namespace, pod.getMetadata().getName(), containerName, tempIdRsaPubOnPod, "/root/.ssh/id_rsa.pub");
                //上传authorized_keys
                copyToPod(namespace, pod.getMetadata().getName(), containerName, tempAuthorizedKeys, "/root/.ssh/authorized_keys");
                //修改权限
                String chmodCmd = StrUtil.format("chmod 644 /root/.ssh/authorized_keys && chmod 600 /root/.ssh/id_rsa && chmod 644 /root/.ssh/id_rsa.pub");
                exec(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), containerName, chmodCmd);
            }
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PodApiImpl.sshAuthentication error,params:[podList]={},error:{}", podList, e);
        } finally {
            //清理临时文件
            FileUtil.del(tempDir);
        }
    }

    /**
     * 设置pod NODE_IPS 环境变量为 pod ip 列表
     * @param podList pod 列表
     */
    @Override
    public void setNodeIpsEnv(List<Pod> podList) {
        if (CollectionUtils.isEmpty(podList) || podList.size() == MagicNumConstant.ONE){
            return;
        }
        LogUtil.info(LogEnum.BIZ_K8S, "PodApiImpl.setNodeIpsEnv params:[podList]={}", podList.stream().map(p->p.getMetadata().getName()).collect(Collectors.toList()));
        List<String> nodeIpList = new ArrayList<>();
        for (int i = 0; i < podList.size(); i++){
            nodeIpList.add(podList.get(i).getStatus().getPodIP());
        }
        String nodeIps = JSON.toJSONString(nodeIpList);
        LogUtil.info(LogEnum.BIZ_K8S, "PodApiImpl.setNodeIpsEnv nodeIps={}", nodeIps);
        for (int i = 0; i < podList.size(); i++) {
            Pod pod = podList.get(i);
            String containerName = pod.getSpec().getContainers().get(MagicNumConstant.ZERO).getName();
            //设置 NODE_IPS 环境变量
            String chmodCmd = StrUtil.format("echo 'export NODE_IPS="+nodeIps+"' >> /root/.bashrc");
            exec(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), containerName, chmodCmd);
        }
    }

    /**
     * 验证访问Notebook的url
     *
     * @param jupyterUrl 访问Notebook的url
     * @return String jupyterUrl jupyter路径
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
