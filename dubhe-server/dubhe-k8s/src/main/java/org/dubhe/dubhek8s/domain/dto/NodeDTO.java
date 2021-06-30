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

package org.dubhe.dubhek8s.domain.dto;

import lombok.Data;

import java.util.List;


/**
 * @description 节点实体类
 * @date 2020-06-03
 */
@Data
public class NodeDTO {

    /**
     * node节点id值
     */
    private String uid;
    /**
     * node节点名称
     */
    private String name;
    /**
     * node节点ip地址
     */
    private String ip;
    /**
     * node节点状态
     */
    private String status;
    /**
     * gpu总数
     */
    private String gpuCapacity;
    /**
     * gpu可用数
     */
    private String gpuAvailable;
    /**
     * 创建字段保存gpu使用数
     */
    private String gpuUsed;
    /**
     * 保存节点信息
     */
      private List<PodDTO> pods;
    /**
     * node节点的使用内存
     */
    private String nodeMemory;
    /**
     * node节点的使用cpu
     */
    private String nodeCpu;
    /**
     * node节点的总的cpu
     */
    private String totalNodeCpu;
    /**
     * node节点的总的内存
     */
    private String totalNodeMemory;
    /**
     * node节点的警告
     */
    private String  warning;

    /**
     * 资源隔离环境
     */
    private String isolationEnv;

    /**
     * 资源隔离id
     */
    private Long isolationId;

    /**
     * 资源占有对象
     */
    private String isolation;
}
