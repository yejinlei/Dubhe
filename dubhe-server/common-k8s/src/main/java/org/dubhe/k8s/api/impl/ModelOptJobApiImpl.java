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
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.ModelOptJobApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtModelOptimizationJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;
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
 * @description ModelOptJobApi 实现类
 * @date 2020-05-29
 */
public class ModelOptJobApiImpl implements ModelOptJobApi {
    private K8sUtils k8sUtils;
    private KubernetesClient client;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Autowired
    private NodeApi nodeApi;
    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;
    @Autowired
    private ResourceQuotaApi resourceQuotaApi;
    @Autowired
    private ResourceIisolationApi resourceIisolationApi;

    public ModelOptJobApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建模型优化 Job
     *
     * @param bo 模型优化 Job BO
     * @return BizJob Job 业务类
     */
    @Override
    public BizJob create(PtModelOptimizationJobBO bo) {
        try {
            LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(bo.getNamespace(), bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
                return new BizJob().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
            }
            LackOfResourcesEnum lack = nodeApi.isAllocatable(bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                return new BizJob().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), lack.getMessage());
            }
            LogUtil.info(LogEnum.BIZ_K8S, "Params of creating Job--create:{}", bo);
            if (!fileStoreApi.createDirs(bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))) {
                return new BizJob().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
            BizJob result = new JobDeployer(bo).buildVolumes().deploy();
            LogUtil.info(LogEnum.BIZ_K8S, "Return value of creating Job--create:{}", result);
            return result;
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ModelOptJobApiImpl.create error, param:{} error:{}", bo, e);
            return new BizJob().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 通过命名空间和资源名称查找Job资源
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizJob Job 业务类
     */
    @Override
    public BizJob getWithResourceName(String namespace, String resourceName) {
        if (StringUtils.isEmpty(namespace)) {
            return new BizJob().baseErrorBadRequest();
        }
        JobList bizJobList = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
        if (CollectionUtil.isEmpty(bizJobList.getItems())) {
            return new BizJob().error(K8sResponseEnum.NOT_FOUND.getCode(), K8sResponseEnum.NOT_FOUND.getMessage());
        }
        Job job = bizJobList.getItems().get(0);
        return BizConvertUtils.toBizJob(job);
    }

    /**
     * 通过命名空间查找Job资源
     *
     * @param namespace 命名空间
     * @return List<BizJob> Job 业务类集合
     */
    @Override
    public List<BizJob> getWithNamespace(String namespace) {
        List<BizJob> bizJobList = new ArrayList<>();
        JobList jobList = client.batch().jobs().inNamespace(namespace).list();
        if (CollectionUtil.isEmpty(jobList.getItems())) {
            return bizJobList;
        }
        return BizConvertUtils.toBizJobList(jobList.getItems());
    }

    /**
     * 查询所有Job资源
     *
     * @return List<BizJob> Job 业务类集合
     */
    @Override
    public List<BizJob> listAll() {
        return client.batch().jobs().inAnyNamespace().list()
                .getItems().parallelStream().map(obj -> BizConvertUtils.toBizJob(obj)).collect(Collectors.toList());
    }

    /**
     * 通过命名空间和资源名删除Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult  基础结果类
     */
    @Override
    public PtBaseResult deleteByResourceName(String namespace, String resourceName) {
        LogUtil.info(LogEnum.BIZ_K8S, "Param of deleteByResourceName namespace {} resourceName {}", namespace,resourceName);
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ModelOptJobApiImpl.deleteByResourceName error, param:[namespace]={}, [resourceName]={}, error:{}", namespace, resourceName, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    private class JobDeployer {
        private String baseName;
        private String jobName;

        private String namespace;
        private String image;

        private List<String> cmdLines;

        private Map<String, PtMountDirBO> fsMounts;

        private Map<String, Quantity> resourcesLimitsMap;
        private Map<String, String> baseLabels;
        private List<VolumeMount> volumeMounts;
        private List<Volume> volumes;
        private String businessLabel;
        private String taskIdentifyLabel;
        private Integer gpuNum;

        private String errCode;
        private String errMessage;

        private JobDeployer(PtModelOptimizationJobBO bo) {
            this.baseName = bo.getName();
            this.jobName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, baseName, RandomUtil.randomString(MagicNumConstant.EIGHT));
            this.namespace = bo.getNamespace();
            this.image = bo.getImage();
            this.cmdLines = new ArrayList();
            this.gpuNum = bo.getGpuNum();
            Optional.ofNullable(bo.getCmdLines()).ifPresent(v -> cmdLines = v);

            this.resourcesLimitsMap = Maps.newHashMap();
            Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
            Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.GPU_RESOURCE_KEY, new Quantity(v.toString())));
            Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
            this.businessLabel = bo.getBusinessLabel();
            this.taskIdentifyLabel = bo.getTaskIdentifyLabel();
            this.fsMounts = bo.getFsMounts();
            this.baseLabels = LabelUtils.getBaseLabels(baseName, businessLabel);

            this.volumeMounts = new ArrayList<>();
            this.volumes = new ArrayList<>();
            this.errCode = K8sResponseEnum.SUCCESS.getCode();
            this.errMessage = SymbolConstant.BLANK;
        }

        /**
         * 部署Job
         *
         * @return BizJob Job业务类
         */
        public BizJob deploy() {
            if (!K8sResponseEnum.SUCCESS.getCode().equals(errCode)) {
                return new BizJob().error(errCode, errMessage);
            }
            //部署job
            try {
                Job job = deployJob();
                return BizConvertUtils.toBizJob(job);
            } catch (KubernetesClientException e) {
                LogUtil.error(LogEnum.BIZ_K8S, "ModelOptJobApiImpl.deploy error:{}", e);
                return new BizJob().error(String.valueOf(e.getCode()), e.getMessage());
            }
        }

        /**
         * 挂载存储
         *
         * @return JobDeployer Job 部署类
         */
        private JobDeployer buildVolumes() {
            if (CollectionUtil.isNotEmpty(fsMounts)) {
                int i = MagicNumConstant.ZERO;
                for (Map.Entry<String, PtMountDirBO> mount : fsMounts.entrySet()) {
                    boolean availableMount = (mount != null && StringUtils.isNotEmpty(mount.getKey()) && mount.getValue() != null && StringUtils.isNotEmpty(mount.getValue().getDir()));
                    if (availableMount) {
                        boolean success = mount.getValue().isRecycle() ? buildFsPvcVolumes(mount.getKey(), mount.getValue(), i) : buildFsVolumes(mount.getKey(), mount.getValue(), i);
                        if (!success) {
                            break;
                        }
                        i++;
                    }
                }
            }
            return this;
        }

        /**
         * 挂载存储
         *
         * @param mountPath 挂载路径
         * @param dirBO 挂载路径参数
         * @param i 名称序号
         * @return boolean true成功 false失败
         */
        private boolean buildFsVolumes(String mountPath, PtMountDirBO dirBO, int i) {
            volumeMounts.add(new VolumeMountBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX + i)
                    .withMountPath(mountPath)
                    .withReadOnly(dirBO.isReadOnly())
                    .build());
            volumes.add(new VolumeBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX + i)
                    .withNewHostPath()
                        .withPath(dirBO.getDir())
                        .withType(K8sParamConstants.HOST_PATH_TYPE)
                    .endHostPath()
                    .build());
            return true;
        }

        /**
         * 按照存储资源声明挂载存储
         *
         * @param mountPath 挂载路径
         * @param dirBO 挂载路径参数
         * @param i 名称序号
         * @return boolean true成功 false失败
         */
        private boolean buildFsPvcVolumes(String mountPath, PtMountDirBO dirBO, int i) {
            BizPersistentVolumeClaim bizPersistentVolumeClaim = persistentVolumeClaimApi.createWithFsPv(new PtPersistentVolumeClaimBO(namespace, baseName, dirBO));
            if (bizPersistentVolumeClaim.isSuccess()) {
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(K8sParamConstants.VOLUME_PREFIX + i)
                        .withMountPath(mountPath)
                        .withReadOnly(dirBO.isReadOnly())
                        .build());
                volumes.add(new VolumeBuilder()
                        .withName(K8sParamConstants.VOLUME_PREFIX + i)
                        .withNewPersistentVolumeClaim(bizPersistentVolumeClaim.getName(), dirBO.isReadOnly())
                        .build());
                return true;
            } else {
                this.errCode = bizPersistentVolumeClaim.getCode();
                this.errMessage = bizPersistentVolumeClaim.getMessage();
            }
            return false;
        }

        /**
         * 检查是否已经存在，存在则返回
         *
         * @return Job 任务类
         */
        private Job alreadyHaveJob() {
            JobList list = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(baseName)).list();
            if (CollectionUtil.isNotEmpty(list.getItems())) {
                Job job = list.getItems().get(0);
                LogUtil.info(LogEnum.BIZ_K8S, "Skip creating job, {} already exists", job.getMetadata().getName());
                return job;
            }
            return null;
        }

        /**
         * 部署Job
         *
         * @return Job 任务job类
         */
        private Job deployJob() {
            //已经存在直接返回
            Job job = alreadyHaveJob();
            if (job != null) {
                return job;
            }
            job = buildJob();
            resourceIisolationApi.addIisolationInfo(job);
            LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(job));
            job = client.batch().jobs().inNamespace(namespace).create(job);
            return job;
        }

        /**
         * 构建Job
         *
         * @return Job 任务job类
         */
        private Job buildJob() {
            Map<String, String> childLabels = LabelUtils.getChildLabels(baseName, jobName, K8sKindEnum.JOB.getKind(), businessLabel, taskIdentifyLabel);
            return new JobBuilder()
                    .withNewMetadata()
                        .withName(jobName)
                        .addToLabels(baseLabels)
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                        .withBackoffLimit(0)
                        .withParallelism(1)
                        .withNewTemplate()
                            .withNewMetadata()
                                .withName(jobName)
                                .addToLabels(childLabels)
                                .withNamespace(namespace)
                            .endMetadata()
                            .withNewSpec()
                                .addToNodeSelector(gpuSelector())
                                .addToContainers(buildContainer())
                                .addToVolumes(volumes.toArray(new Volume[0]))
                                .withRestartPolicy(RestartPolicyEnum.NEVER.getRestartPolicy())
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
        }

        /**
         * 添加Gpu label
         *
         * @return Map<String, String> Gpu label键值
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
         * @return Container 容器类
         */
        private Container buildContainer() {
            return new ContainerBuilder()
                    .withNewName(jobName)
                    .withNewImage(image)
                    .withNewImagePullPolicy(ImagePullPolicyEnum.IFNOTPRESENT.getPolicy())
                    .withVolumeMounts(volumeMounts)
                    .addNewCommand(ShellCommandEnum.BASH.getShell())
                    .addAllToArgs(cmdLines)
                    .withNewResources().addToLimits(resourcesLimitsMap).endResources()
                    .build();
        }
    }
}
