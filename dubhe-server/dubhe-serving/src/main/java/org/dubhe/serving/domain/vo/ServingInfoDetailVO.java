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
package org.dubhe.serving.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @description 查询服务详情返回
 * @date 2020-08-27
 */
@Data
@Accessors(chain = true)
public class ServingInfoDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务ID")
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String name;

    @ApiModelProperty(value = "服务类型：0-Restful，1-gRPC")
    private Integer type;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "模型配置列表")
    private List<ServingModelConfigVO> modelConfigList;

    @ApiModelProperty(value = "调用失败次数")
    private String failNum;

    @ApiModelProperty(value = "调用总次数")
    private String totalNum;

    @ApiModelProperty(value = "运行节点数")
    private Integer runningNode;

    @ApiModelProperty(value = "服务总节点数")
    private Integer totalNode;

    @ApiModelProperty(value = "服务状态")
    private String status;

    @ApiModelProperty(value = "状态对应的详情信息")
    private String statusDetail;

    @ApiModelProperty(value = "创建人id")
    private Long createUserId;
}
