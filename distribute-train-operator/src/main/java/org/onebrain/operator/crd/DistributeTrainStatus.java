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

package org.onebrain.operator.crd;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import lombok.Data;

/**
 * @description 分布式训练状态
 * @date 2020-09-23
 */
@JsonDeserialize(
    using = JsonDeserializer.None.class
)
@Data
public class DistributeTrainStatus implements KubernetesResource {

    /**
     * 副本数
     */
    private Integer replicas;

    /**
     * 处在ready状态的副本数
     */
    private Integer readyReplicas;

    /**
     * 成功数
     */
    private Integer success;

    /**
     * 失败数
     */
    private Integer failed;

}
