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
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.CustomResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.DistributeTrainApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.api.VolumeApi;
import org.dubhe.k8s.cache.ResourceCache;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.BuildFsVolumeBO;
import org.dubhe.k8s.domain.bo.DistributeTrainBO;
import org.dubhe.k8s.domain.bo.TaskYamlBO;
import org.dubhe.k8s.domain.cr.DistributeTrain;
import org.dubhe.k8s.domain.cr.DistributeTrainDoneable;
import org.dubhe.k8s.domain.cr.DistributeTrainList;
import org.dubhe.k8s.domain.cr.DistributeTrainSpec;
import org.dubhe.k8s.domain.entity.K8sTask;
import org.dubhe.k8s.domain.resource.BizDistributeTrain;
import org.dubhe.k8s.domain.vo.VolumeVO;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LackOfResourcesEnum;
import org.dubhe.k8s.enums.LimitsOfResourcesEnum;
import org.dubhe.k8s.service.K8sTaskService;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.dubhe.biz.base.constant.MagicNumConstant.ONE;
import static org.dubhe.biz.base.constant.MagicNumConstant.SIXTY_LONG;
import static org.dubhe.biz.base.constant.MagicNumConstant.THOUSAND_LONG;
import static org.dubhe.biz.base.constant.MagicNumConstant.ZERO;
import static org.dubhe.biz.base.constant.SymbolConstant.BLANK;
import static org.dubhe.k8s.constant.K8sParamConstants.GPU_RESOURCE_KEY;
import static org.dubhe.k8s.constant.K8sParamConstants.NODE_READY_TRUE;
import static org.dubhe.k8s.constant.K8sParamConstants.PYTHONUNBUFFERED;
import static org.dubhe.k8s.constant.K8sParamConstants.QUANTITY_CPU_KEY;
import static org.dubhe.k8s.constant.K8sParamConstants.QUANTITY_MEMORY_KEY;

/**
 * @description DistributeTrain 实现类
 * @date 2020-07-07
 */
public class DistributeTrainApiImpl implements DistributeTrainApi {
    private static final String CRD_NAME = "distributetrains.onebrain.oneflow.org";
    private static final String CRD_GROUP = "onebrain.oneflow.org";
    private static final String VERSION = "v1alpha1";
    private static final String PLURAL = "distributetrains";
    private static final String SCOPE = "Namespaced";
    private static final String ENABLE_USER_OP = "ENABLE_USER_OP";
    private static final String DATA_ROOT = "DATA_ROOT";
    private static final String NODE_NUM = "NODE_NUM";
    private static final String GPU_NUM_PER_NODE = "GPU_NUM_PER_NODE";
    private static final String ONEFLOW_DEBUG_MODE = "ONEFLOW_DEBUG_MODE";
    private static final String NCCL_DEBUG = "NCCL_DEBUG";
    private static final String INFO = "INFO";
    private static final String DATA_ROOT_VALUE = "/dataset";

    @javax.annotation.Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;

    @Autowired
    private K8sTaskService k8sTaskService;

    @Autowired
    private ResourceCache resourceCache;

    @Autowired
    private VolumeApi volumeApi;

    @Autowired
    private NodeApi nodeApi;

    @Autowired
    private ResourceQuotaApi resourceQuotaApi;

    @Autowired
    private ResourceIisolationApi resourceIisolationApi;

    private KubernetesClient client;
    private MixedOperation<DistributeTrain, DistributeTrainList, DistributeTrainDoneable, Resource<DistributeTrain, DistributeTrainDoneable>> dtClient;

    public DistributeTrainApiImpl(K8sUtils k8sUtils) {
        this.client = k8sUtils.getClient();
        initDtClient();
    }

    /**
     * 创建cr
     *
     * @param bo 自定义资源入参
     * @return BizDistributeTrain 自定义资源dt
     */
    @Override
    public BizDistributeTrain create(DistributeTrainBO bo) {
        LogUtil.info(LogEnum.BIZ_K8S, "Params of creating DistributeTrain--create:{}", bo);
        LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(bo.getNamespace(), bo.getCpuNum() * bo.getSize(), bo.getMemNum() * bo.getSize(), bo.getGpuNum() == null?0:bo.getGpuNum() * bo.getSize());
        if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
            return new BizDistributeTrain().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
        }
        if (bo.getGpuNum() != null && bo.getGpuNum() > 0) {
            LackOfResourcesEnum lack = nodeApi.isOutOfTotalAllocatableGpu(bo.getGpuNum() * bo.getSize());
            if (!LackOfResourcesEnum.ADEQUATE.equals(lack)) {
                return new BizDistributeTrain().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), lack.getMessage());
            }
        }
        if (!fileStoreApi.createDirs(bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))) {
            return new BizDistributeTrain().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
        }
        VolumeVO volumeVO = volumeApi.buildFsVolumes(new BuildFsVolumeBO(bo.getNamespace(), bo.getName(), bo.getFsMounts()));
        if (!K8sResponseEnum.SUCCESS.getCode().equals(volumeVO.getCode())) {
            return new BizDistributeTrain().error(volumeVO.getCode(), volumeVO.getMessage());
        }
        //删除pod名称缓存
        resourceCache.deletePodCacheByResourceName(bo.getNamespace(), bo.getName());
        return new DistributeTrainDeployer(bo, volumeVO).deploy();
    }

    public class DistributeTrainDeployer {
        private String baseName;
        private String distributeTrainName;
        private String namespace;
        private int size;
        private String image;
        private String masterCmd;
        private Integer memNum;
        private Integer cpuNum;
        private Integer gpuNum;
        private String slaveCmd;
        private Map<String, String> env;
        private Map<String, String> baseLabels;
        private String businessLabel;
        private String taskIdentifyLabel;
        private Integer delayCreate;
        private Integer delayDelete;
        private TaskYamlBO taskYamlBO;
        private VolumeVO volumeVO;

        public DistributeTrainDeployer(DistributeTrainBO bo, VolumeVO volumeVO) {
            this.baseName = bo.getName();
            this.distributeTrainName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, baseName, RandomUtil.randomString(K8sParamConstants.RESOURCE_NAME_SUFFIX_LENGTH));
            this.namespace = bo.getNamespace();
            this.size = bo.getSize();
            this.image = bo.getImage();
            this.masterCmd = bo.getMasterCmd();
            this.memNum = bo.getMemNum();
            this.cpuNum = bo.getCpuNum();
            this.gpuNum = bo.getGpuNum();
            this.slaveCmd = bo.getSlaveCmd();
            this.env = bo.getEnv();
            this.businessLabel = bo.getBusinessLabel();
            this.taskIdentifyLabel = bo.getTaskIdentifyLabel();
            this.baseLabels = LabelUtils.getChildLabels(baseName, distributeTrainName, K8sKindEnum.DISTRIBUTETRAIN.getKind(), businessLabel, taskIdentifyLabel);
            this.delayCreate = bo.getDelayCreateTime();
            this.delayDelete = bo.getDelayDeleteTime();
            this.taskYamlBO = new TaskYamlBO();
            this.volumeVO = volumeVO;
        }

        /**
         * 判断分布式训练资源定义在集群是否存在
         */
        private boolean isCrdExist() {
            CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
            //获取所有自定义资源的定义
            List<CustomResourceDefinition> crdsItems = crds.getItems();
            LogUtil.info(LogEnum.BIZ_K8S, "Found {} CRD(s)", crdsItems.size());
            CustomResourceDefinition crd = null;
            //循环判断是否存在分布式训练资源定义
            for (CustomResourceDefinition obj : crdsItems) {
                ObjectMeta metadata = obj.getMetadata();

                if (metadata != null) {
                    String name = metadata.getName();
                    if (CRD_NAME.equals(name)) {
                        crd = obj;
                        break;
                    }
                }
            }

            if (crd != null) {
                LogUtil.info(LogEnum.BIZ_K8S, "Found CRD: {}", crd.getMetadata().getSelfLink());
                return true;
            } else {
                LogUtil.info(LogEnum.BIZ_K8S, "Not found crd {}", CRD_NAME);
                return false;
            }
        }

        /**
         * 构建Spec
         */
        private DistributeTrainSpec buildSpec() {
            DistributeTrainSpec distributeTrainSpec = new DistributeTrainSpec();
            //配置节点数
            distributeTrainSpec.setSize(size);
            //配置镜像和拉取策略
            distributeTrainSpec.setImage(image);
            distributeTrainSpec.setImagePullPolicy(ImagePullPolicyEnum.IFNOTPRESENT.getPolicy());
            //配置主从节点运行命令
            distributeTrainSpec.setMasterCmd(masterCmd);
            distributeTrainSpec.setSlaveCmd(slaveCmd);

            //master节点申请资源
            ResourceRequirements masterResources = new ResourceRequirements();
            masterResources.setLimits(new HashMap<String, Quantity>() {{
                Optional.ofNullable(memNum).ifPresent(v -> put(QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
                Optional.ofNullable(cpuNum).ifPresent(v -> put(QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
                Optional.ofNullable(gpuNum).ifPresent(v -> put(GPU_RESOURCE_KEY, new Quantity(v.toString())));
            }});
            distributeTrainSpec.setMasterResources(masterResources);
            //slave节点申请资源
            ResourceRequirements slaveResources = new ResourceRequirements();
            slaveResources.setLimits(new HashMap<String, Quantity>() {{
                Optional.ofNullable(memNum).ifPresent(v -> put(QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
                Optional.ofNullable(cpuNum).ifPresent(v -> put(QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
                Optional.ofNullable(gpuNum).ifPresent(v -> put(GPU_RESOURCE_KEY, new Quantity(v.toString())));
            }});
            distributeTrainSpec.setSlaveResources(slaveResources);
            //配置环境变量
            List<EnvVar> envVarList = new ArrayList() {{
                add(new EnvVarBuilder().withName(PYTHONUNBUFFERED).withValue(SymbolConstant.ZERO).build());
                add(new EnvVarBuilder().withName(ENABLE_USER_OP).withValue(NODE_READY_TRUE).build());
                add(new EnvVarBuilder().withName(DATA_ROOT).withValue(DATA_ROOT_VALUE).build());
                add(new EnvVarBuilder().withName(NODE_NUM).withValue(String.valueOf(size)).build());
                add(new EnvVarBuilder().withName(ONEFLOW_DEBUG_MODE).withValue(BLANK).build());
                add(new EnvVarBuilder().withName(NCCL_DEBUG).withValue(INFO).build());
            }};

            if (gpuNum != null && gpuNum != 0) {
                envVarList.add(new EnvVarBuilder().withName(GPU_NUM_PER_NODE).withValue(String.valueOf(gpuNum)).build());
            }
            if (CollectionUtils.isNotEmpty(env)) {
                Set<String> envNames = env.keySet();
                for (String envName : envNames) {
                    envVarList.add(new EnvVarBuilder().withName(envName).withValue(env.get(envName)).build());
                }
            }

            distributeTrainSpec.setEnv(envVarList);
            distributeTrainSpec.setVolumeMounts(volumeVO.getVolumeMounts());
            distributeTrainSpec.setVolumes(volumeVO.getVolumes());

            return distributeTrainSpec;
        }

        /**
         * 判断分布式训练资源在集群是否存在
         */
        private BizDistributeTrain alreadyHaveDistributeTrain() {
            CustomResourceList<DistributeTrain> dummyList = dtClient.inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(baseName)).list();
            List<DistributeTrain> dummyItems = dummyList.getItems();
            if (CollectionUtil.isNotEmpty(dummyItems)) {
                DistributeTrain distributeTrain = dummyItems.get(0);
                LogUtil.warn(LogEnum.BIZ_K8S, "Skip creating DistributeTrain, {} already exists", baseName);
                return BizConvertUtils.toBizDistributeTrain(distributeTrain);
            }
            return null;
        }

        /**
         * 部署分布式训练资源
         */
        private BizDistributeTrain deploy() {
            try {
                if (!isCrdExist()) {
                    return null;
                }

                BizDistributeTrain bizdistributeTrain = alreadyHaveDistributeTrain();
                if (bizdistributeTrain != null) {
                    return bizdistributeTrain;
                }

                DistributeTrain distributeTrain = new DistributeTrain();
                ObjectMeta metadata = new ObjectMeta();
                metadata.setName(distributeTrainName);
                metadata.setNamespace(namespace);
                metadata.setLabels(baseLabels);
                distributeTrain.setMetadata(metadata);

                distributeTrain.setSpec(buildSpec());

                delayCreate = delayCreate == null || delayCreate <= 0 ? ZERO : delayCreate;
                delayDelete = delayDelete == null || delayDelete <= 0 ? ZERO : delayDelete;

                if (delayCreate > ZERO || delayDelete > ZERO) {
                    taskYamlBO.append(distributeTrain);
                    long applyUnixTime = System.currentTimeMillis() / THOUSAND_LONG + delayCreate * SIXTY_LONG;
                    Timestamp applyDisplayTime = new Timestamp(applyUnixTime * THOUSAND_LONG);
                    long stopUnixTime = applyUnixTime + delayDelete * SIXTY_LONG;
                    Timestamp stopDisplayTime = new Timestamp(stopUnixTime * THOUSAND_LONG);
                    K8sTask k8sTask = new K8sTask() {{
                        setNamespace(namespace);
                        setResourceName(baseName);
                        setTaskYaml(JSON.toJSONString(taskYamlBO));
                        setBusiness(businessLabel);
                        setApplyUnixTime(applyUnixTime);
                        setApplyDisplayTime(applyDisplayTime);
                        setApplyStatus(delayCreate == ZERO ? ZERO : ONE);
                    }};
                    if (delayDelete > ZERO) {
                        k8sTask.setStopUnixTime(stopUnixTime);
                        k8sTask.setStopDisplayTime(stopDisplayTime);
                        k8sTask.setStopStatus(ONE);
                    }
                    k8sTaskService.createOrUpdateTask(k8sTask);
                }
                if (delayCreate == null || delayCreate == 0) {
                    LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}", distributeTrainName);
                    resourceIisolationApi.addIisolationInfo(distributeTrain);
                    distributeTrain = dtClient.inNamespace(namespace).create(distributeTrain);
                    LogUtil.info(LogEnum.BIZ_K8S, "{} deployed successfully", distributeTrainName);
                }

                return BizConvertUtils.toBizDistributeTrain(distributeTrain);
            } catch (KubernetesClientException e) {
                LogUtil.error(LogEnum.BIZ_K8S, "DistributeTrainApi.deploy过程错误, 错误信息为{}", e.toString());
                return new BizDistributeTrain().error(String.valueOf(e.getCode()), e.getMessage());
            }
        }
    }

    /**
     * 删除
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult deleteByResourceName(String namespace, String resourceName) {
        LogUtil.info(LogEnum.BIZ_K8S, "deleteByResourceName namespace {} resourceName {}", namespace,resourceName);
        if (dtClient == null) {
            LogUtil.error(LogEnum.BIZ_K8S, "dtClient初始化失败");
        }
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(resourceName)) {
            return new BizDistributeTrain().baseErrorBadRequest();
        }
        try {
            k8sTaskService.deleteByNamespaceAndResourceName(namespace,resourceName);
            //根据条件获得对应的分布式训练资源集合
            DistributeTrainList list = dtClient.inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            List<DistributeTrain> items = list.getItems();
            //判断该分布式训练资源是否存在
            if (items.size() == 0) {
                return new PtBaseResult(K8sResponseEnum.SUCCESS.getCode(), "k8s任务不存在,无需删除");
            }
            //判断是否存在多个分布式训练资源
            if (items.size() > 1) {
                LogUtil.error(LogEnum.BIZ_K8S, "DistributeTrainApiImpl.deleteByResourceName过程错误, 存在多个dt");
                return new PtBaseResult(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }

            if (dtClient.delete(items.get(0))) {
                LogUtil.info(LogEnum.BIZ_K8S, "input namespace={};resourceName={}; 删除成功");
                return new PtBaseResult();
            } else {
                return new PtBaseResult(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "DistributeTrainApiImpl.deleteByResourceName过程错误, 错误信息为{}", e);
            return new BizDistributeTrain().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 根据namespace和resourceName查询cr信息
     *
     * @param namespce 命令空间
     * @param resourceName 资源名称
     * @return BizDistributeTrain  自定义资源转换类集合
     */
    @Override
    public BizDistributeTrain get(String namespce, String resourceName) {
        try {
            DistributeTrain distributeTrain = dtClient.inNamespace(namespce).withName(resourceName).get();
            if (distributeTrain != null) {
                return BizConvertUtils.toBizDistributeTrain(distributeTrain);
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "DistributeTrainApiImpl.get查询错误,错误信息为{}", e.toString());
            return new BizDistributeTrain().error(String.valueOf(e.getCode()), e.getMessage());
        }
        return null;
    }

    /**
     * 根据名称查cr
     *
     * @param crName 自定义资源名称
     * @return BizDistributeTrain 自定义资源类
     */
    @Override
    public BizDistributeTrain findDisByName(String crName) {
        //获取所有DistributeTrain资源
        List<DistributeTrain> distributeTrains = dtClient.inAnyNamespace().list().getItems();
        if (!CollectionUtil.isEmpty(distributeTrains)) {
            for (DistributeTrain distributeTrain : distributeTrains) {
                ObjectMeta metadata = distributeTrain.getMetadata();
                if (metadata != null) {
                    if (crName != null && crName.equals(metadata.getName())) {
                        return BizConvertUtils.toBizDistributeTrain(distributeTrain);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 通过 yaml创建
     * @param crYaml cr定义yaml脚本
     * @return BizDistributeTrain 自定义资源类
     */
    @Override
    public BizDistributeTrain create(String crYaml) {
        try {
            DistributeTrain distributeTrain = dtClient.load(new ByteArrayInputStream(crYaml.getBytes())).createOrReplace();
            return BizConvertUtils.toBizDistributeTrain(distributeTrain);
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "Create DistributeTrain error:{} ,yml:{}", e.getMessage(), crYaml);
            return null;
        }
    }

    /**
     * 通过 yaml删除
     * @param crYaml cr定义yaml脚本
     * @return boolean true 删除成功 false删除失败
     */
    @Override
    public boolean delete(String crYaml) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "delete crYaml {}",crYaml);
            return dtClient.load(new ByteArrayInputStream(crYaml.getBytes())).delete();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "Delete DistributeTrain error:{} ,yml:{}", e.getMessage(), crYaml);
            return false;
        }
    }

    /**
     * 初始化dtClient
     */
    void initDtClient() {
        LogUtil.info(LogEnum.BIZ_K8S, "dtClient初始化开始");
        try {
            //构建CustomResourceDefinitionContext
            CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                    .withGroup(CRD_GROUP)
                    .withName(CRD_NAME)
                    .withPlural(PLURAL)
                    .withScope(SCOPE)
                    .withVersion(VERSION)
                    .build();

            dtClient = client.customResources(crdContext, DistributeTrain.class, DistributeTrainList.class, DistributeTrainDoneable.class);
        } catch (Exception e) {
            LogUtil.error(LogEnum.BIZ_K8S, "dtClient初始化失败, 错误信息为{}", e);
        }
        LogUtil.info(LogEnum.BIZ_K8S, "dtClient初始化完成");
    }
}
