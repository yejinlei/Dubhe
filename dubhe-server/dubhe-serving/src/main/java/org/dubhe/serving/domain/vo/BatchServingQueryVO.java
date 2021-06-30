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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 批量服务查询返回对象
 * @date 2020-08-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchServingQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "批量服务id")
    private Long id;

    @ApiModelProperty(value = "批量服务名称")
    private String name;

    @ApiModelProperty(value = "批量服务类型：0-Restful，1-gRPC")
    private Integer type;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "状态对应的详情信息")
    private String statusDetail;

    @ApiModelProperty(value = "进度")
    private String progress;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "开始时间")
    private Timestamp startTime;

    @ApiModelProperty(value = "结束时间")
    private Timestamp endTime;

    @ApiModelProperty(value = "输出路径")
    private String outputPath;
}
