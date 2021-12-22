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
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.tadl.domain.dto.AlgorithmCreateDTO;
import org.dubhe.tadl.domain.dto.AlgorithmUpdateDTO;
import org.dubhe.tadl.enums.StageEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Builder
@ToString
@Data
@TableName("tadl_algorithm_stage")
@ApiModel(value = "AlgorithmStage 对象", description = "算法阶段表")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlgorithmStage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
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
    private Integer multiGpu;

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


    public static List<AlgorithmStage> getCreateAlgorithmStageList(List<AlgorithmCreateDTO.Stage> stage,Long algorithmId,Long algorithmVersionId) {
        return new ArrayList<AlgorithmStage>(){{
            stage.forEach(v->{
                AlgorithmStage algorithmStage = new AlgorithmStage(
                        null,
                        StageEnum.getStageName(v.getStageOrder()),
                        v.getStageOrder(),
                        algorithmId,
                        algorithmVersionId,
                        v.getDatasetName(),
                        v.getDatasetId(),
                        v.getDatasetVersion(),
                        v.getDatasetPath(),
                        v.getPythonVersion(),
                        v.getExecuteScript(),
                        v.getMultiGpu()? NumberConstant.NUMBER_0:NumberConstant.NUMBER_1,
                        v.getMaxTrialNum(),
                        v.getMaxExecDuration(),
                        v.getTrialConcurrentNum(),
                        v.getMaxExecDurationUnit()
                );
                add(algorithmStage);
            });
        }};
    }

    public static List<AlgorithmStage> getUpdateAlgorithmStageList(List<AlgorithmUpdateDTO.Stage> stage,Long algorithmId,Long algorithmVersionId) {
        return new ArrayList<AlgorithmStage>() {{
            stage.forEach(v -> {
                AlgorithmStage algorithmStage = new AlgorithmStage(
                        v.getId(),
                        StageEnum.getStageName(v.getStageOrder()),
                        v.getStageOrder(),
                        algorithmId,
                        algorithmVersionId,
                        v.getDatasetName(),
                        v.getDatasetId(),
                        v.getDatasetVersion(),
                        v.getDatasetPath(),
                        v.getPythonVersion(),
                        v.getExecuteScript(),
                        v.getMultiGpu() ? NumberConstant.NUMBER_0 : NumberConstant.NUMBER_1,
                        v.getMaxTrialNum(),
                        v.getMaxExecDuration(),
                        v.getTrialConcurrentNum(),
                        v.getMaxExecDurationUnit()
                );
                add(algorithmStage);
            });
        }};
    }
}
