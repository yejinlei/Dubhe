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
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.enums.ExperimentStageStateEnum;
import org.dubhe.tadl.enums.TadlErrorEnum;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExperimentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("实验名称")
    private String name;

    @ApiModelProperty("实验描述")
    private String description;

    @ApiModelProperty("算法id")
    private Long algorithmId;

    @ApiModelProperty("算法版本id")
    private Long algorithmVersionId;

    @ApiModelProperty("算法名称")
    private String algorithmName;

    @ApiModelProperty("算法版本")
    private String algorithmVersion;

    @ApiModelProperty("模型类型")
    private Integer modelType;

    @ApiModelProperty("实验状态：（待运行：101 等待中：102，运行中：103，已暂停：104已终止：105，已完成：106，运行失败：107）")
    private Integer status;

    @ApiModelProperty("启动时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("运行时间")
    private Long runTime;

    @ApiModelProperty("删除标识")
    private Boolean deleted;

    @ApiModelProperty("创建人id")
    private Long createUserId;

    @ApiModelProperty("更新人id")
    private Long updateUserId;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("创建人")
    private String createUser;

    @ApiModelProperty("更新人")
    private String updateUser;

    @ApiModelProperty("阶段状态")
    private List<ExperimentStageVO> stage;

    @ApiModelProperty("当前运行阶段")
    private Integer runStage;

    @ApiModelProperty("所有trial中的最佳精度")
    private Double bestAccuracy;

    @ApiModelProperty("最佳精度对应的trial")
    private Integer bestTrialSequence;

    @ApiModelProperty("best_checkpoint路径")
    private String bestCheckpointPath;

    public static ExperimentVO from(Experiment experiment) {
        return new ExperimentVO(){{
            setId(experiment.getId());
            setName(experiment.getName());
            setAlgorithmId(experiment.getAlgorithmId());
            setAlgorithmVersionId(experiment.getAlgorithmVersionId());
            setDescription(experiment.getDescription());
            setEndTime(experiment.getEndTime());
            setStartTime(experiment.getStartTime());
            setModelType(experiment.getModelType());
            setStatus(experiment.getStatus());
            setDeleted(experiment.getDeleted());
            setCreateTime(experiment.getCreateTime());
            setUpdateTime(experiment.getUpdateTime());
            setCreateUserId(experiment.getCreateUserId());
            setUpdateUserId(experiment.getUpdateUserId());
        }};
    }

    public static ExperimentVO from(Experiment experiment,String algorithmName,String algorithmVersion) {
        ExperimentVO experimentVO = from(experiment);
        experimentVO.setAlgorithmName(algorithmName);
        experimentVO.setAlgorithmVersion(algorithmVersion);
        return experimentVO;
    }

    @SuppressWarnings("all")
    public void setRunStage() {
        HashMap<Integer, Integer> map = new HashMap<>();
        stage.forEach(s -> {
            ExperimentStageStateEnum stageStateEnum = Objects.requireNonNull(ExperimentStageStateEnum.getState(s.getStatus()));
            switch (stageStateEnum) {
                case FAILED_EXPERIMENT_STAGE_STATE:
                    if (ObjectUtils.isEmpty(map.get(stageStateEnum.getCode()))||map.get(stageStateEnum.getCode())>s.getStageOrder()) {
                        map.put(stageStateEnum.getCode(),s.getStageOrder());
                    }
                    break;
                case RUNNING_EXPERIMENT_STAGE_STATE:
                    if (ObjectUtils.isEmpty(map.get(stageStateEnum.getCode()))||map.get(stageStateEnum.getCode())>s.getStageOrder()) {
                        map.put(stageStateEnum.getCode(),s.getStageOrder());
                    }
                    break;
                case FINISHED_EXPERIMENT_STAGE_STATE:
                    if (ObjectUtils.isEmpty(map.get(stageStateEnum.getCode()))||map.get(stageStateEnum.getCode())<s.getStageOrder()) {
                        map.put(stageStateEnum.getCode(),s.getStageOrder());
                    }
                    break;
                case TO_RUN_EXPERIMENT_STAGE_STATE:
                    if (ObjectUtils.isEmpty(map.get(stageStateEnum.getCode()))||map.get(stageStateEnum.getCode())>s.getStageOrder()) {
                        map.put(stageStateEnum.getCode(),s.getStageOrder());
                    }
                    break;
                default:
                    throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
            }
        });
        if (map.containsKey(ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode())){
            this.runStage=map.get(ExperimentStageStateEnum.FAILED_EXPERIMENT_STAGE_STATE.getCode());
        }else if (map.containsKey(ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode())){
            this.runStage=map.get(ExperimentStageStateEnum.RUNNING_EXPERIMENT_STAGE_STATE.getCode());
        }else if (map.containsKey(ExperimentStageStateEnum.FINISHED_EXPERIMENT_STAGE_STATE.getCode())){
            this.runStage=map.get(ExperimentStageStateEnum.FINISHED_EXPERIMENT_STAGE_STATE.getCode());
        }else if (map.containsKey(ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode())){
            this.runStage=map.get(ExperimentStageStateEnum.TO_RUN_EXPERIMENT_STAGE_STATE.getCode());
        }else {
            throw new BusinessException(TadlErrorEnum.PARAM_ERROR);
        }
    }
}
