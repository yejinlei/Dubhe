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

package org.dubhe.k8s.domain.resource;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.constant.K8sLabelConstants;
import org.dubhe.k8s.domain.PtBaseResult;

import java.util.List;
import java.util.Map;

/**
 * @description Deployment 业务类
 * @date 2020-05-28
 */
@Data
@Accessors(chain = true)
public class BizDeployment extends PtBaseResult<BizDeployment> {
    @K8sField("apiVersion")
    private String apiVersion;
    @K8sField("kind")
    private String kind;

    @K8sField("metadata:creationTimestamp")
    private String creationTimestamp;
    @K8sField("metadata:name")
    private String name;
    @K8sField("metadata:labels")
    private Map<String, String> labels = Maps.newHashMap();
    @K8sField("metadata:namespace")
    private String namespace;
    @K8sField("metadata:uid")
    private String uid;
    @K8sField("metadata:resourceVersion")
    private String resourceVersion;

    @K8sField("spec:template:spec:containers")
    private List<BizContainer> containers;

    @K8sField("status:conditions")
    private List<BizDeploymentCondition> conditions;

    @K8sField("status:replicas")
    private Integer replicas;

    @K8sField("status:readyReplicas")
    private Integer readyReplicas;

    /**
     * 获取业务标签
     * @return
     */
    public String getBusinessLabel() {
        return labels.get(K8sLabelConstants.BASE_TAG_BUSINESS);
    }

    /**
     * 根据键获取label
     *
     * @param labelKey
     * @return
     */
    public String getLabel(String labelKey) {
        return labels.get(labelKey);
    }
}
