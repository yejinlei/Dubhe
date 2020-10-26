/**
 * Copyright 2020 Zhejiang Lab & The OneFlow Authors. All Rights Reserved.
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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 分布式训练
 * @date 2020-09-24
 */
@Data
@NoArgsConstructor
public class DistributeTrain extends CustomResource {

    /**
     * 分布式训练详细规格
     */
    private DistributeTrainSpec spec;

    /**
     * 分布式训练状态
     */
    private DistributeTrainStatus status;

    public DistributeTrain(ObjectMeta objectMeta, DistributeTrainSpec spec) {
        this.setMetadata(objectMeta);
        this.spec = spec;
    }
}
