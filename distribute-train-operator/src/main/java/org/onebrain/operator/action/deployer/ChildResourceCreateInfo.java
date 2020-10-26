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

package org.onebrain.operator.action.deployer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.fabric8.kubernetes.api.model.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.onebrain.operator.constants.KubeConstants;
import org.onebrain.operator.constants.NumberConstant;
import org.onebrain.operator.crd.DistributeTrain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description 暂存创建子资源所需的信息
 * @date 2020-06-16
 */
@Data
@Accessors(chain = true)
public class ChildResourceCreateInfo extends AbstractResourceCreateInfo {

    public static final String SLAVE_TEMPLATE = "{}-slave-{}";
    public static final String MASTER_TEMPLATE = "{}-master-{}";
    public static final String SVC_TEMPLATE = "{}-svc";
    /**
     * 父级名称（分布式训练名称）
     */
    private String parentName;

    /**
     * job名称
     */
    private String jobName;

    /**
     * statefullSet名称
     */
    private String statefulSetName;

    /**
     * 服务名称
     */
    private String svcName;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 镜像
     */
    private String image;

    /**
     * 镜像拉取策略
     */
    private String imagePullPolicy;

    /**
     * 标签
     */
    private Map<String, String> labels;

    /**
     * master副本数
     */
    private Integer masterReplicas;

    /**
     * slave副本数
     */
    private Integer slaveReplicas;

    /**
     * master命令
     */
    private String masterCmd;

    /**
     * slave命令
     */
    private String slaveCmd;

    /**
     * master 资源节点限制
     */
    private ResourceRequirements masterResources;

    /**
     * slave 资源节点限制
     */
    private ResourceRequirements slaveResources;

    /**
     * 节点调度选择器
     */
    private Map<String, String> nodeSelector;

    /**
     * 初始化容器
     */
    private Container initContainer;

    /**
     * 工作目录挂载
     */
    private Volume workspaceVolume;

    /**
     * 数据集目录挂载
     */
    private Volume datasetVolume;

    /**
     * 模型目录挂载
     */
    private Volume modelVolume;

    /**
     * 环境变量
     */
    private List<EnvVar> env;

    /**
     * 拥有者信息
     */
    private OwnerReference ownerReference;

    /**
     * 将分布式训练转换为K8S的资源信息
     * @param distributeTrain 分布式训练
     * @return ChildResourceCreateInfo
     */
    public static ChildResourceCreateInfo fromCr(DistributeTrain distributeTrain){
        ChildResourceCreateInfo info = new ChildResourceCreateInfo();
        //ownerReferece信息
        info.generateOwnerReference(distributeTrain);
        //各种资源的名称
        info.setNamespace(distributeTrain.getMetadata().getNamespace());
        info.setParentName(distributeTrain.getMetadata().getName());
        info.generateResoureName();
        //标签
        info.setLabels(distributeTrain.getMetadata().getLabels());
        //镜像
        info.setImage(distributeTrain.getSpec().getImage())
                .setImagePullPolicy(distributeTrain.getSpec().getImagePullPolicy());
        //副本数
        Integer size = distributeTrain.getSpec().getSize();
        info.setMasterReplicas(NumberConstant.NUMBER_1);
        info.setSlaveReplicas(size - NumberConstant.NUMBER_1);
        //命令行
        info.setMasterCmd(distributeTrain.getSpec().getMasterCmd())
                .setSlaveCmd(distributeTrain.getSpec().getSlaveCmd());
        //挂载
        Optional.ofNullable(distributeTrain.getSpec().getWorkspaceStorage())
                .ifPresent(v -> info.setWorkspaceVolume(v));
        Optional.ofNullable(distributeTrain.getSpec().getDatasetStorage())
                .ifPresent(v -> info.setDatasetVolume(v));
        Optional.ofNullable(distributeTrain.getSpec().getModelStorage())
                .ifPresent(v -> info.setModelVolume(v));

        //主从两组资源限制
        Optional.ofNullable(distributeTrain.getSpec().getMasterResources())
                .ifPresent(v -> info.setMasterResources(v));
        Optional.ofNullable(distributeTrain.getSpec().getSlaveResources())
                .ifPresent(v -> info.setSlaveResources(v));

        //环境变量
        List<EnvVar> env = distributeTrain.getSpec().getEnv();
        if(CollectionUtil.isNotEmpty(env)){
            env = env.stream().filter(e -> !KubeConstants.ENV_NODE_NUM.equals(e.getName())).collect(Collectors.toList());
            info.setEnv(env);
        }

        //node调度
        info.setNodeSelector(distributeTrain.getSpec().getNodeSelector());

        //init-container
        info.setInitContainer(distributeTrain.getSpec().getInitContainer());

        return info;
    }

    /**
     * 生成资源名称
     */
    private void generateResoureName(){
        String suffix = getRandomStr(NumberConstant.NUMBER_5);
        this.statefulSetName = StrUtil.format(SLAVE_TEMPLATE, this.parentName, suffix);
        this.jobName = StrUtil.format(MASTER_TEMPLATE, this.parentName, suffix);
        this.svcName = StrUtil.format(SVC_TEMPLATE, this.parentName);
    }

    /**
     * 生成所有者信息
     * @param distributeTrain 分布式训练
     */
    private void generateOwnerReference(DistributeTrain distributeTrain){
        this.ownerReference = new OwnerReferenceBuilder()
                .withApiVersion(distributeTrain.getApiVersion())
                .withKind(distributeTrain.getKind())
                .withName(distributeTrain.getMetadata().getName())
                .withNewUid(distributeTrain.getMetadata().getUid())
                .build();
    }
}
