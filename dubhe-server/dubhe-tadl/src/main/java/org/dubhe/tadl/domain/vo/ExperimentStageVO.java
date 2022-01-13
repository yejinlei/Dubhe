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
import lombok.Data;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.domain.entity.ExperimentStage;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ExperimentStageVO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
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

    @ApiModelProperty("yaml")
    private String yaml;

    @ApiModelProperty("datasetId")
    private Long datasetId;

    @ApiModelProperty("datasetName")
    private String datasetName;

    @ApiModelProperty("datasetVersion")
    private String datasetVersion;

    @ApiModelProperty("multiGpu")
    private Boolean multiGpu;

    public static ExperimentStageVO from(ExperimentStage experimentStage){
        return new ExperimentStageVO(){{
            setId(experimentStage.getId());
            setStageName(experimentStage.getStageName());
            setStageOrder(experimentStage.getStageOrder());
            setStatus(experimentStage.getStatus());
        }};
    }

    public static ExperimentStageVO from(ExperimentStage experimentStage, String yaml, AlgorithmStage algorithmStage){
        ExperimentStageVO experimentStageVO = from(experimentStage);
        experimentStageVO.setExperimentId(experimentStage.getExperimentId());
        experimentStageVO.setAlgorithmStageId(experimentStage.getAlgorithmStageId());
        experimentStageVO.setResourceId(experimentStage.getResourceId());
        experimentStageVO.setResourceName(experimentStage.getResourceName());
        experimentStageVO.setMaxTrialNum(experimentStage.getMaxTrialNum());
        experimentStageVO.setTrialConcurrentNum(experimentStage.getTrialConcurrentNum());
        experimentStageVO.setMaxExecDuration(experimentStage.getMaxExecDuration());
        experimentStageVO.setStartTime(experimentStage.getStartTime());
        experimentStageVO.setEndTime(experimentStage.getEndTime());
        experimentStageVO.setMaxExecDurationUnit(experimentStage.getMaxExecDurationUnit());
        experimentStageVO.setYaml(yaml);
        experimentStageVO.setDatasetId(algorithmStage.getDatasetId());
        experimentStageVO.setDatasetName(algorithmStage.getDatasetName());
        experimentStageVO.setDatasetVersion(algorithmStage.getDatasetVersion());
        experimentStageVO.setMultiGpu(algorithmStage.getMultiGpu().equals(NumberConstant.NUMBER_0));
        return experimentStageVO;
    }

}