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

import org.dubhe.k8s.annotation.K8sField;
import org.dubhe.k8s.domain.PtBaseResult;
import org.dubhe.k8s.domain.resource.BizContainer;
import org.dubhe.k8s.utils.MappingUtils;
import io.fabric8.kubernetes.api.model.batch.Job;
import lombok.Data;

import java.util.List;

/**
 * @description job result
 * @date 2020-04-22
 */
@Data
public class PtJupyterJobVO extends PtBaseResult<PtJupyterJobVO> {

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

    public static PtJupyterJobVO getInstance(Job job) {
        return MappingUtils.mappingTo(job, PtJupyterJobVO.class);
    }
}
