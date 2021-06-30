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
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.domain.PtBaseResult;

import java.util.List;
import java.util.Map;

/**
 * @description BizDistributeTrain实体类
 * @date 2020-07-08
 */
@Data
@Accessors(chain = true)
public class BizDistributeTrain extends PtBaseResult<BizDistributeTrain> {
    @K8sField("apiVersion")
    private String apiVersion;
    @K8sField("kind")
    private String kind;
    @K8sField("metadata:name")
    private String name;
    @K8sField("metadata:labels")
    private Map<String, String> labels = Maps.newHashMap();
    @K8sField("metadata:namespace")
    private String namespace;
    @K8sField("spec:size")
    private Integer size;
    @K8sField("spec:image")
    private String image;
    @K8sField("spec:masterCmd")
    private String masterCmd;
    @K8sField("spec:masterResources")
    private BizDistributeTrainResources masterResources;
    @K8sField("spec:slaveCmd")
    private String slaveCmd;
    @K8sField("spec:slaveResources")
    private BizDistributeTrainResources slaveResources;
    @K8sField("spec:nodeSelector")
    private Map<String, String> nodeSelector = Maps.newHashMap();
    @K8sField("spec:volumeMounts")
    private List<VolumeMount> volumeMounts;
    @K8sField("spec:volumes")
    private List<Volume> volumes;
}
