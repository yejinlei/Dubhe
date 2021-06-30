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
import io.fabric8.kubernetes.api.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description 分布式训练详细规格
 * @date 2020-09-23
 */
@JsonDeserialize(
    using = JsonDeserializer.None.class
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributeTrainSpec implements KubernetesResource {

    /**
     * 镜像
     */
    private String image;

    /**
     * 镜像拉取策略
     */
    private String imagePullPolicy;
    /**
     * 机器数
     */
    private Integer size;

    /**
     * 环境参数
     */
    private List<EnvVar> env;

    /**
     * master 命令
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
    private Map<String,String> nodeSelector;

    /**
     * 初始化容器
     */
    private Container initContainer;

    /**
     * 工作目录挂载
     */
    private Volume workspaceStorage;

    /**
     * 数据集目录挂载
     */
    private Volume datasetStorage;

    /**
     * 模型目录挂载
     */
    private Volume modelStorage;

    /**
     * 内部映射
     */
    private List<VolumeMount> volumeMounts;
    /**
     * 外部挂载
     */
    private List<Volume> volumes;

    /**
     * 容忍度
     */
    private List<Toleration> tolerations;

}
