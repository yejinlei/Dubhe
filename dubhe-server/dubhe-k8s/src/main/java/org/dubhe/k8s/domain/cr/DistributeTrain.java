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
package org.dubhe.k8s.domain.cr;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

/**
 * @description 自定义资源 DistributeTrain
 * @date 2020-07-07
 */
public class DistributeTrain extends CustomResource implements Namespaced {
    private String apiVersion = "onebrain.oneflow.org/v1alpha1";
    private String kind = "DistributeTrain";

    private DistributeTrainSpec spec;

    @Override
    public String toString() {
        return "DistributeTrain{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public DistributeTrainSpec getSpec() {
        return spec;
    }

    public void setSpec(DistributeTrainSpec spec) {
        this.spec = spec;
    }

    @Override
    public ObjectMeta getMetadata() { return super.getMetadata(); }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public String getKind() {
        return kind;
    }
}
