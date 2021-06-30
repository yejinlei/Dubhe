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

package org.dubhe.k8s.domain.bo;

import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.SymbolConstant;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.k8s.constant.K8sParamConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 构建 Ingress
 * @date 2020-09-11
 */
@Data
@Accessors(chain = true)
public class BuildIngressBO {
    /**
     * 命名空间
     **/
    private String namespace;
    /**
     * 名称
     */
    private String name;
    /**
     * 标签
     */
    private Map<String, String> labels;
    /**
     * 上传限制
     */
    private String maxUploadSize;
    /**
     * 根路径
     */
    private String path;
    /**
     * A list of host rules used to configure the Ingress
     **/
    private List<IngressRule> ingressRules;
    /**
     * TLS configuration
     **/
    private List<IngressTLS> ingressTLSs;

    private Map<String, String> annotations;

    public BuildIngressBO(String namespace, String name, Map<String, String> labels){
        this.namespace = namespace;
        this.name = name;
        this.labels = labels;
        this.maxUploadSize = K8sParamConstants.INGRESS_MAX_UPLOAD_SIZE;
        this.path = SymbolConstant.SLASH;
    }

    /**
     * 添加ingress 规则
     * @param ingressRule
     */
    public void addIngressRule(IngressRule ingressRule){
        if (null == ingressRule){
            return;
        }
        if (ingressRules == null){
            ingressRules = new ArrayList<>();
        }
        ingressRules.add(ingressRule);
    }

    /**
     * 添加 ingress tls
     * @param ingressTLS
     */
    public void addIngressTLS(IngressTLS ingressTLS){
        if (null == ingressTLS){
            return;
        }
        if (ingressTLSs == null){
            ingressTLSs = new ArrayList<>();
        }
        ingressTLSs.add(ingressTLS);
    }

    /**
     * 设置 annotation
     * @param key
     * @param value
     */
    public void putAnnotation(String key,String value){
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)){
            return;
        }
        if (annotations == null){
            annotations = new HashMap<>();
        }
        annotations.put(key,value);
    }
}
