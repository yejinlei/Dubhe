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
package org.dubhe.tadl.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.base.annotation.EnumValue;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.tadl.enums.ModelTypeEnum;
import org.dubhe.tadl.enums.TimeUnitEnum;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 算法创建
 *
 * @date 2021-03-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel
public class ExperimentUpdateDTO {


    @ApiModelProperty("实验ID")
    @NotNull(message = "实验ID不能为空")
    private Long id;

    @ApiModelProperty("实验名称")
    @NotBlank(message = "实验名称不能为空")
    private String name;

    @ApiModelProperty("模型类别")
    @NotNull(message = "模型类别不能为空")
    @EnumValue(enumClass = ModelTypeEnum.class, enumMethod = "isValid")
    private Integer modelType;

    @ApiModelProperty("实验描述")
    private String description;

    @ApiModelProperty("阶段")
    private List<Stage> stage;

    @ApiModelProperty("算法ID")
    @NotNull(message = "算法ID不能为空")
    private Long algorithmId;

    @ApiModelProperty("算法版本ID")
    @NotNull(message = "算法版本ID不能为空")
    private Long algorithmVersionId;

    @ApiModelProperty("是否启动实验")
    private Boolean start;

    @ApiModelProperty("是否使用gpu")
    private Boolean gpu;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ApiModel
    public static class Stage {

        @ApiModelProperty("阶段ID")
        @NotNull(message = "阶段ID不能为空")
        private Long id;

        @ApiModelProperty("算法阶段ID")
        @NotNull(message = "算法阶段ID不能为空")
        private Long algorithmStageId;

        @ApiModelProperty("阶段排序")
        @NotNull(message = "阶段排序不能为空")
        private Integer stageOrder;

        @ApiModelProperty("阶段名称")
        @NotBlank(message = "阶段名称不能为空")
        private String stageName;

        @ApiModelProperty("实验资源配置ID")
        @NotNull(message = "实验资源配置ID不能为空")
        private Long resourceId;

        @ApiModelProperty("实验资源配置值")
        @NotBlank(message = "实验资源配置值不能为空")
        private String resourceName;

        @ApiModelProperty("最大 trial 数量")
        @NotNull(message = "最大 trial 数量不能为空")
        @Min(value = NumberConstant.NUMBER_1, message = "最大 trial 数量不能小于1")
        private Integer maxTrialNum;

        @ApiModelProperty("trial 并发数量")
        @NotNull(message = "trial 并发数量不能为空")
        @Min(value = NumberConstant.NUMBER_1, message = "并发数量不能小于1")
        private Integer trialConcurrentNum;

        @ApiModelProperty("是否支持多卡训练")
        @NotNull(message = "是否支持多卡训练不能为空")
        private Boolean multiGpu;

        @ApiModelProperty("最大执行时间")
        @NotNull(message = "最大执行时间不能为空")
        private Double maxExecDuration;

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
        @NotNull(message = "最大运行时间单位不能为空")
        @EnumValue(enumClass = TimeUnitEnum.class, enumMethod = "isValid")
        private String maxExecDurationUnit;

        @ApiModelProperty("yaml")
        @NotBlank(message = "yaml不能为空")
        private String yaml;
    }


}
