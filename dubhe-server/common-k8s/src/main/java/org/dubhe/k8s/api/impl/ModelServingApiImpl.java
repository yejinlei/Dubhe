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
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.StringConstant;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.biz.file.api.FileStoreApi;
import org.dubhe.biz.log.enums.LogEnum;
import org.dubhe.biz.log.utils.LogUtil;
import org.dubhe.k8s.api.ModelServingApi;
import org.dubhe.k8s.api.NodeApi;
import org.dubhe.k8s.api.PodApi;
import org.dubhe.k8s.api.ResourceIisolationApi;
import org.dubhe.k8s.api.ResourceQuotaApi;
import org.dubhe.k8s.api.VolumeApi;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.bo.BuildIngressBO;
import org.dubhe.k8s.domain.bo.BuildFsVolumeBO;
import org.dubhe.k8s.domain.bo.BuildServiceBO;
import org.dubhe.k8s.domain.bo.ModelServingBO;
import org.dubhe.k8s.domain.vo.ModelServingVO;
import org.dubhe.k8s.domain.vo.VolumeVO;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sResponseEnum;
import org.dubhe.k8s.enums.LimitsOfResourcesEnum;
import org.dubhe.k8s.enums.RestartPolicyEnum;
import org.dubhe.k8s.enums.ShellCommandEnum;
import org.dubhe.k8s.utils.BizConvertUtils;
import org.dubhe.k8s.utils.K8sUtils;
import org.dubhe.k8s.utils.LabelUtils;
import org.dubhe.k8s.utils.ResourceBuildUtils;
import org.dubhe.k8s.utils.YamlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description 模型部署接口实现
 * @date 2020-09-10
 */
public class ModelServingApiImpl implements ModelServingApi {
    private K8sUtils k8sUtils;
    private KubernetesClient client;
    @Resource(name = "hostFileStoreApiImpl")
    private FileStoreApi fileStoreApi;
    @Autowired
    private VolumeApi volumeApi;
    @Autowired
    private NodeApi nodeApi;
    @Autowired
    private PodApi podApi;
    @Autowired
    private ResourceQuotaApi resourceQuotaApi;
    @Autowired
    private ResourceIisolationApi resourceIisolationApi;

    @Value("${k8s.serving.host}")
    String servingHost;

    @Value("${k8s.serving.tls-crt}")
    String servingTlsCrt;

    @Value("${k8s.serving.tls-key}")
    String servingTlsKey;

    private static final String MODEL_SERVING_MAX_UPLOAD_SIZE = "100m";

    public ModelServingApiImpl(K8sUtils k8sUtils) {
        this.k8sUtils = k8sUtils;
        this.client = k8sUtils.getClient();
    }

    /**
     * 创建
     * @param bo
     * @return
     */
    @Override
    public ModelServingVO create(ModelServingBO bo) {
        try {
            //资源配额校验
            LimitsOfResourcesEnum limitsOfResources = resourceQuotaApi.reachLimitsOfResources(bo.getNamespace(), bo.getCpuNum(), bo.getMemNum(), bo.getGpuNum());
            if (!LimitsOfResourcesEnum.ADEQUATE.equals(limitsOfResources)) {
                return new ModelServingVO().error(K8sResponseEnum.LACK_OF_RESOURCES.getCode(), limitsOfResources.getMessage());
            }
            LogUtil.info(LogEnum.BIZ_K8S, "Params of creating ModelServing--create:{}", bo);
            if (!fileStoreApi.createDirs(bo.getDirList().toArray(new String[MagicNumConstant.ZERO]))) {
                return new ModelServingVO().error(K8sResponseEnum.INTERNAL_SERVER_ERROR.getCode(), K8sResponseEnum.INTERNAL_SERVER_ERROR.getMessage());
            }
            //存储卷构建
            VolumeVO volumeVO = volumeApi.buildFsVolumes(new BuildFsVolumeBO(bo.getNamespace(), bo.getResourceName(), bo.getFsMounts()));
            if (!K8sResponseEnum.SUCCESS.getCode().equals(volumeVO.getCode())) {
                return new ModelServingVO().error(volumeVO.getCode(), volumeVO.getMessage());
            }

            //名称生成
            String deploymentName = StrUtil.format(K8sParamConstants.RESOURCE_NAME_TEMPLATE, bo.getResourceName(), RandomUtil.randomString(MagicNumConstant.EIGHT));
            String svcName = StrUtil.format(K8sParamConstants.SUB_RESOURCE_NAME_TEMPLATE, bo.getResourceName(), K8sParamConstants.SVC_SUFFIX, RandomUtil.randomString(MagicNumConstant.FIVE));
            String ingressName = StrUtil.format(K8sParamConstants.SUB_RESOURCE_NAME_TEMPLATE, bo.getResourceName(), K8sParamConstants.INGRESS_SUFFIX, RandomUtil.randomString(MagicNumConstant.FIVE));

            //标签生成
            Map<String, String> baseLabels = LabelUtils.getBaseLabels(bo.getResourceName(), bo.getBusinessLabel());
            Map<String, String> podLabels = LabelUtils.getChildLabels(bo.getResourceName(), deploymentName, K8sKindEnum.DEPLOYMENT.getKind(), bo.getBusinessLabel(), bo.getTaskIdentifyLabel());

            //部署deployment
            Deployment deployment = buildDeployment(bo, volumeVO, deploymentName);
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml信息为{}", deploymentName, YamlUtils.dumpAsYaml(deployment));
            resourceIisolationApi.addIisolationInfo(deployment);
            Deployment deploymentResult = client.apps().deployments().inNamespace(bo.getNamespace()).create(deployment);

            //部署service
            BuildServiceBO buildServiceBO = new BuildServiceBO(bo.getNamespace(), svcName, baseLabels, podLabels);
            if (bo.getHttpPort() != null) {
                buildServiceBO.addPort(ResourceBuildUtils.buildServicePort(bo.getHttpPort(), bo.getHttpPort(), SymbolConstant.HTTP));
            }
            if (bo.getGrpcPort() != null) {
                buildServiceBO.addPort(ResourceBuildUtils.buildServicePort(bo.getGrpcPort(), bo.getGrpcPort(), SymbolConstant.GRPC));
            }
            Service service = ResourceBuildUtils.buildService(buildServiceBO);
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml信息为{}", svcName, YamlUtils.dumpAsYaml(service));
            Service serviceResult = client.services().create(service);

            //部署ingress
            BuildIngressBO buildIngressBO = new BuildIngressBO(bo.getNamespace(), ingressName, baseLabels);
            if (StringUtils.isNotEmpty(buildIngressBO.getMaxUploadSize())) {
                buildIngressBO.putAnnotation(K8sParamConstants.INGRESS_PROXY_BODY_SIZE_KEY, buildIngressBO.getMaxUploadSize());
            }
            if (bo.getHttpPort() != null) {
                String httpHost = RandomUtil.randomString(MagicNumConstant.SIX) + SymbolConstant.DOT + servingHost;
                buildIngressBO.addIngressRule(ResourceBuildUtils.buildIngressRule(httpHost, svcName, SymbolConstant.HTTP));
            }
            Secret secretResult = null;
            if (bo.getGrpcPort() != null) {
                String secretName = StrUtil.format(K8sParamConstants.SUB_RESOURCE_NAME_TEMPLATE, bo.getResourceName(), SymbolConstant.TOKEN, RandomUtil.randomString(MagicNumConstant.FIVE));
                Map<String, String> data = new HashMap<String, String>(MagicNumConstant.FOUR) {
                    {
                        put(K8sParamConstants.SECRET_TLS_TLS_CRT, servingTlsCrt);
                        put(K8sParamConstants.SECRET_TLS_TLS_KEY, servingTlsKey);
                    }
                };
                Secret secret = ResourceBuildUtils.buildTlsSecret(bo.getNamespace(), secretName, baseLabels, data);
                secretResult = client.secrets().create(secret);

                String grpcHost = RandomUtil.randomString(MagicNumConstant.SIX) + SymbolConstant.DOT + servingHost;
                buildIngressBO.addIngressRule(ResourceBuildUtils.buildIngressRule(grpcHost, svcName, SymbolConstant.GRPC));
                buildIngressBO.addIngressTLS(ResourceBuildUtils.buildIngressTLS(secretName, grpcHost));
                buildIngressBO.putAnnotation(K8sParamConstants.INGRESS_CLASS_KEY, StringConstant.NGINX_LOWERCASE);
                buildIngressBO.putAnnotation(K8sParamConstants.INGRESS_SSL_REDIRECT_KEY, StringConstant.TRUE_LOWERCASE);
                buildIngressBO.putAnnotation(K8sParamConstants.INGRESS_BACKEND_PROTOCOL_KEY, StringConstant.GRPC_CAPITALIZE);
            }
            Ingress ingress = ResourceBuildUtils.buildIngress(buildIngressBO);
            LogUtil.info(LogEnum.BIZ_K8S, "Ready to deploy {}, yaml信息为{}", ingressName, YamlUtils.dumpAsYaml(ingress));
            Ingress ingressResult = client.extensions().ingresses().create(ingress);

            return new ModelServingVO(BizConvertUtils.toBizSecret(secretResult), BizConvertUtils.toBizService(serviceResult), BizConvertUtils.toBizDeployment(deploymentResult), BizConvertUtils.toBizIngress(ingressResult));
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "ModelOptJobApiImpl.create error, param:{} error:", bo, e);
            return new ModelServingVO().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 删除
     * @param namespace 命名空间
     * @param resourceName 资源名称
     * @return PtBaseResult 基础结果类
     */
    @Override
    public PtBaseResult delete(String namespace, String resourceName) {
        try {
            LogUtil.info(LogEnum.BIZ_K8S, "delete model serving namespace:{} resourceName:{}",namespace,resourceName);
            DeploymentList deploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            if (deploymentList == null || deploymentList.getItems().size() == 0){
                return new PtBaseResult();
            }
            Boolean res = client.extensions().ingresses().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete()
                    && client.services().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete()
                    && client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete()
                    && client.secrets().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).delete();
            if (res) {
                return new PtBaseResult();
            } else {
                return K8sResponseEnum.REPEAT.toPtBaseResult();
            }
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "delete error:", e);
            return new PtBaseResult(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 查询
     * @param namespace
     * @param resourceName
     * @return
     */
    @Override
    public ModelServingVO get(String namespace, String resourceName) {
        try {
            IngressList ingressList = client.extensions().ingresses().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Ingress ingress = CollectionUtil.isEmpty(ingressList.getItems()) ? null : ingressList.getItems().get(0);
            ServiceList svcList = client.services().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Service svc = CollectionUtil.isEmpty(svcList.getItems()) ? null : svcList.getItems().get(0);
            DeploymentList deploymentList = client.apps().deployments().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Deployment deployment = CollectionUtil.isEmpty(deploymentList.getItems()) ? null : deploymentList.getItems().get(0);
            SecretList secretList = client.secrets().inNamespace(namespace).withLabels(LabelUtils.withEnvResourceName(resourceName)).list();
            Secret secret = CollectionUtil.isEmpty(secretList.getItems()) ? null : secretList.getItems().get(0);
            return new ModelServingVO(BizConvertUtils.toBizSecret(secret), BizConvertUtils.toBizService(svc), BizConvertUtils.toBizDeployment(deployment), BizConvertUtils.toBizIngress(ingress));
        } catch (KubernetesClientException e) {
            LogUtil.error(LogEnum.BIZ_K8S, "get error:", e);
            return new ModelServingVO().error(String.valueOf(e.getCode()), e.getMessage());
        }
    }

    /**
     * 构建Deployment
     *
     * @return Deployment
     */
    private Deployment buildDeployment(ModelServingBO bo, VolumeVO volumeVO, String deploymentName) {
        Map<String, String> childLabels = LabelUtils.getChildLabels(bo.getResourceName(), deploymentName, K8sKindEnum.DEPLOYMENT.getKind(), bo.getBusinessLabel(),bo.getTaskIdentifyLabel());
        LabelSelector labelSelector = new LabelSelector();
        labelSelector.setMatchLabels(childLabels);
        return new DeploymentBuilder()
                .withNewMetadata()
                    .withName(deploymentName)
                    .addToLabels(LabelUtils.getBaseLabels(bo.getResourceName(), bo.getBusinessLabel()))
                    .withNamespace(bo.getNamespace())
                .endMetadata()
                .withNewSpec()
                    .withReplicas(bo.getReplicas())
                    .withSelector(labelSelector)
                    .withNewTemplate()
                        .withNewMetadata()
                            .withName(deploymentName)
                            .addToLabels(childLabels)
                            .withNamespace(bo.getNamespace())
                        .endMetadata()
                        .withNewSpec()
                            .addToNodeSelector(K8sUtils.gpuSelector(bo.getGpuNum()))
                            .addToContainers(buildContainer(bo, volumeVO, deploymentName))
                            .addToVolumes(volumeVO.getVolumes().toArray(new Volume[0]))
                            .withRestartPolicy(RestartPolicyEnum.ALWAYS.getRestartPolicy())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();
    }

    /**
     * 构建 Container
     * @param bo
     * @param volumeVO
     * @param name
     * @return
     */
    private Container buildContainer(ModelServingBO bo, VolumeVO volumeVO, String name) {
        Map<String, Quantity> resourcesLimitsMap = Maps.newHashMap();
        Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
        Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.GPU_RESOURCE_KEY, new Quantity(v.toString())));
        Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
        Container container = new ContainerBuilder()
                .withNewName(name)
                .withNewImage(bo.getImage())
                .withNewImagePullPolicy(ImagePullPolicyEnum.IFNOTPRESENT.getPolicy())
                .withVolumeMounts(volumeVO.getVolumeMounts())
                .withNewResources().addToLimits(resourcesLimitsMap).endResources()
                .build();
        if (bo.getCmdLines() != null) {
            container.setCommand(Arrays.asList(ShellCommandEnum.BIN_BANSH.getShell()));
            container.setArgs(bo.getCmdLines());
        }
        List<ContainerPort> ports = new ArrayList<>();
        if (bo.getHttpPort() != null) {
            ports.add(new ContainerPortBuilder()
                    .withContainerPort(bo.getHttpPort())
                    .withName(SymbolConstant.HTTP).build());
        }
        if (bo.getGrpcPort() != null) {
            ports.add(new ContainerPortBuilder()
                    .withContainerPort(bo.getGrpcPort())
                    .withName(SymbolConstant.GRPC).build());
        }
        if (CollectionUtil.isNotEmpty(ports)) {
            container.setPorts(ports);
        }
        return container;
    }
}
