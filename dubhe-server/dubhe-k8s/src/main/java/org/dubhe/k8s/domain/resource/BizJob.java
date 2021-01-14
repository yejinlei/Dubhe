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

import org.dubhe.k8s.annotation.K8sField;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.domain.PtBaseResult;

import java.util.List;
import java.util.Map;

/**
 * @description BizJob实体类
 * @date 2020-04-22
 */
@Data
@Accessors(chain = true)
public class BizJob extends PtBaseResult<BizJob> {
    @K8sField("metadata:name")
    private String name;
    @K8sField("metadata:labels")
    private Map<String, String> labels = Maps.newHashMap();
    @K8sField("metadata:namespace")
    private String namespace;
    @K8sField("metadata:uid")
    private String uid;
    @K8sField("spec:template:spec:containers")
    private List<BizContainer> containers;
    @K8sField("status:completionTime")
    private String completionTime;
    @K8sField("status:conditions")
    private List<BizJobCondition> conditions;
    @K8sField("status:startTime")
    private String startTime;
    @K8sField("status:succeeded")
    private Integer succeeded;
}
