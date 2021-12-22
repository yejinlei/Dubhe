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
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.*;
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
import org.dubhe.k8s.api.*;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.*;
import org.dubhe.k8s.domain.entity.K8sTask;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.k8s.enums.*;
import org.dubhe.k8s.service.K8sGpuConfigService;
import org.dubhe.k8s.service.K8sTaskService;
import org.dubhe.k8s.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description TrainJobApi实现类
 * @date 2020-04-22
 */
public class TrainJobApiImpl implements TrainJobApi {

    private K8sUtils k8sUtils;

    private KubernetesClient client;

    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;
    @Autowired
    private NodeApi nodeApi;

    @Autowired
    private K8sTaskService k8sTaskService;
    @Autowired
    private ResourceCache resourceCache;
    @Autowired
    private ResourceQuotaApi resourceQuotaApi;
    @Autowired
    private ResourceIisolationApi resourceIisolationApi;
    @Autowired
    private K8sGpuConfigService k8sGpuConfigService;

    @Autowired
    private K8sCommonUtils k8sCommonUtils;

    public TrainJobApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建训练任务 Job
     *
     * @param bo 训练任务 Job BO
     * @return PtJupyterJobVO 训练任务 Job 结果类
     */
    @Override
    public PtJupyterJobVO create(PtJupyterJobBO bo) {
        try {
            BaseResourceBo baseResourceBo = new BaseResourceBo();
            BeanUtils.copyProperties(bo, baseResourceBo);
            LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(baseResourceBo);

            if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
                return new PtJupyterJobVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
            }
            if (bo.getUseGpu()) {
                Integer k8sGpuNumLimit = k8sGpuConfigService.getGpuLimit(bo.getNamespace(), bo.getGpuModel(), bo.getK8sLabelKey());
                if(bo.getGpuNum() > k8sGpuNumLimit){
                    return new PtJupyterJobVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), LimitsOfResourcesEnum.LIMITS_OF_GPU.getMessage());
                }
            }
            LackOfResourcesEnum lack = nodeApi.isAllocatable(baseResourceBo);
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                return new PtJupyterJobVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), lack.getMessage());
            }
            LogUtil.info(LogEnum.BIZ_K8S, "Params of creating Job--create:{}", bo);
            if (!fileStoreApi.createDirs(bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))) {
                return new PtJupyterJobVO().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
            resourceCache.deletePodCacheByResourceName(bo.getNamespace(),bo.getName());
            PtJupyterJobVO result = new JupyterDeployer(bo).buildVolumes().deploy();
            LogUtil.info(LogEnum.BIZ_K8S,"Return value of creating Job--create:{}", result);
            return result;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S,"TrainJobApiImpl.create error, param:{} error:{}", bo, e);
            return new PtJupyterJobVO().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据命名空间和资源名删除Job
     *
     * @param namespace    命名空间
     * @param resourceName 资源名称
     * @return Boolean true成功 false失败
     */
    @Override
    public Boolean delete(String namespace, String resourceName){
        try {
            LogUtil.info(LogEnum.BIZ_K8S,"Params of delete Job--namespace:{}, resourceName:{}",namespace, resourceName);
            k8sTaskService.deleteByNamespaceAndResourceName(namespace,resourceName);
            JobList jobList = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (jobList == null || jobList.getItems().size() == 0){
                return true;
            }
            return client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "TrainJobApiImpl.delete error, param:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName,e);
            return false;
        }
    }

    /**
     * 根据命名空间查询Job
     *
     * @param namespace 命名空间
     * @return List<BizJob> Job业务类集合
     */
    @Override
    public List<BizJob> list(String namespace){
        JobList list = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName()).list();
        if(CollectionUtil.isEmpty(list.getItems())){
            return null;
        }
        return list.getItems().stream().map(item -> BizConvertUtils.toBizJob(item)).collect(Collectors.toList());
    }

    /**
     * 根据命名空间和资源名查询Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return BizJob Job业务类
     */
    @Override
    public BizJob get(String namespace, String resourceName){
        try {
            JobList list = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if(CollectionUtil.isEmpty(list.getItems())){
                return null;
            }
            Job job = list.getItems().get(0);
            return BizConvertUtils.toBizJob(job);
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "TrainJobApiImpl.get error, param:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName,e);
            return new BizJob().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    private class JupyterDeployer{
        private String baseName;
        private String jobName;

        private String namespace;
        private String image;
        private Boolean useGpu;
        private String gpuModel;
        private List<String> cmdLines;
        private Map<String, PtMountDirBO> fsMounts;

        private Map<String, Quantity> resourcesLimitsMap;
        private Map<String, String> baseLabels;

        private List<VolumeMount> volumeMounts;
        private List<Volume> volumes;
        private String businessLabel;
        private String taskIdentifyLabel;
        private Integer delayCreate;
        private Integer delayDelete;
        private TaskYamlBO taskYamlBO;
        private String errCode;
        private String errMessage;

        private JupyterDeployer(PtJupyterJobBO bo){
            this.baseName = bo.getName();
            this.jobName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, baseName, RandomUtil.randomString(K8sParamConstants.RESOURCE_NAME_SUFFIX_LENGTH));
            this.namespace = bo.getNamespace();
            this.image = bo.getImage();
            this.cmdLines = new ArrayList();
            this.gpuModel = bo.getGpuModel();
            Optional.ofNullable(bo.getCmdLines()).ifPresent(v -> cmdLines = v);
            this.useGpu = bo.getUseGpu()==null?false:bo.getUseGpu();
            if (bo.getUseGpu() != null && bo.getUseGpu() && null == bo.getGpuNum()){
                bo.setGpuNum(MagicNumConstant.ZERO);
            }

            this.resourcesLimitsMap = Maps.newHashMap();
            Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
            Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(bo.getK8sLabelKey(), new Quantity(v.toString())));
            Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
            k8sCommonUtils.addRdmaResource(resourcesLimitsMap);

            this.fsMounts = bo.getFsMounts();
            businessLabel = bo.getBusinessLabel();
            this.baseLabels = LabelUtils.getBaseLabels(baseName,bo.getBusinessLabel(),bo.getExtraLabelMap());
            this.taskIdentifyLabel = bo.getTaskIdentifyLabel();

            this.volumeMounts = new ArrayList<>();
            this.volumes = new ArrayList<>();
            this.delayCreate = bo.getDelayCreateTime();
            this.delayDelete = bo.getDelayDeleteTime();
            this.taskYamlBO = new TaskYamlBO();
            this.errCode = K8sResponseEnum.SUCCESS.getCode();
            this.errMessage = SymbolConstant.BLANK;
        }

        /**
         * 部署Job
         *
         * @return PtJupyterJobVO 训练任务 Job 结果类
         */
        public PtJupyterJobVO deploy()  {
            delayCreate = delayCreate == null || delayCreate <= 0 ? MagicNumConstant.ZERO : delayCreate;
            delayDelete = delayDelete == null || delayDelete <= 0 ? MagicNumConstant.ZERO : delayDelete;
            if (!K8sResponseEnum.SUCCESS.getCode().equals(errCode)){
                return new PtJupyterJobVO().error(errCode,errMessage);
            }
            //部署job
            Job job = deployJob(delayCreate, delayDelete);
            if (CollectionUtil.isNotEmpty(taskYamlBO.getYamlList()) && (delayCreate > MagicNumConstant.ZERO || delayDelete > MagicNumConstant.ZERO)){
                long applyUnixTime = System.currentTimeMillis()/MagicNumConstant.THOUSAND_LONG + delayCreate*MagicNumConstant.SIXTY_LONG;
                Timestamp applyDisplayTime = new Timestamp(applyUnixTime * MagicNumConstant.THOUSAND_LONG);
                long stopUnixTime = applyUnixTime + delayDelete* MagicNumConstant.SIXTY_LONG;
                Timestamp stopDisplayTime = new Timestamp(stopUnixTime * MagicNumConstant.THOUSAND_LONG);
                K8sTask k8sTask = new K8sTask(){{
                    setNamespace(namespace);
                    setResourceName(baseName);
                    setTaskYaml(JSON.toJSONString(taskYamlBO));
                    setBusiness(businessLabel);
                    setApplyUnixTime(applyUnixTime);
                    setApplyDisplayTime(applyDisplayTime);
                    setApplyStatus(delayCreate == MagicNumConstant.ZERO ? MagicNumConstant.ZERO : MagicNumConstant.ONE);
                }};
                if (delayDelete > MagicNumConstant.ZERO){
                    k8sTask.setStopUnixTime(stopUnixTime);
                    k8sTask.setStopDisplayTime(stopDisplayTime);
                    k8sTask.setStopStatus(MagicNumConstant.ONE);
                }
                k8sTaskService.createOrUpdateTask(k8sTask);
            }

            return PtJupyterJobVO.getInstance(job);
        }

        /**
         * 挂载存储
         *
         * @return JupyterDeployer Jupyter Job 部署类
         */
        private JupyterDeployer buildVolumes(){

            // 针对于共享内存挂载存储
            buildSharedMemoryVolume();

            if (CollectionUtil.isNotEmpty(fsMounts)){
                int i = MagicNumConstant.ZERO;
                for (Map.Entry<String, PtMountDirBO> mount : fsMounts.entrySet()) {
                    boolean availableMount = (mount != null && StringUtils.isNotEmpty(mount.getKey()) && mount.getValue() != null && StringUtils.isNotEmpty(mount.getValue().getDir()));
                    if (availableMount){
                        boolean success = mount.getValue().isRecycle()?buildFsPvcVolumes(mount.getKey(),mount.getValue(),i):buildFsVolumes(mount.getKey(),mount.getValue(),i);
                        if (!success){
                            break;
                        }
                        i++;
                    }
                }
            }
            return this;
        }

        /**
         * 针对于共享内存挂载存储
         *
         */
        private void buildSharedMemoryVolume(){
            volumeMounts.add(new VolumeMountBuilder()
                    .withName(K8sParamConstants.SHM_NAME)
                    .withMountPath(K8sParamConstants.SHM_MOUNTPATH)
                    .build());
            volumes.add(new VolumeBuilder()
                    .withName(K8sParamConstants.SHM_NAME)
                    .withNewEmptyDir()
                    .withMedium(K8sParamConstants.SHM_MEDIUM)
                    .endEmptyDir()
                    .build());
        }

        /**
         * 挂载存储
         *
         * @param mountPath 挂载路径
         * @param dirBO     挂载路径参数
         * @param num       名称序号
         * @return boolean true成功 false失败
         */
        private boolean buildFsVolumes(String mountPath,PtMountDirBO dirBO,int num){
            volumeMounts.add(new VolumeMountBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+num)
                    .withMountPath(mountPath)
                    .withReadOnly(dirBO.isReadOnly())
                    .build());
            volumes.add(new VolumeBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+num)
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
         * @param dirBO     挂载路径参数
         * @param i         名称序号
         * @return boolean true成功 false失败
         */
        private boolean buildFsPvcVolumes(String mountPath,PtMountDirBO dirBO,int i){
            BizPersistentVolumeClaim bizPersistentVolumeClaim = persistentVolumeClaimApi.createWithFsPv(new PtPersistentVolumeClaimBO(namespace,baseName,dirBO));
            if (bizPersistentVolumeClaim.isSuccess()){
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(K8sParamConstants.VOLUME_PREFIX+i)
                        .withMountPath(mountPath)
                        .withReadOnly(dirBO.isReadOnly())
                        .build());
                volumes.add(new VolumeBuilder()
                        .withName(K8sParamConstants.VOLUME_PREFIX+i)
                        .withNewPersistentVolumeClaim(bizPersistentVolumeClaim.getName(), dirBO.isReadOnly())
                        .build());
                return true;
            }else {
                this.errCode = bizPersistentVolumeClaim.getCode();
                this.errMessage = bizPersistentVolumeClaim.getMessage();
            }
            return false;
        }

        /**
         * 部署Job
         *
         * @param delayCreate 创建
         * @param delayDelete 删除
         * @return Job job类
         */
        private Job deployJob(Integer delayCreate, Integer delayDelete) {

            Job job = null;
            JobList list = client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(baseName)).list();
            if(CollectionUtil.isNotEmpty(list.getItems())){
                job = list.getItems().get(0);
                jobName = job.getMetadata().getName();
                boolean succeedOrFailed = (job.getStatus().getSucceeded() != null && job.getStatus().getSucceeded() > 0) || (job.getStatus().getFailed() != null && job.getStatus().getFailed() > 0);
                if(succeedOrFailed){
                    LogUtil.info(LogEnum.BIZ_K8S, "Delete existing job {}", jobName);
                    client.resource(job).delete();
                }else{
                    LogUtil.info(LogEnum.BIZ_K8S, "Skip creating job, {} already exists", jobName);
                    return job;
                }
            }
            //容器
            Container container = new Container();

            //环境变量
            List<EnvVar> env = new ArrayList();
            env.add(new EnvVarBuilder().withName(K8sParamConstants.PYTHONUNBUFFERED).withValue(SymbolConstant.ZERO).build());
            container.setEnv(env);

            //镜像
            container.setName(jobName);
            container.setImage(image);
            container.setImagePullPolicy(ImagePullPolicyEnum.ALWAYS.getPolicy());
            container.setVolumeMounts(volumeMounts);
            //启动命令
            container.setCommand(Collections.singletonList(ShellCommandEnum.BIN_BANSH.getShell()));
            container.setArgs(cmdLines);

            //资源限制
            container.setResources(new ResourceRequirementsBuilder()
                    .addToLimits(resourcesLimitsMap)
                    .build());

            Map<String, String> gpuLabel = new HashMap<String, String>(2);
            if (useGpu) {
                gpuLabel.put(K8sLabelConstants.NODE_GPU_LABEL_KEY, K8sLabelConstants.NODE_GPU_LABEL_VALUE);
                gpuLabel.put(K8sLabelConstants.NODE_GPU_MODEL_LABEL_KEY, gpuModel);
            }

            job = new JobBuilder()
                    .withNewMetadata()
                        .withName(jobName)
                        .addToLabels(baseLabels)
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                        .withParallelism(1)
                        .withCompletions(1)
                        .withBackoffLimit(0)
                        .withNewTemplate()
                            .withNewMetadata()
                                .withName(jobName)
                                .addToLabels(LabelUtils.getChildLabels(baseName, jobName, K8sKindEnum.JOB.getKind(),businessLabel,taskIdentifyLabel,baseLabels))
                                .withNamespace(namespace)
                            .endMetadata()
                            .withNewSpec()
                                .withTerminationGracePeriodSeconds(MagicNumConstant.ZERO_LONG)
                                .addToNodeSelector(gpuLabel)
                                .addToContainers(container)
                                .addToVolumes(volumes.toArray(new Volume[0]))
                                .withRestartPolicy(RestartPolicyEnum.NEVER.getRestartPolicy())
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
            if (delayCreate == null || delayCreate == MagicNumConstant.ZERO){
                resourceIisolationApi.addIisolationInfo(job);
                LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}", jobName);
                job = client.batch().jobs().create(job);
                LogUtil.info(LogEnum.BIZ_K8S, "{} deployed successfully", jobName);
            }
            if (delayCreate > MagicNumConstant.ZERO || delayDelete > MagicNumConstant.ZERO) {
                taskYamlBO.append(job);
            }
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml info is : {}", jobName, YamlUtils.dumpAsYaml(job));
            return job;
        }
    }
}
