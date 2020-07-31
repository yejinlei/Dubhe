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
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.base.MagicNumConstant;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.enums.LogEnum;
import org.dubhe.k8s.api.LogMonitoringApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.TrainJobApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.PtJupyterJobBO;
import org.dubhe.k8s.domain.bo.PtMountDirBO;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.dubhe.k8s.domain.resource.BizJob;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;
import org.dubhe.k8s.domain.resource.BizPod;
import org.dubhe.k8s.domain.vo.PtJupyterJobVO;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.RestartPolicyEnum;
import org.dubhe.k8s.enums.ShellCommandEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.k8s.utils.YamlUtils;
import org.dubhe.utils.LogUtil;
import org.dubhe.utils.NfsUtil;
import org.dubhe.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description TrainJobApi实现类
 * @date 2020-04-22
 */
public class TrainJobApiImpl implements TrainJobApi {

    private K8sUtils k8sUtils;
    private KubernetesClient client;

    @Autowired
    private NfsUtil nfsUtil;

    @Autowired
    private PersistentVolumeClaimApi persistentVolumeClaimApi;
    @Autowired
    private NodeApi nodeApi;
    @Autowired
    private PodApi podApi;
    @Autowired
    private LogMonitoringApi logMonitoringApi;

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
    public PtJupyterJobVO create(PtJupyterJobBO bo)  {
        try{
            LackOfResourcesEnum lack = nodeApi.isAllocatable(bo.getCpuNum(),bo.getMemNum(),bo.getGpuNum());
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)){
                return new PtJupyterJobVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(),lack.getMessage());
            }
            LogUtil.info(LogEnum.BIZ_K8S,"Params of creating Job--create:{}", bo);
            if(!nfsUtil.createDirs(true,bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))){
                return new PtJupyterJobVO().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(),K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
            PtJupyterJobVO result = new JupyterDeployer(bo).buildVolumes().deploy();
            LogUtil.info(LogEnum.BIZ_K8S,"Return value of creating Job--create:{}", result);
            return result;
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S,"TrainJobApiImpl.create error, param:{} error:", bo, e);
            return new PtJupyterJobVO().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    /**
     * 根据命名空间和资源名删除Job
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return Boolean
     */
    @Override
    public Boolean delete(String namespace, String resourceName){
        try {
            List<BizPod> podList = podApi.getListByResourceName(namespace,resourceName);
            if (CollectionUtil.isNotEmpty(podList)){
                podList.forEach(pod->logMonitoringApi.addLogsToEs(pod.getName(),pod.getNamespace()));
            }
            return client.batch().jobs().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
        }catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "TrainJobApiImpl.delete error, param:[namespace]={}, [resourceName]={}, error:",namespace, resourceName,e);
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
            LogUtil.error(LogEnum.BIZ_K8S, "TrainJobApiImpl.get error, param:[namespace]={}, [resourceName]={}, error:",namespace, resourceName,e);
            return new BizJob().error(String.valueOf(e.getCode()),e.getMessage());
        }
    }

    private class JupyterDeployer{
        private String baseName;
        private String jobName;

        private String namespace;
        private String image;
        private Boolean useGpu;
        private List<String> cmdLines;
        private String nfs;
        private Map<String, PtMountDirBO> nfsMounts;

        private Map<String, Quantity> resourcesLimitsMap;
        private Map<String, String> baseLabels;

        private List<VolumeMount> volumeMounts;
        private List<Volume> volumes;
        private String businessLabel;

        private String errCode;
        private String errMessage;

        private JupyterDeployer(PtJupyterJobBO bo){
            this.baseName = bo.getName();
            this.jobName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, baseName, RandomUtil.randomString(K8sParamConstants.RESOURCE_NAME_SUFFIX_LENGTH));
            this.namespace = bo.getNamespace();
            this.image = bo.getImage();
            this.cmdLines = new ArrayList();
            Optional.ofNullable(bo.getCmdLines()).ifPresent(v -> cmdLines = v);
            this.useGpu = bo.getUseGpu()==null?false:bo.getUseGpu();
            if (bo.getUseGpu() != null && bo.getUseGpu() && null == bo.getGpuNum()){
                bo.setGpuNum(MagicNumConstant.ZERO);
            }

            this.resourcesLimitsMap = Maps.newHashMap();
            Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
            Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.GPU_RESOURCE_KEY, new Quantity(v.toString())));
            Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));

            this.nfs = k8sUtils.getNfs();
            this.nfsMounts = bo.getNfsMounts();
            businessLabel = bo.getBusinessLabel();
            this.baseLabels = LabelUtils.getBaseLabels(baseName,bo.getBusinessLabel());

            this.volumeMounts = new ArrayList<>();
            this.volumes = new ArrayList<>();
            this.errCode = K8sResponseEnum.SUCCESS.getCode();
            this.errMessage = SymbolConstant.BLANK;
        }

        /**
         * 部署Job
         *
         * @return PtJupyterJobVO 训练任务 Job 结果类
         */
        public PtJupyterJobVO deploy()  {
            if (!K8sResponseEnum.SUCCESS.getCode().equals(errCode)){
                return new PtJupyterJobVO().error(errCode,errMessage);
            }
            //部署job
            Job job = deployJob();

            return PtJupyterJobVO.getInstance(job);
        }

        /**
         * 挂载存储
         *
         * @return JupyterDeployer Jupyter Job 部署类
         */
        private JupyterDeployer buildVolumes(){
            if (CollectionUtil.isNotEmpty(nfsMounts)){
                int i = MagicNumConstant.ZERO;
                for (Map.Entry<String, PtMountDirBO> mount : nfsMounts.entrySet()) {
                    boolean availableMount = (mount != null && StringUtils.isNotEmpty(mount.getKey()) && mount.getValue() != null && StringUtils.isNotEmpty(mount.getValue().getDir()));
                    if (availableMount){
                        boolean success = mount.getValue().isRecycle()?buildNfsPvcVolumes(mount.getKey(),mount.getValue(),i):buildNfsVolumes(mount.getKey(),mount.getValue(),i);
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
         * 挂载存储
         *
         * @param mountPath 挂载路径
         * @param dirBO 挂载路径参数
         * @param num 名称序号
         * @return boolean
         */
        private boolean buildNfsVolumes(String mountPath,PtMountDirBO dirBO,int num){
            volumeMounts.add(new VolumeMountBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+num)
                    .withMountPath(mountPath)
                    .withReadOnly(dirBO.isReadOnly())
                    .build());
            volumes.add(new VolumeBuilder()
                    .withName(K8sParamConstants.VOLUME_PREFIX+num)
                    .withNewNfs()
                        .withPath(dirBO.getDir())
                        .withServer(nfs)
                    .endNfs()
                    .build());
            return true;
        }

        /**
         * 按照存储资源声明挂载存储
         *
         * @param mountPath 挂载路径
         * @param dirBO 挂载路径参数
         * @param i 名称序号
         * @return boolean
         */
        private boolean buildNfsPvcVolumes(String mountPath,PtMountDirBO dirBO,int i){
            BizPersistentVolumeClaim bizPersistentVolumeClaim = persistentVolumeClaimApi.createWithNfsPv(new PtPersistentVolumeClaimBO(namespace,baseName,dirBO));
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
         * @return Job
         */
        private Job deployJob() {
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
            container.setImagePullPolicy(ImagePullPolicyEnum.IFNOTPRESENT.getPolicy());
            container.setVolumeMounts(volumeMounts);
            //启动命令
            container.setCommand(Collections.singletonList(ShellCommandEnum.BIN_BANSH.getShell()));
            container.setArgs(cmdLines);

            //资源限制
            container.setResources(new ResourceRequirementsBuilder()
                    .addToLimits(resourcesLimitsMap)
                    .build());

            Map<String,String> gpuLabel = new HashMap<String,String>(1);
            if (useGpu){
                gpuLabel.put(K8sLabelConstants.NODE_GPU_LABEL_KEY,K8sLabelConstants.NODE_GPU_LABEL_VALUE);
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
                                .addToLabels(LabelUtils.getChildLabels(baseName, jobName, K8sKindEnum.JOB.getKind(),businessLabel))
                                .withNamespace(namespace)
                            .endMetadata()
                            .withNewSpec()
                                .addToNodeSelector(gpuLabel)
                                .addToContainers(container)
                                .addToVolumes(volumes.toArray(new Volume[0]))
                                .withRestartPolicy(RestartPolicyEnum.NEVER.getRestartPolicy())
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();
            System.out.println(YamlUtils.dumpAsYaml(job));
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}", jobName);
            job = client.batch().jobs().create(job);
            LogUtil.info(LogEnum.BIZ_K8S, "{} deployed successfully", jobName);
            return job;
        }
    }
}
