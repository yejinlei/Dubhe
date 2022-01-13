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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.tadl.domain.entity.Trial;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TrialListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("实验id")
    private Long experimentId;

    @ApiModelProperty("实验阶段")
    private Long stageId;

    @ApiModelProperty("trial名称")
    private String name;

    @ApiModelProperty("trial名称")
    private String algorithmName;

    @ApiModelProperty("启动时间")
    private Timestamp startTime;

    @ApiModelProperty("结束时间")
    private Timestamp endTime;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("command命令所使用py文件")
    private String executeScript;

    @ApiModelProperty("最优数据")
    private Double value;

    @ApiModelProperty("运行时间")
    private Long runTime;

    @ApiModelProperty("实验资源值")
    private String resourceName;

    @ApiModelProperty("k8s实验资源值")
    private String k8sResourceName;

    @ApiModelProperty("podName")
    private String podName;

    public static TrialListVO from(Trial trial) {
        return new TrialListVO(){{
            setId(trial.getId());
            setStatus(trial.getStatus());
            setStartTime(trial.getStartTime());
            setStageId(trial.getStageId());
            setEndTime(trial.getEndTime());
            setSequence(trial.getSequence());
            setName(trial.getName());
            setK8sResourceName(trial.getResourceName());
        }};
    }
}