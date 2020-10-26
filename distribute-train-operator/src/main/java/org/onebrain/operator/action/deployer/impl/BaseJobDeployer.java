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

package org.onebrain.operator.action.deployer.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import io.fabric8.kubernetes.api.model.CapabilitiesBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import org.onebrain.operator.action.deployer.ChildResourceCreateInfo;
import org.onebrain.operator.action.deployer.JobDeployer;
import org.onebrain.operator.constants.KubeConstants;

import java.util.*;

import static org.onebrain.operator.constants.NumberConstant.LONG_NUMBER_0;
import static org.onebrain.operator.constants.NumberConstant.NUMBER_1;
import static org.onebrain.operator.constants.NumberConstant.NUMBER_22;

/**
 * @description Job部署器
 * @date 2020-09-23
 */
public class BaseJobDeployer implements JobDeployer<ChildResourceCreateInfo> {

    public static final String PVC_WORKSPACE = "pvc-workspace";
    public static final String SSH = "ssh";
    public static final String WORKSPACE = "/workspace";
    public static final String PVC_DATASET = "pvc-dataset";
    public static final String DATASET = "/dataset";
    public static final String PVC_MODEL = "pvc-model";
    public static final String MODEL = "/model";
    public static final String MEMORY = "Memory";
    public static final String DEV_SHM = "/dev/shm";
    public static final String BIN_BASH = "/bin/bash";
    public static final String IPC_LOCK = "IPC_LOCK";
    public static final String RESTART_POLICY_NEVER = "Never";

    /**
     * 部署Job
     * @param info 资源信息
     * @return
     */
    @Override
    public JobBuilder deploy(ChildResourceCreateInfo info) {

        //容器
        Container container = buildContainer(info);
        //存储卷
        List<Volume> volumes = buildVolumes(info);
        //挂载
        List<VolumeMount> volumeMounts = buildVolumeMounts(volumes);

        container.setVolumeMounts(volumeMounts);

        //启动命令
        container.setCommand(Collections.singletonList(BIN_BASH));
        //训练等待命令
        //一个是等待 pretreatment 文件 通过 podApi 拷贝 到pod上
        //另一个是等待 服务（svc）创建成功
        List<String> cmdLines = Arrays.asList("while [ ! -f /home/pretreatment ]; do echo pretreatment not exist >> pretreatment.log; sleep 1;done && chmod a+x /home/pretreatment && bash /home/pretreatment ", "until nslookup " + info.getSvcName() + "; do sleep 5; done", info.getMasterCmd());
        container.setArgs(Arrays.asList("-c", CollectionUtil.join(cmdLines, " && ")));

        //权限
        container.setSecurityContext(new SecurityContextBuilder()
                .withAllowPrivilegeEscalation(true)
                .withCapabilities(new CapabilitiesBuilder()
                        .withAdd(Collections.singletonList(IPC_LOCK))
                        .build())
                .build());

        //用户自定义的标签
        Map<String,String> customizeLabels = CollectionUtil.isNotEmpty(info.getLabels())? info.getLabels(): new HashMap<>();

        JobBuilder builder = new JobBuilder();
        builder.withNewMetadata()
                    .withName(info.getJobName())
                    .withNamespace(info.getNamespace())
                    .addToLabels(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName())
                    .addToLabels(customizeLabels)
                    .addToOwnerReferences(info.getOwnerReference())
                .endMetadata()
                .withNewSpec()
                    //并行1个
                    .withParallelism(NUMBER_1)
                    //共计运行1次
                    .withCompletions(NUMBER_1)
                    //失败重试次数
                    .withBackoffLimit(KubeConstants.BACKOFFLIMIT)
                    .withNewTemplate()
                        .withNewMetadata()
                            .withName(info.getJobName())
                            .addToLabels(KubeConstants.DISTRIBUTE_TRAIN_LABEL, info.getParentName())
                            .addToLabels(KubeConstants.JOB_LABEL, info.getJobName())
                            .addToLabels(customizeLabels)
                        .endMetadata()
                        .withNewSpec()
                            //关闭指令发出时 立即执行
                            .withTerminationGracePeriodSeconds(LONG_NUMBER_0)
                            .addToContainers(container)
                            .addToVolumes(volumes.toArray(new Volume[volumes.size()]))
                            .withRestartPolicy(RESTART_POLICY_NEVER)
                        .endSpec()
                    .endTemplate()
                .endSpec();

        //init-container
        JobBuilder finalBuilder = builder;
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
    private Container buildContainer(ChildResourceCreateInfo info){
        //容器
        Container container = new Container();
        //镜像
        container.setName(KubeConstants.MASTER_CONTAINER_NAME);
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
        Optional.ofNullable(info.getMasterResources()).ifPresent(v->container.setResources(v));
        return container;
    }

    /**
     * 构建存储卷集合
     * @param info 资源信息
     * @return 存储卷集合
     */
    private List<Volume> buildVolumes(ChildResourceCreateInfo info){
        //存储卷
        List<Volume> volumes = new LinkedList<>();
        Optional.ofNullable(info.getWorkspaceVolume()).ifPresent(v-> volumes.add(v));
        Optional.ofNullable(info.getDatasetVolume()).ifPresent(v-> volumes.add(v));
        Optional.ofNullable(info.getModelVolume()).ifPresent(v-> volumes.add(v));
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
        List<VolumeMount> volumeMounts = new LinkedList<>();
        for (Volume volume : volumes) {
            if(PVC_WORKSPACE.equals(volume.getName())){
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(WORKSPACE)
                        .build());
                continue;
            }
            if(PVC_DATASET.equals(volume.getName())){
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(DATASET)
                        .build());
                continue;
            }
            if(PVC_MODEL.equals(volume.getName())){
                volumeMounts.add(new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(MODEL)
                        .build());
                continue;
            }
        }

        volumeMounts.add(new VolumeMountBuilder()
                .withName(KubeConstants.VOLUME_SHM)
                .withMountPath(DEV_SHM)
                .build());
        return volumeMounts;
    }
}
