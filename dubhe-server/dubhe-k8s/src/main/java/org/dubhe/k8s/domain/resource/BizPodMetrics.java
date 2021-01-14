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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.Duration;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.ContainerMetrics;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.enums.K8sKindEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description BizPodMetrics实体类
 * @date 2020-05-22
 */
@Data
@Accessors(chain = true)
public class BizPodMetrics {
    @K8sField("apiVersion")
    private java.lang.String apiVersion = "metrics.k8s.io/v1beta1";

    @K8sField("containers")
    private List<ContainerMetrics> containers = new ArrayList<>();

    @K8sField("kind")
    private String kind = K8sKindEnum.PODMETRICS.getKind();

    @K8sField("metadata")
    private ObjectMeta metadata;

    @K8sField("timestamp")
    private String timestamp;

    @K8sField("window")
    private Duration window;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>(0);
}
