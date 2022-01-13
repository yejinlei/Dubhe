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
package org.dubhe.tadl.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @description 实验列表分页查询
 * @date 2021-08-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("实验名称")
    private String name;

    @ApiModelProperty("实验状态：（待运行：101 等待中：102，运行中：103，已暂停：104已终止：105，已完成：106，运行失败：107）")
    private Integer status;

    @ApiModelProperty("状态列表")
    private List<StageVO> stages;

    @ApiModelProperty("模型类型")
    private Integer modelType;

    @ApiModelProperty("启动时间")
    private Date startTime;

    @ApiModelProperty("运行时间")
    private Long runTime;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("实验描述")
    private String description;

    @ApiModelProperty("状态对应的详情信息")
    private String statusDetail;

    private Long createUserId;

    @Data
    public static class StageVO {

        @ApiModelProperty("阶段名称")
        private String stageName;

        @ApiModelProperty("阶段状态")
        private Integer status;

        @ApiModelProperty("阶段结束时间")
        private Timestamp endTime;
    }

}
