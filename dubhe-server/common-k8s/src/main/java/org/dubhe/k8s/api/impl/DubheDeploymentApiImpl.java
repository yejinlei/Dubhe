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
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.DubheDeploymentApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtModelOptimizationDeploymentBO;
import org.dubhe.k8s.domain.resource.BizDeployment;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.LimitsOfResourcesEnum;
import org.dubhe.k8s.enums.RestartPolicyEnum;
import org.dubhe.k8s.enums.ShellCommandEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.k8s.utils.YamlUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description DubheDeploymentApi 实现类
 * @date 2020-05-26
 */
public class DubheDeploymentApiImpl implements DubheDeploymentApi {
    private K8sUtils k8sUtils;
    private KubernetesClient client;

    @Autowired
    private NodeApi nodeApi;
    @Autowired
    private ResourceCache resourceCache;
    @Autowired
    private ResourceQuotaApi resourceQuotaApi;
    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Autowired
    private ResourceIisolationApi resourceIisolationApi;

    private static final String DATASET = "/dataset";
    private static final String WORKSPACE = "/workspace";
    private static final String OUTPUT = "/output";

    private static final String PVC_DATASET = "pvc-dataset";
    private static final String PVC_WORKSPACE = "pvc-workspace";
    private static final String PVC_OUTPUT = "pvc-output";

    public DubheDeploymentApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建模型压缩Deployment
     *
     * @param bo 模型压缩 Deployment BO
     * @return BizDeployment Deployment 业务类
     */
    @Override
    public BizDeployment create(PtModelOptimizationDeploymentBO bo) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "Param of create:{}", bo);
            LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(bo.getNamespace(), bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
                return new BizDeployment().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
            }
            LackOfResourcesEnum lack = nodeApi.isAllocatable(bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                return new BizDeployment().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), lack.getMessage());
            }
            if (!fileStoreApi.createDirs(bo.getWorkspaceDir(), bo.getDatasetDir(), bo.getOutputDir())) {
                return new BizDeployment().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
            resourceCache.deletePodCacheByResourceName(bo.getNamespace(), bo.getName());
            return new DeploymentDeployer(bo).deploy();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "DeploymentApiImpl.create error, param:{} error:{}", bo, e);
            return new BizDeployment().error(String.valueOf(e.getCode()), e.getMessage());
        }

    }

    /**
     * 通过命名空间和资源名称查找Deployment资源
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizDeployment Deployment 业务类
     */
    @Override
    public BizDeployment getWithResourceName(String namespace, String resourceName) {
        try {
            if (StringUtils.isEmpty(namespace)) {
                return new BizDeployment().baseErrorBadRequest();
            }
            DeploymentList bizDeploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (CollectionUtil.isEmpty(bizDeploymentList.getItems())) {
                return new BizDeployment().error(K8sResponseEnum.NOT_FOUND.getCode(), K8sResponseEnum.NOT_FOUND.getMessage());
            }
            Deployment deployment = bizDeploymentList.getItems().get(0);
            return BizConvertUtils.toBizDeployment(deployment);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "DeploymentApiImpl.getWithResourceName error, param:[namespace]={}, [resourceName]={}, error:{}", namespace, resourceName, e);
            return new BizDeployment().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 通过命名空间查找Deployment资源集合
     *
     * @param namespace 命名空间
     * @return List<BizDeployment> Deployment 业务类集合
     */
    @Override
    public List<BizDeployment> getWithNamespace(String namespace) {
        List<BizDeployment> bizDeploymentList = new ArrayList<>();
        DeploymentList deploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName()).list();
        if (CollectionUtil.isEmpty(deploymentList.getItems())) {
            return bizDeploymentList;
        }
        return BizConvertUtils.toBizDeploymentList(deploymentList.getItems());
    }

    /**
     * 查询集群所有Deployment资源
     *
     * @return List<BizDeployment> Deployment 业务类集合
     */
    @Override
    public List<BizDeployment> listAll() {
        return client.apps().deployments().inAnyNamespace().withLabels(LabelUtils.withEnvResourceName()).list().getItems().parallelStream().map(obj -> BizConvertUtils.toBizDeployment(obj)).collect(Collectors.toList());
    }

    /**
     * 通过资源名进行删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult deleteByResourceName(String namespace, String resourceName) {
        LogUtil.info(LogEnum.BIZ_K8S, "Param of deleteByResourceName:namespace {} resourceName {}", namespace,resourceName);
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "DeploymentApiImpl.deleteByResourceName error, param:[namespace]={}, [resourceName]={}, error:{}", namespace, resourceName, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    private class DeploymentDeployer {
        private String baseName;
        private String deploymentName;

        private String namespace;
        private String image;
        private String datasetDir;
        private String datasetMountPath;
        private String workspaceDir;
        private String workspaceMountPath;
        private String outputDir;
        private String outputMountPath;

        private List<String> cmdLines;
        //数据集默认只读
        private boolean datasetReadOnly;

        private Map<String, Quantity> resourcesLimitsMap;
        private Map<String, String> baseLabels;
        private String businessLabel;
        private String taskIdentifyLabel;
        private Integer gpuNum;


        private DeploymentDeployer(PtModelOptimizationDeploymentBO bo) {
            this.baseName = bo.getName();
            this.deploymentName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, baseName, RandomUtil.randomString(MagicNumConstant.EIGHT));
            this.namespace = bo.getNamespace();
            this.image = bo.getImage();
            this.datasetDir = bo.getDatasetDir();
            this.datasetMountPath = StringUtils.isEmpty(bo.getDatasetMountPath()) ? DATASET : bo.getDatasetMountPath();
            this.workspaceDir = bo.getWorkspaceDir();
            this.workspaceMountPath = StringUtils.isEmpty(bo.getWorkspaceMountPath()) ? WORKSPACE : bo.getWorkspaceMountPath();
            this.outputDir = bo.getOutputDir();
            this.outputMountPath = StringUtils.isEmpty(bo.getOutputMountPath()) ? OUTPUT : bo.getOutputMountPath();
            this.cmdLines = new ArrayList();
            this.gpuNum = bo.getGpuNum();
            Optional.ofNullable(bo.getDatasetReadOnly()).ifPresent(v -> datasetReadOnly = v);
            Optional.ofNullable(bo.getCmdLines()).ifPresent(v -> cmdLines = v);

            this.resourcesLimitsMap = Maps.newHashMap();
            Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
            Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.GPU_RESOURCE_KEY, new Quantity(v.toString())));
            Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
            this.businessLabel = bo.getBusinessLabel();
            this.taskIdentifyLabel = bo.getTaskIdentifyLabel();
            this.baseLabels = LabelUtils.getBaseLabels(baseName, businessLabel);

            this.datasetReadOnly = true;
        }

        /**
         * 部署Deployment
         *
         * @return BizDeployment Deployment 业务类
         */
        public BizDeployment deploy() {
            //部署deployment
            try {
                Deployment deployment = deployDeployment();
                return BizConvertUtils.toBizDeployment(deployment);
            } catch (KubernetesClientException e) {
                LogUtil.error(LogEnum.BIZ_K8S, "DeploymentApiImpl.deploy error:{}", e);
                return (BizDeployment) new PtBaseResult().error(String.valueOf(e.getCode()), e.getMessage());
            }

        }

        /**
         * 检查资源是否已经存在
         *
         * @return Deployment Deployment 业务类
         */
        private Deployment alreadyHaveDeployment() {
            DeploymentList list = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(baseName)).list();
            if (CollectionUtil.isNotEmpty(list.getItems())) {
                Deployment deployment = list.getItems().get(0);
                LogUtil.info(LogEnum.BIZ_K8S, "Skip creating job, {} already exists", deployment.getMetadata().getName());
                return deployment;
            }
            return null;
        }

        /**
         * 部署Deployment
         *
         * @return Deployment Deployment 业务类
         */
        private Deployment deployDeployment() {
            //已经存在直接返回
            Deployment deployment = alreadyHaveDeployment();
            if (deployment != null) {
                return deployment;
            }
            deployment = buildDeployment();
            LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(deployment));
            resourceIisolationApi.addIisolationInfo(deployment);
            deployment = client.apps().deployments().inNamespace(namespace).create(deployment);
            return deployment;
        }

        /**
         * 构建Deployment
         *
         * @return Deployment Deployment 业务类
         */
        private Deployment buildDeployment() {
            Map<String, String> childLabels = LabelUtils.getChildLabels(baseName, deploymentName, K8sKindEnum.DEPLOYMENT.getKind(), businessLabel, taskIdentifyLabel);
            LabelSelector labelSelector = new LabelSelector();
            labelSelector.setMatchLabels(childLabels);
            return new DeploymentBuilder()
                    .withNewMetadata()
                        .withName(deploymentName)
                        .addToLabels(baseLabels)
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                        .withSelector(labelSelector)
                        .withNewTemplate()
                            .withNewMetadata()
                                .withName(deploymentName)
                                .addToLabels(childLabels)
                                .withNamespace(namespace)
                            .endMetadata()
                            .withNewSpec()
                                .addToNodeSelector(gpuSelector())
                                .addToContainers(buildContainer())
                                .addToVolumes(buildVolume().toArray(new Volume[0]))
                                .withRestartPolicy(RestartPolicyEnum.ALWAYS.getRestartPolicy())
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
        }

        /**
         * 添加Gpu label
         *
         * @return Map<String, String> Gpu label 键值
         */
        private Map<String, String> gpuSelector() {
            Map<String, String> gpuSelector = new HashMap<>(2);
            if (gpuNum != null && gpuNum > 0) {
                gpuSelector.put(K8sLabelConstants.NODE_GPU_LABEL_KEY, K8sLabelConstants.NODE_GPU_LABEL_VALUE);
            }
            return gpuSelector;
        }

        /**
         * 构建Container
         *
         * @return Container  容器
         */
        private Container buildContainer() {
            return new ContainerBuilder()
                    .withNewName(deploymentName)
                    .withNewImage(image)
                    .withNewImagePullPolicy(ImagePullPolicyEnum.IFNOTPRESENT.getPolicy())
                    .withVolumeMounts(buildVolumeMount())
                    .addNewCommand(ShellCommandEnum.BIN_BANSH.getShell())
                    .addAllToArgs(cmdLines)
                    .withNewResources().addToLimits(resourcesLimitsMap).endResources()
                    .build();
        }

        /**
         * 构建VolumeMount
         *
         * @return List<VolumeMount> VolumeMount集合类
         */
        private List<VolumeMount> buildVolumeMount() {
            List<VolumeMount> volumeMounts = new ArrayList<>();
            if (StrUtil.isNotBlank(datasetDir)) {
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(PVC_DATASET)
                        .withMountPath(datasetMountPath)
                        .withReadOnly(datasetReadOnly)
                        .build());
            }
            if (StrUtil.isNotBlank(workspaceDir)) {
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(PVC_WORKSPACE)
                        .withMountPath(workspaceMountPath)
                        .withReadOnly(false)
                        .build());
            }
            if (StrUtil.isNotBlank(outputDir)) {
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(PVC_OUTPUT)
                        .withMountPath(outputMountPath)
                        .withReadOnly(false)
                        .build());
            }
            return volumeMounts;
        }

        /**
         * 构建Volume
         *
         * @return List<Volume> Volume集合类
         */
        private List<Volume> buildVolume() {
            List<Volume> volumes = new ArrayList<>();
            if (StrUtil.isNotBlank(datasetDir)) {
                volumes.add(new VolumeBuilder()
                        .withName(PVC_DATASET)
                        .withNewHostPath()
                            .withPath(datasetDir)
                            .withType(K8sParamConstants.HOST_PATH_TYPE)
                        .endHostPath()
                        .build());
            }
            if (StrUtil.isNotBlank(workspaceDir)) {
                volumes.add(new VolumeBuilder()
                        .withName(PVC_WORKSPACE)
                        .withNewHostPath()
                            .withPath(workspaceDir)
                            .withType(K8sParamConstants.HOST_PATH_TYPE)
                        .endHostPath()
                        .build());
            }
            if (StrUtil.isNotBlank(outputDir)) {
                volumes.add(new VolumeBuilder()
                        .withName(PVC_OUTPUT)
                        .withNewHostPath()
                            .withPath(outputDir)
                            .withType(K8sParamConstants.HOST_PATH_TYPE)
                        .endHostPath()
                        .build());
            }
            return volumes;
        }
    }
}
