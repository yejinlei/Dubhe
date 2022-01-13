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
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.tadl.domain.entity.AlgorithmStage;
import org.dubhe.tadl.enums.StageEnum;


@Builder
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlgorithmStageVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("阶段名称")
    private String name;

    @ApiModelProperty("阶段排序")
    private Integer stageOrder;

    @ApiModelProperty("算法id")
    private Long algorithmId;

    @ApiModelProperty("算法版本id")
    private Long algorithmVersionId;

    @ApiModelProperty("数据集名称")
    private String datasetName;

    @ApiModelProperty("数据集id")
    private Long datasetId;

    @ApiModelProperty("数据集版本id")
    private String datasetVersion;

    @ApiModelProperty("数据集路径")
    private String datasetPath;

    @ApiModelProperty("command命令所使用的python环境")
    private String pythonVersion;

    @ApiModelProperty("command命令所使用py文件")
    private String executeScript;

    @ApiModelProperty("是否支持gpu训练：0支持，1不支持")
    private Boolean multiGpu;

    @ApiModelProperty("默认最大运行次数")
    private Integer maxTrialNum;

    @ApiModelProperty("当前阶段默认最大执行时间")
    private Double maxExecDuration;

    @ApiModelProperty("trial默认并发数量")
    private Integer trialConcurrentNum;

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

    public static AlgorithmStageVO from(AlgorithmStage algorithmStage) {
        return new AlgorithmStageVO() {{
            setId(algorithmStage.getId());
            setName(StageEnum.getStageName(algorithmStage.getStageOrder()));
            setStageOrder(algorithmStage.getStageOrder());
            setAlgorithmId(null);
            setAlgorithmVersionId(algorithmStage.getAlgorithmVersionId());
            setDatasetName(algorithmStage.getDatasetName());
            setDatasetId(algorithmStage.getDatasetId());
            setDatasetVersion(algorithmStage.getDatasetVersion());
            setDatasetPath(algorithmStage.getDatasetPath());
            setPythonVersion(algorithmStage.getPythonVersion());
            setExecuteScript(algorithmStage.getExecuteScript());
            setMultiGpu(algorithmStage.getMultiGpu().equals(NumberConstant.NUMBER_0));
            setMaxTrialNum(algorithmStage.getMaxTrialNum());
            setMaxExecDuration(algorithmStage.getMaxExecDuration());
            setTrialConcurrentNum(algorithmStage.getTrialConcurrentNum());
            setMaxExecDurationUnit(algorithmStage.getMaxExecDurationUnit());
        }};
    }

}
