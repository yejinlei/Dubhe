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

package org.dubhe.k8s.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.api.model.extensions.IngressTLSBuilder;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.BuildIngressBO;
import org.dubhe.k8s.domain.bo.BuildServiceBO;
import org.dubhe.k8s.domain.bo.DeploymentBO;
import org.dubhe.k8s.domain.bo.ModelServingBO;
import org.dubhe.k8s.domain.vo.VolumeVO;
import org.dubhe.k8s.enums.ImagePullPolicyEnum;
import org.dubhe.k8s.enums.K8sKindEnum;
import org.dubhe.k8s.enums.K8sTolerationEffectEnum;
import org.dubhe.k8s.enums.K8sTolerationOperatorEnum;
import org.dubhe.k8s.enums.RestartPolicyEnum;
import org.dubhe.k8s.enums.ShellCommandEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.dubhe.biz.base.constant.MagicNumConstant.ZERO_LONG;

/**
 * @description 构建 Kubernetes 资源对象
 * @date 2020-09-10
 */
public class ResourceBuildUtils {

    /**
     * 构建 Service
     * @param bo
     * @return
     */
    public static Service buildService(BuildServiceBO bo) {
        Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName(bo.getName())
                    .addToLabels(bo.getLabels())
                    .withNamespace(bo.getNamespace())
                .endMetadata()
                .withNewSpec()
                    .withPorts(bo.getPorts())
                    .withSelector(bo.getSelector())
                .endSpec()
                .build();
        if (!StringUtils.isEmpty(bo.getType())){
            service.getSpec().setType(bo.getType());
        }
        return service;
    }

    /**
     * 构建 ServicePort
     * @param targetPort
     * @param port
     * @param name
     * @return
     */
    public static ServicePort buildServicePort(Integer targetPort, Integer port, String name){
        ServicePort servicePort = new ServicePortBuilder()
                .withNewTargetPort(targetPort)
                .withPort(port)
                .withName(name)
                .build();
        return servicePort;
    }

    /**
     * 构建 IngressRule
     * @param host
     * @param serviceName
     * @param servicePort
     * @return
     */
    public static IngressRule buildIngressRule(String host, String serviceName, Integer servicePort){
        return new IngressRuleBuilder()
                .withHost(host)
                .withNewHttp()
                    .addNewPath()
                        .withPath(SymbolConstant.SLASH)
                        .withNewBackend()
                            .withNewServiceName(serviceName)
                            .withNewServicePort(servicePort)
                        .endBackend()
                    .endPath()
                .endHttp()
                .build();
    }

    /**
     * 构建 IngressRule
     * @param host
     * @param serviceName
     * @param servicePort
     * @return
     */
    public static IngressRule buildIngressRule(String host, String serviceName, String servicePort){
        return new IngressRuleBuilder()
                .withHost(host)
                .withNewHttp()
                    .addNewPath()
                        .withPath(SymbolConstant.SLASH)
                        .withNewBackend()
                            .withNewServiceName(serviceName)
                            .withNewServicePort(servicePort)
                        .endBackend()
                    .endPath()
                .endHttp()
                .build();
    }

    /**
     * 构建 IngressTLS
     * @param secretName
     * @param host
     * @return
     */
    public static IngressTLS buildIngressTLS(String secretName, String host){
        return new IngressTLSBuilder()
                .withSecretName(secretName)
                .withHosts(host)
                .build();
    }

    /**
     * 构建 Ingress
     * @param bo
     * @return
     */
    public static Ingress buildIngress(BuildIngressBO bo) {
        return new IngressBuilder()
                .withNewMetadata()
                    .withName(bo.getName())
                    .addToLabels(bo.getLabels())
                    .withNamespace(bo.getNamespace())
                    .addToAnnotations(bo.getAnnotations())
                .endMetadata()
                .withNewSpec()
                    .withRules(bo.getIngressRules())
                    .withTls(bo.getIngressTLSs())
                .endSpec()
                .build();
    }

    /**
     * 构建 Secret
     * @param namespace
     * @param name
     * @param labels
     * @param map
     * @return
     */
    public static Secret buildTlsSecret(String namespace, String name, Map<String, String> labels, Map<String, String> map){
        Secret secret = new SecretBuilder()
                    .withType(K8sParamConstants.SECRET_TLS_TYPE)
                    .withNewMetadata()
                        .withName(name)
                        .addToLabels(labels)
                        .withNamespace(namespace)
                    .endMetadata()
                    .addToData(map)
                    .build();
        return secret;
    }

    /**
     * 构建 Toleration
     *
     * @param effect
     * @param key
     * @param operator
     * @param tolerationSeconds
     * @param value
     * @return
     */
    public static Toleration buildToleration(String effect,String key,String operator,Long tolerationSeconds,String value){
        return new Toleration(effect,key,operator,tolerationSeconds,value);
    }

    /**
     * 构建 effect=NoSchedule operator=Equal 的 Toleration
     *
     * @param key 键
     * @param value 值
     * @return
     */
    public static Toleration buildNoScheduleEqualToleration(String key,String value){
        return new Toleration(K8sTolerationEffectEnum.NOSCHEDULE.getEffect(),key, K8sTolerationOperatorEnum.EQUAL.getOperator(),null,value);
    }

    /**
     * 构建Deployment
     *
     * @return Deployment
     */
    public static Deployment buildDeployment(DeploymentBO bo, VolumeVO volumeVO, String deploymentName) {
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
                            .withTerminationGracePeriodSeconds(ZERO_LONG)
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
    public static Container buildContainer(DeploymentBO bo, VolumeVO volumeVO, String name) {
        Map<String, Quantity> resourcesLimitsMap = Maps.newHashMap();
        Optional.ofNullable(bo.getCpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_CPU_KEY, new Quantity(v.toString(), K8sParamConstants.CPU_UNIT)));
        Optional.ofNullable(bo.getGpuNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.GPU_RESOURCE_KEY, new Quantity(v.toString())));
        Optional.ofNullable(bo.getMemNum()).ifPresent(v -> resourcesLimitsMap.put(K8sParamConstants.QUANTITY_MEMORY_KEY, new Quantity(v.toString(), K8sParamConstants.MEM_UNIT)));
        Container container = new ContainerBuilder()
                .withNewName(name)
                .withNewImage(bo.getImage())
                .withNewImagePullPolicy(StringUtils.isEmpty(bo.getImagePullPolicy())?ImagePullPolicyEnum.IFNOTPRESENT.getPolicy():bo.getImagePullPolicy())
                .withVolumeMounts(volumeVO.getVolumeMounts())
                .withNewResources().addToLimits(resourcesLimitsMap).endResources()
                .build();
        if (bo.getCmdLines() != null) {
            container.setCommand(Arrays.asList(ShellCommandEnum.BIN_BANSH.getShell()));
            container.setArgs(bo.getCmdLines());
        }
        List<ContainerPort> ports = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bo.getPorts())){
            bo.getPorts().forEach(port ->{
                ports.add(new ContainerPortBuilder()
                    .withContainerPort(port)
                    .withName(SymbolConstant.PORT+SymbolConstant.HYPHEN+port).build());
            });
            container.setPorts(ports);
        }
        return container;
    }
}
