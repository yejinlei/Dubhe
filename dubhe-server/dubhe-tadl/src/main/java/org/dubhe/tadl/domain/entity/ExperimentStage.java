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
package org.dubhe.tadl.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.ExperimentCreateDTO;
import org.dubhe.tadl.domain.dto.ExperimentUpdateDTO;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;

import java.io.Serializable;
import java.sql.Timestamp;


@Builder
@ToString
@Data
@TableName("tadl_experiment_stage")
@ApiModel(value = "ExperimentStage 对象", description = "实验阶段表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExperimentStage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("实验id")
    private Long experimentId;

    @ApiModelProperty("阶段id")
    private Long algorithmStageId;

    @ApiModelProperty("阶段名称")
    private String stageName;

    @ApiModelProperty("阶段在实验中所处的先后顺序")
    private Integer stageOrder;

    @ApiModelProperty("实验资源配置id")
    private Long resourceId;

    @ApiModelProperty("实验资源值")
    private String resourceName;

    @ApiModelProperty("最大trail次数")
    private Integer maxTrialNum;

    @ApiModelProperty("trail并发数量")
    private Integer trialConcurrentNum;

    @ApiModelProperty("最大运行时间")
    private Double maxExecDuration;

    @ApiModelProperty("实验阶段状态")
    private Integer status;

    @ApiModelProperty("启动时间")
    private Timestamp startTime;

    @ApiModelProperty("每次开始运行的时间")
    private Timestamp beginTime;

    @ApiModelProperty("结束时间")
    private Timestamp endTime;

    @ApiModelProperty("最大运行时间单位\n" +
            "年（y）\n" +
            "月（m）\n" +
            "周（w）\n" +
            "日（d）\n" +
            "小时（h）\n" +
            "分钟（min）\n" +
            "秒（s）\n" +
            "毫秒（ms）\n" +
            "微秒（us）\n" +
            "纳秒（ns）\n" +
            "皮秒（ps）\n" +
            "飞秒（fs）")
    private String maxExecDurationUnit;

    @ApiModelProperty("暂停前已经运行的时间")
    private Long runTime;

    public ExperimentStage(ExperimentCreateDTO.Stage stage, Experiment experiment) {
        this.experimentId = experiment.getId();
        this.algorithmStageId = stage.getAlgorithmStageId();
        this.stageName = stage.getStageName();
        this.stageOrder = stage.getStageOrder();
        this.resourceId = stage.getResourceId();
        this.resourceName = stage.getResourceName();
        this.maxTrialNum = stage.getMaxTrialNum();
        this.trialConcurrentNum = stage.getTrialConcurrentNum();
        this.maxExecDuration = stage.getMaxExecDuration();
        this.status = ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode();
        this.maxExecDurationUnit = stage.getMaxExecDurationUnit();
        setCreateUserId(experiment.getCreateUserId());
    }

    public ExperimentStage(ExperimentUpdateDTO.Stage stage, Experiment experiment) {
        this.id=stage.getId();
        this.experimentId = experiment.getId();
        this.algorithmStageId = stage.getAlgorithmStageId();
        this.stageName = stage.getStageName();
        this.stageOrder = stage.getStageOrder();
        this.resourceId = stage.getResourceId();
        this.resourceName = stage.getResourceName();
        this.maxTrialNum = stage.getMaxTrialNum();
        this.trialConcurrentNum = stage.getTrialConcurrentNum();
        this.maxExecDuration = stage.getMaxExecDuration();
        this.status = ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode();
        this.maxExecDurationUnit = stage.getMaxExecDurationUnit();
        setCreateUserId(experiment.getCreateUserId());
    }

}
