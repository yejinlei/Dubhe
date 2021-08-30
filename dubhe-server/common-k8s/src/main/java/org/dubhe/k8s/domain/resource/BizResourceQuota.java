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

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.domain.PtBaseResult;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description ResourceQuota 业务类
 * @date 2020-04-23
 */
@Data
@Accessors(chain = true)
public class BizResourceQuota extends PtBaseResult<BizResourceQuota> {
    @K8sField("apiVersion")
    private String apiVersion;
    @K8sField("kind")
    private String kind;

    @K8sField("metadata:creationTimestamp")
    private String creationTimestamp;
    @K8sField("metadata:name")
    private String name;
    @K8sField("metadata:namespace")
    private String namespace;
    @K8sField("metadata:uid")
    private String uid;
    @K8sField("metadata:resourceVersion")
    private String resourceVersion;

    @K8sField("status:hard")
    private Map<String, BizQuantity> hard;

    @K8sField("status:used")
    private Map<String, BizQuantity> used;

    @K8sField("spec:scopeSelector:matchExpressions")
    private List<BizScopedResourceSelectorRequirement> matchExpressions;

    /**
     * 获取余量
     * @return 余量列表
     */
    public Map<String, BizQuantity> getRemainder(){
        Map<String, BizQuantity> remainder = new HashMap<>();
        if (!CollectionUtils.isEmpty(hard)){
            for (Map.Entry<String, BizQuantity> entry : hard.entrySet()) {
                if (used.get(entry.getKey()) != null){
                    remainder.put(entry.getKey(),entry.getValue().reduce(used.get(entry.getKey()),entry.getKey()));
                }
            }
        }
        return remainder;
    }
}
