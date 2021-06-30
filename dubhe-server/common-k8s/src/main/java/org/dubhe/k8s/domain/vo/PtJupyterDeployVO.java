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

package org.dubhe.k8s.domain.vo;

import cn.hutool.core.codec.Base64;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.constant.K8sParamConstants;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.utils.MappingUtils;

import java.util.List;
import java.util.Optional;

/**
 * @description Notebook deploy result
 * @date 2020-04-17
 */
@Data
@Accessors(chain = true)
public class PtJupyterDeployVO extends PtBaseResult<PtJupyterDeployVO> {

    private String defaultJupyterPwd;
    private String baseUrl;
    private SecretInfo secretInfo;
    private StatefulSetInfo statefulSetInfo;
    private ServiceInfo serviceInfo;
    private IngressInfo ingressInfo;

    public PtJupyterDeployVO() {

    }

    public PtJupyterDeployVO(Secret secret, StatefulSet statefulSet, Service service, Ingress ingress) {
        Optional.ofNullable(secret).ifPresent(v -> {
            String base64edPwd = v.getData().get(K8sParamConstants.SECRET_PWD_KEY);
            String base64edBaseUrl = v.getData().get(K8sParamConstants.SECRET_URL_KEY);
            this.defaultJupyterPwd = Base64.decodeStr(base64edPwd);
            this.baseUrl = Base64.decodeStr(base64edBaseUrl) + SymbolConstant.SLASH;
            this.secretInfo = MappingUtils.mappingTo(v, SecretInfo.class);
        });
        Optional.ofNullable(statefulSet).ifPresent(v -> {
            this.statefulSetInfo = MappingUtils.mappingTo(v, StatefulSetInfo.class);
        });
        Optional.ofNullable(service).ifPresent(v -> {
            this.serviceInfo = MappingUtils.mappingTo(v, ServiceInfo.class);
        });
        Optional.ofNullable(ingress).ifPresent(v -> {
            this.ingressInfo = MappingUtils.mappingTo(v, IngressInfo.class);
        });
    }

    @Data
    public static class SecretInfo {
        @K8sField("metadata:name")
        private String name;
        @K8sField("metadata:namespace")
        private String namespace;
        @K8sField("metadata:uid")
        private String uid;
    }

    @Data
    public static class StatefulSetInfo {
        @K8sField("metadata:name")
        private String name;
        @K8sField("metadata:namespace")
        private String namespace;
        @K8sField("metadata:uid")
        private String uid;
        @K8sField("spec:template:metadata:uid")
        private String podId;
        @K8sField("spec:template:spec:containers")
        private List<BizContainer> containers;
    }

    @Data
    public static class ServiceInfo {
        @K8sField("metadata:name")
        private String name;
        @K8sField("metadata:namespace")
        private String namespace;
        @K8sField("metadata:uid")
        private String uid;
    }

    @Data
    public static class IngressInfo {
        @K8sField("metadata:name")
        private String name;
        @K8sField("metadata:namespace")
        private String namespace;
        @K8sField("metadata:uid")
        private String uid;
        @K8sField("spec:rules")
        private List<BizIngressRule> rules;
    }

    @Data
    public static class BizIngressRule {
        @K8sField("host")
        private String host;
        @K8sField("http")
        private HTTPIngressRuleValue http;
    }
}
