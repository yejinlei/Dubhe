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

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.api.model.extensions.IngressTLSBuilder;
import org.dubhe.constant.SymbolConstant;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.bo.BuildIngressBO;
import org.dubhe.k8s.domain.bo.BuildServiceBO;

import java.util.Map;

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
        return new ServiceBuilder()
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
    }

    /**
     * 构建 ServicePort
     * @param targetPort
     * @param port
     * @param name
     * @return
     */
    public static ServicePort buildServicePort(Integer targetPort,Integer port,String name){
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
    public static IngressTLS buildIngressTLS(String secretName,String host){
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
    public static Secret buildTlsSecret(String namespace, String name, Map<String, String> labels,Map<String, String> map){
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
}
