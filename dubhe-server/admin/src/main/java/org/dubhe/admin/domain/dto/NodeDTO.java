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

package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @description 节点实体类
 * @date 2020-06-03
 */
@Data
public class NodeDTO {


    @ApiModelProperty(value = "node节点id值")
    private String uid;

    @ApiModelProperty(value = "node节点名称")
    private String name;

    @ApiModelProperty(value = "node节点ip地址")
    private String ip;

    @ApiModelProperty(value = "node节点状态")
    private String status;

    @ApiModelProperty(value = "gpu总数")
    private String gpuCapacity;

    @ApiModelProperty(value = "gpu可用数")
    private String gpuAvailable;

    @ApiModelProperty(value = "创建字段保存gpu使用数")
    private String gpuUsed;

    @ApiModelProperty(value = "保存节点信息")
    private List<PodDTO> pods;

    @ApiModelProperty(value = "node节点的使用内存")
    private String nodeMemory;

    @ApiModelProperty(value = "node节点的使用cpu")
    private String nodeCpu;

    @ApiModelProperty(value = "node节点的总的cpu")
    private String totalNodeCpu;

    @ApiModelProperty(value = "node节点的总的内存")
    private String totalNodeMemory;

    @ApiModelProperty(value = "node节点的警告")
    private String warning;
}
