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

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
import io.fabric8.kubernetes.api.model.storage.StorageClassBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.PersistentVolumeClaimApi;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.PtPersistentVolumeClaimBO;
import org.dubhe.k8s.domain.resource.BizPersistentVolumeClaim;
import org.dubhe.k8s.enums.AccessModeEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.PvReclaimPolicyEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.k8s.utils.YamlUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description pvc api
 * @date 2020-04-23
 */
public class PersistentVolumeClaimApiImpl implements PersistentVolumeClaimApi {
    private K8sUtils k8sUtils;
    private KubernetesClient client;

    private final static String STORAGE_CLASS_API_VERSION = "storage.k8s.io/v1";
    private final static String PV = "pv";
    private final static String PV_SUFFIX = "-pv";
    private final static String STORAGE = "storage";
    private final static String STORAGE_CLASS_SUFFIX = "-storageclass";
    private final static String HOSTPATH_RECYCLING_STRATEGY = "DirectoryOrCreate";
    private final static String PROVISIONER = "k8s.io/minikube-hostpath";

    @Value("${k8s.nfs-storage-class-name}")
    private String nfsStorageClassName;

    public PersistentVolumeClaimApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建PVC
     *
     * @param bo PVC BO
     * @return BizPersistentVolumeClaim PVC业务类
     */
    @Override
    public BizPersistentVolumeClaim create(PtPersistentVolumeClaimBO bo) {
        if (bo == null || StringUtils.isEmpty(bo.getNamespace()) || StringUtils.isEmpty(bo.getPvcName()) || StringUtils.isEmpty(bo.getRequest())) {
            return new BizPersistentVolumeClaim().errorBadRequest();
        }
        try {
            Map<String, String> storageClassLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getPvcName(), K8sKindEnum.PERSISTENTVOLUMECLAIM.getKind());

            Map<String, String> pvcLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getNamespace(), K8sKindEnum.NAMESPACE.getKind());

            //创建StorageClass
            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(getStorageClassName(bo.getPvcName()));
            metadata.setNamespace(bo.getNamespace());
            metadata.setLabels(storageClassLabels);
            StorageClass storageClass = new StorageClassBuilder().withApiVersion(STORAGE_CLASS_API_VERSION)
                    .withKind(K8sKindEnum.STORAGECLASS.getKind())
                    .withMetadata(metadata)
                    .withParameters(bo.getParameters())
                    .withProvisioner(bo.getProvisioner()==null?PROVISIONER:bo.getProvisioner())
                    .build();
            storageClass = client.storage().storageClasses().inNamespace(bo.getNamespace()).createOrReplace(storageClass);
            LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(storageClass));
            //创建pvc
            PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                    .withNewMetadata().withName(bo.getPvcName()).addToLabels(pvcLabels).addToLabels(bo.getLabels()).endMetadata()
                    .withNewSpec().addAllToAccessModes(bo.getAccessModes())
                    .withNewResources().addToRequests(STORAGE, new Quantity(bo.getRequest())).endResources()
                    .withNewStorageClassName(getStorageClassName(bo.getPvcName())).endSpec().build();
            if (StringUtils.isNotEmpty(bo.getLimit())) {
                pvc.getSpec().getResources().setLimits(new HashMap<String, Quantity>() {{
                    put(STORAGE, new Quantity(bo.getLimit()));
                }});
            }
            LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pvc));
            return BizConvertUtils.toBizPersistentVolumeClaim(client.persistentVolumeClaims().inNamespace(bo.getNamespace()).create(pvc));
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApi.create error, param:{} error:{}", bo, e);
            return new BizPersistentVolumeClaim().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 创建挂载文件存储服务 PV
     *
     * @param bo PVC bo
     * @return BizPersistentVolumeClaim PVC业务类
     */
    @Override
    public BizPersistentVolumeClaim createWithFsPv(PtPersistentVolumeClaimBO bo) {
        if (bo == null || StringUtils.isEmpty(bo.getNamespace()) || StringUtils.isEmpty(bo.getPvcName()) || StringUtils.isEmpty(bo.getRequest())) {
            return new BizPersistentVolumeClaim().errorBadRequest();
        }
        try {
            Map<String, String> pvLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getPvcName(), K8sKindEnum.PERSISTENTVOLUMECLAIM.getKind());
            pvLabels.put(PV, bo.getPvcName() + PV_SUFFIX);

            if (client.persistentVolumes().withName(bo.getPvcName() + PV_SUFFIX).get() == null) {
                //创建pv
                PersistentVolume pv = new PersistentVolumeBuilder()
                        .withNewMetadata().addToLabels(pvLabels).withName(bo.getPvcName() + PV_SUFFIX).endMetadata()
                        .withNewSpec().addToCapacity(STORAGE, new Quantity(bo.getRequest())).addNewAccessMode(AccessModeEnum.READ_WRITE_ONCE.getType()).withNewPersistentVolumeReclaimPolicy(StringUtils.isNotEmpty(bo.getReclaimPolicy())?PvReclaimPolicyEnum.RECYCLE.getPolicy():bo.getReclaimPolicy())
                        .withNewHostPath().withNewPath(bo.getPath()).withType(K8sParamConstants.HOST_PATH_TYPE).endHostPath()
                        .endSpec()
                        .build();
                LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pv));
                client.persistentVolumes().createOrReplace(pv);
            }

            Map<String, String> pvcLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getNamespace(), K8sKindEnum.NAMESPACE.getKind());

            PersistentVolumeClaim old = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).withName(bo.getPvcName()).get();
            if (old == null) {
                //创建pvc
                PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                        .withNewMetadata().withName(bo.getPvcName()).addToLabels(pvcLabels).addToLabels(bo.getLabels()).endMetadata()
                        .withNewSpec().addAllToAccessModes(bo.getAccessModes())
                        .withNewSelector().addToMatchLabels(PV, bo.getPvcName() + PV_SUFFIX).endSelector()
                        .withNewResources().addToRequests(STORAGE, new Quantity(bo.getRequest())).endResources()
                        .endSpec().build();
                if (StringUtils.isNotEmpty(bo.getLimit())) {
                    pvc.getSpec().getResources().setLimits(new HashMap<String, Quantity>(MagicNumConstant.SIXTEEN) {{
                        put(STORAGE, new Quantity(bo.getLimit()));
                    }});
                }
                LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pvc));
                pvc = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).createOrReplace(pvc);
                return BizConvertUtils.toBizPersistentVolumeClaim(pvc);
            } else {
                return BizConvertUtils.toBizPersistentVolumeClaim(old);
            }

        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApiImpl.createWithFsPv error, param:{} error:{}", bo, e);
            return new BizPersistentVolumeClaim().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 创建挂载直接存储 PV
     *
     * @param bo PVC BO
     * @return BizPersistentVolumeClaim PVC业务类
     */
    @Override
    public BizPersistentVolumeClaim createWithDirectPv(PtPersistentVolumeClaimBO bo) {
        if (bo == null || StringUtils.isEmpty(bo.getNamespace()) || StringUtils.isEmpty(bo.getPvcName()) || StringUtils.isEmpty(bo.getRequest())) {
            return new BizPersistentVolumeClaim().errorBadRequest();
        }
        try {
            Map<String, String> pvLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getPvcName(), K8sKindEnum.PERSISTENTVOLUMECLAIM.getKind());
            pvLabels.put(PV, bo.getPvcName() + PV_SUFFIX);

            if (client.persistentVolumes().withName(bo.getPvcName() + PV_SUFFIX).get() == null) {
                //创建pv
                PersistentVolume pv = new PersistentVolumeBuilder()
                        .withNewMetadata().addToLabels(pvLabels).withName(bo.getPvcName() + PV_SUFFIX).endMetadata()
                        .withNewSpec().addToCapacity(STORAGE, new Quantity(bo.getRequest())).addNewAccessMode(AccessModeEnum.READ_WRITE_ONCE.getType()).withNewPersistentVolumeReclaimPolicy("Recycle")
                        .withNewHostPath(bo.getPath(),HOSTPATH_RECYCLING_STRATEGY)
                        .endSpec()
                        .build();
                LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pv));
                client.persistentVolumes().createOrReplace(pv);
            }

            Map<String, String> pvcLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getNamespace(), K8sKindEnum.NAMESPACE.getKind());

            PersistentVolumeClaim old = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).withName(bo.getPvcName()).get();
            if (old == null) {
                //创建pvc
                PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                        .withNewMetadata().withName(bo.getPvcName()).addToLabels(pvcLabels).addToLabels(bo.getLabels()).endMetadata()
                        .withNewSpec().addAllToAccessModes(bo.getAccessModes())
                        .withNewSelector().addToMatchLabels(PV, bo.getPvcName() + PV_SUFFIX).endSelector()
                        .withNewResources().addToRequests(STORAGE, new Quantity(bo.getRequest())).endResources()
                        .endSpec().build();
                if (StringUtils.isNotEmpty(bo.getLimit())) {
                    pvc.getSpec().getResources().setLimits(new HashMap<String, Quantity>(MagicNumConstant.SIXTEEN) {{
                        put(STORAGE, new Quantity(bo.getLimit()));
                    }});
                }
                LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pvc));
                pvc = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).createOrReplace(pvc);
                return BizConvertUtils.toBizPersistentVolumeClaim(pvc);
            } else {
                return BizConvertUtils.toBizPersistentVolumeClaim(old);
            }

        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApiImpl.createWithDirectPv error, param:{} error:{}", bo, e);
            return new BizPersistentVolumeClaim().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 创建创建自动挂载nfs动态存储的PVC
     *
     * @param bo PVC bo
     * @return BizPersistentVolumeClaim PVC业务类
     */
    @Override
    public BizPersistentVolumeClaim createDynamicNfs(PtPersistentVolumeClaimBO bo) {
        if (bo == null || StringUtils.isEmpty(bo.getNamespace()) || StringUtils.isEmpty(bo.getPvcName()) || StringUtils.isEmpty(bo.getRequest())) {
            return new BizPersistentVolumeClaim().errorBadRequest();
        }
        try {
            Map<String, String> pvcLabels = LabelUtils.getChildLabels(bo.getResourceName(), bo.getNamespace(), K8sKindEnum.NAMESPACE.getKind());

            PersistentVolumeClaim old = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).withName(bo.getPvcName()).get();
            if (old == null) {
                //创建pvc
                PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                        .withNewMetadata().withName(bo.getPvcName()).addToLabels(pvcLabels).addToLabels(bo.getLabels()).endMetadata()
                        .withNewSpec().addAllToAccessModes(bo.getAccessModes())
                        .withNewStorageClassName(nfsStorageClassName)
                        .withNewResources().addToRequests(STORAGE, new Quantity(bo.getRequest())).endResources()
                        .endSpec().build();
                if (StringUtils.isNotEmpty(bo.getLimit())) {
                    pvc.getSpec().getResources().setLimits(new HashMap<String, Quantity>(MagicNumConstant.SIXTEEN) {{
                        put(STORAGE, new Quantity(bo.getLimit()));
                    }});
                }
                pvc = client.persistentVolumeClaims().inNamespace(bo.getNamespace()).createOrReplace(pvc);
                LogUtil.info(LogEnum.BIZ_K8S, YamlUtils.dumpAsYaml(pvc));
                return BizConvertUtils.toBizPersistentVolumeClaim(pvc);
            } else {
                return BizConvertUtils.toBizPersistentVolumeClaim(old);
            }

        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApiImpl.createDynamicNfs error, param:{} error:{}", bo, e);
            return new BizPersistentVolumeClaim().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询命名空间下所有 PVC
     *
     * @param namespace 命名空间
     * @return List<BizPersistentVolumeClaim> PVC业务类集合
     */
    @Override
    public List<BizPersistentVolumeClaim> list(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return Collections.EMPTY_LIST;
        }
        PersistentVolumeClaimList persistentVolumeClaimList = client.persistentVolumeClaims().inNamespace(namespace).list();
        return persistentVolumeClaimList.getItems().parallelStream().map(obj -> BizConvertUtils.toBizPersistentVolumeClaim(obj)).collect(Collectors.toList());
    }

    /**
     * 删除具体的PVC
     *
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult delete(String namespace, String pvcName) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(pvcName)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.storage().storageClasses().inNamespace(namespace).withName(getStorageClassName(pvcName)).delete();
            client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).delete();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApiImpl.delete error, param:[namespace]={}, error:{}", namespace, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 回收存储（recycle 的pv才能回收）
     *
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult recycle(String namespace, String resourceName) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceName)) {
            return new PtBaseResult().baseErrorBadRequest();
        }
        try {
            client.persistentVolumeClaims().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            client.persistentVolumes().withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            return new PtBaseResult();
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "PersistentVolumeClaimApiImpl.recycle error, param:[namespace]={}, [resourceName]={}, error:{}",namespace, resourceName, e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 拼接storageClassName
     *
     * @param pvcName PVC名称
     * @return String 动态PVC的名称
     */
    @Override
    public String getStorageClassName(String pvcName) {
        if (StringUtils.isEmpty(pvcName)) {
            return null;
        } else {
            return pvcName + STORAGE_CLASS_SUFFIX;
        }
    }

    /**
     * 删除PV
     *
     * @param pvName PV名称
     * @return boolean true成功 false失败
     */
    @Override
    public boolean deletePv(String pvName) {
        return client.persistentVolumes().withName(pvName).delete();
    }

    /**
     * 删除PV
     *
     * @param resourceName 资源名称
     * @return boolean true成功 false失败
     */
    @Override
    public boolean deletePvByResourceName(String resourceName) {
        return client.persistentVolumes().withLabel(K8sLabelConstants.BASE_TAG_SOURCE,resourceName).delete();
    }

    /**
     * 查询PV
     *
     * @param pvName PV名称
     * @return PersistentVolume PV实体类
     */
    @Override
    public PersistentVolume getPv(String pvName) {
        return client.persistentVolumes().withName(pvName).get();
    }
}
