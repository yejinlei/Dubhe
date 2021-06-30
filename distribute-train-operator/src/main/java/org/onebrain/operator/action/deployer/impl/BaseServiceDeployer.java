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

package org.onebrain.operator.action.deployer.impl;

import cn.hutool.core.collection.CollectionUtil;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.onebrain.operator.action.deployer.ChildResourceCreateInfo;
import org.onebrain.operator.action.deployer.ServiceDeployer;
import org.onebrain.operator.constants.KubeConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.onebrain.operator.constants.NumberConstant.NUMBER_22;
import static org.onebrain.operator.constants.NumberConstant.NUMBER_30000;

/**
 * @description Service部署器
 * @date 2020-09-23
 */
public class BaseServiceDeployer implements ServiceDeployer<ChildResourceCreateInfo> {

    public static final String WEB_SSH = "web-ssh";
    public static final String NONE = "None";

    /**
     * 构建service信息
     * @param info 资源信息
     * @return
     */
    @Override
    public ServiceBuilder deploy(ChildResourceCreateInfo info) {

        //用户自定义的标签
        Map<String,String> customizeLabels = CollectionUtil.isNotEmpty(info.getLabels())? info.getLabels(): new HashMap<>();

        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(info.getSvcName())
                    .addToLabels(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName())
                    .addToLabels(customizeLabels)
                    .withNamespace(info.getNamespace())
                    .addToOwnerReferences(info.getOwnerReference())
                .endMetadata()
                .withNewSpec()
                    .addNewPort()
                        .withPort(NUMBER_30000)
                        .withTargetPort(new IntOrString(NUMBER_22))
                        .withName(WEB_SSH)
                    .endPort()
                    .withClusterIP(NONE)
                    //选择带有分布式训练的节点
                    .withSelector(Collections.singletonMap(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName()))
                .endSpec();
    }
}
