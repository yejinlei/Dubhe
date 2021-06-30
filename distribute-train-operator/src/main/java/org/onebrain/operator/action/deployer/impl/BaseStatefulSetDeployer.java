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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.fabric8.kubernetes.api.model.CapabilitiesBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import org.onebrain.operator.action.deployer.ChildResourceCreateInfo;
import org.onebrain.operator.action.deployer.StatefulSetDeployer;
import org.onebrain.operator.constants.KubeConstants;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.onebrain.operator.constants.NumberConstant.LONG_NUMBER_0;
import static org.onebrain.operator.constants.NumberConstant.LONG_NUMBER_60;
import static org.onebrain.operator.constants.NumberConstant.NUMBER_22;

/**
 * @description StatefullSet部署器
 * @date 2020-09-23
 */
public class BaseStatefulSetDeployer implements StatefulSetDeployer<ChildResourceCreateInfo> {

    public static final String SSH = "ssh";
    public static final String MEMORY = "Memory";
    public static final String DEV_SHM = "/dev/shm";
    public static final String BIN_BASH = "/bin/bash";
    public static final String IPC_LOCK = "IPC_LOCK";

    /**
     * 生成 StatefullSet 信息
     * @param info 资源信息
     * @return
     */
    @Override
    public StatefulSetBuilder deploy(ChildResourceCreateInfo info) {
        //标签筛选
        LabelSelector labelSelector = new LabelSelector();
        labelSelector.setMatchLabels(ImmutableMap.of(KubeConstants.STATEFULSET_LABEL, info.getStatefulSetName()));
        //存储卷
        List<Volume> volumes = buildVolumes(info);
        //容器
        Container container = buildContainer(info);
        //挂载
        List<VolumeMount> volumeMounts = buildVolumeMounts(volumes);

        if (!CollectionUtils.isEmpty(info.getVolumes()) && !CollectionUtils.isEmpty(info.getVolumeMounts())){
            volumes.addAll(info.getVolumes());
            volumeMounts.addAll(info.getVolumeMounts());
        }

        container.setVolumeMounts(volumeMounts);

        //启动命令
        List<String> cmdLines = Arrays.asList("while [ ! -f /home/pretreatment ]; do echo pretreatment not exist >> pretreatment.log; sleep 1;done && chmod a+x /home/pretreatment && bash /home/pretreatment ", "until nslookup " + info.getSvcName() + "; do sleep 5; done", info.getSlaveCmd());
        container.setCommand(Collections.singletonList(BIN_BASH));
        container.setArgs(Arrays.asList("-c", CollectionUtil.join(cmdLines, " && ")));

        //权限
        container.setSecurityContext(new SecurityContextBuilder()
                .withAllowPrivilegeEscalation(true)
//                .withPrivileged(true)
                .withCapabilities(new CapabilitiesBuilder()
                        .withAdd(Collections.singletonList(IPC_LOCK))
                        .build())
                .build());

        //用户自定义的标签
        Map<String,String> customizeLabels = CollectionUtil.isNotEmpty(info.getLabels())? info.getLabels(): new HashMap<>();


        StatefulSetBuilder builder = new StatefulSetBuilder();
        builder.withNewMetadata()
                    .withName(info.getStatefulSetName())
                    .withNamespace(info.getNamespace())
                    .addToOwnerReferences(info.getOwnerReference())
                    .addToLabels(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName())
                .endMetadata()
                .withNewSpec()
                    .withSelector(labelSelector)
                    .withServiceName(info.getStatefulSetName())
                    .withReplicas(info.getSlaveReplicas())
                    .withNewTemplate()
                        .withNewMetadata()
                            .withName(info.getStatefulSetName())
                            .addToLabels(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName())
                            .addToLabels(KubeConstants.STATEFULSET_LABEL, info.getStatefulSetName())
                            .addToLabels(customizeLabels)
                        .endMetadata()
                        .withNewSpec()
                            .withTerminationGracePeriodSeconds(LONG_NUMBER_0)
                            .withTerminationGracePeriodSeconds(LONG_NUMBER_60)
                            .addToContainers(container)
                            .addToVolumes(volumes.toArray(new Volume[0]))
                            .withTolerations(info.getTolerations())
                        .endSpec()
                    .endTemplate()
                .endSpec();

        //init-container
        StatefulSetBuilder finalBuilder = builder;
        Optional.ofNullable(info.getInitContainer())
                .ifPresent(initContainer -> {
                    finalBuilder.editSpec()
                            .editTemplate()
                            .editSpec()
                            .addToInitContainers(initContainer)
                            .endSpec()
                            .endTemplate()
                            .endSpec();
                });

        //固定节点调度
        if(CollectionUtil.isNotEmpty(info.getNodeSelector())){
            builder = builder.editSpec()
                    .editTemplate().editSpec()
                    .addToNodeSelector(info.getNodeSelector())
                    .endSpec().endTemplate()
                    .endSpec();
        }

        return builder;
    }

    /**
     * 构建容器
     * @param info 资源信息
     * @return 容器信息
     */
    private Container buildContainer(ChildResourceCreateInfo info) {
        Container container = new Container();
        //镜像
        container.setName(KubeConstants.SLAVE_CONTAINER_NAME);
        container.setImage(info.getImage());
        container.setImagePullPolicy(info.getImagePullPolicy());
        //端口映射
        container.setPorts(Arrays.asList(new ContainerPortBuilder()
                .withContainerPort(NUMBER_22)
                .withName(SSH).build()));
        //环境变量
        List<EnvVar> envVars = Lists.newArrayList(new EnvVarBuilder()
                .withName(KubeConstants.ENV_NODE_NUM)
                .withValue(String.valueOf(info.getSlaveReplicas() + info.getMasterReplicas()))
                .build());
        Optional.ofNullable(info.getEnv()).ifPresent(v -> envVars.addAll(v));
        container.setEnv(envVars);

        //资源限制
        Optional.ofNullable(info.getSlaveResources()).ifPresent(v -> container.setResources(v));

        return container;
    }

    /**
     * 构建存储卷集合
     * @param info 资源信息
     * @return 存储卷集合
     */
    private List<Volume> buildVolumes(ChildResourceCreateInfo info) {
        List<Volume> volumes = new LinkedList<>();

        //shm默认就有
        volumes.add(new VolumeBuilder()
                .withName(KubeConstants.VOLUME_SHM)
                .withNewEmptyDir()
                .withMedium(MEMORY)
                .endEmptyDir()
                .build());

        return volumes;
    }

    /**
     * 构建挂载存储卷集合
     * @param volumes 存储卷集合
     * @return 构建挂载存储卷集合
     */
    private List<VolumeMount> buildVolumeMounts(List<Volume> volumes) {
        List<VolumeMount> volumeMounts=new LinkedList<>();
        volumeMounts.add(new VolumeMountBuilder()
                .withName(KubeConstants.VOLUME_SHM)
                .withMountPath(DEV_SHM)
                .build());
        return volumeMounts;
    }
}
