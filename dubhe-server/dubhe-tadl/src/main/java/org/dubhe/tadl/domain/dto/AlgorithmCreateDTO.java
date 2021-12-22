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
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.tadl.enums.ModelTypeEnum;
import org.dubhe.tadl.enums.TimeUnitEnum;
import org.hibernate.validator.constraints.Length;

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
public class AlgorithmCreateDTO {

    @ApiModelProperty("算法名称")
    @NotBlank(message = "算法名称不能为空")
    private String name;

    @ApiModelProperty("模型类别")
    @NotNull(message = "模型类别不能为空")
    @EnumValue(enumClass = ModelTypeEnum.class, enumMethod = "isValid")
    private Integer modelType;

    @ApiModelProperty("算法描述")
    @NotBlank(message = "算法描述不能为空")
    private String description;

    @ApiModelProperty("默认指标")
    @NotBlank(message = "默认指标不能为空")
    private String defaultMetric;

    @ApiModelProperty("算法类型")
    @NotBlank(message = "算法类型不能为空")
    private String algType;

    @ApiModelProperty("框架名称")
    @NotBlank(message = "框架名称不能为空")
    private String platform;

    @ApiModelProperty("框架版本")
    @NotBlank(message = "框架版本不能为空")
    private String platformVersion;

    @ApiModelProperty("是否oneShot")
    @NotNull(message = "是否oneShot不能为空")
    private Boolean oneShot;

    @ApiModelProperty("是否支持gpu")
    @NotNull(message = "是否支持gpu不能为空")
    private Boolean gpu;

    @ApiModelProperty("阶段")
    private List<Stage> stage;

    @ApiModelProperty(value = "zip压缩包路径（路径规则：/algorithm-manage/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/用户上传的算法具体文件(zip文件）名称或从notebook跳转时为/notebook/{userId}/{YYYYMMDDhhmmssSSS+四位随机数}/）", required = true)
    @NotBlank(message = "zip压缩包路径不能为空")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "zip压缩包路径-输入长度不能超过128个字符")
    private String zipPath;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ApiModel
    public static class Stage {

        @ApiModelProperty("阶段排序")
        @NotNull(message = "阶段排序不能为空")
        private Integer stageOrder;

        @ApiModelProperty("数据集ID")
        @NotNull(message = "数据集ID不能为空")
        private Long datasetId;

        @ApiModelProperty("数据集版本")
        @NotBlank(message = "数据集版本不能为空")
        private String datasetVersion;

        @ApiModelProperty("数据集路径")
        @NotBlank(message = "数据集路径不能为空")
        private String datasetPath;

        @ApiModelProperty("数据集路径")
        @NotBlank(message = "数据集名称不能为空")
        private String datasetName;

        @ApiModelProperty("command命令所使用的python环境")
        @NotBlank(message = "command命令所使用的python环境不能为空")
        private String pythonVersion;

        @ApiModelProperty("command命令所使用py文件")
        @NotBlank(message = "command命令所使用py文件不能为空")
        private String executeScript;

        @ApiModelProperty("是否支持gpu训练：0支持，1不支持")
        @NotNull(message = "是否支持gpu训练不能为空")
        private Boolean multiGpu;

        @ApiModelProperty("最大 trial 数量")
        @NotNull(message = "最大 trial 数量不能为空")
        @Min(value = NumberConstant.NUMBER_1, message = "最大 trial 数量不能小于1")
        private Integer maxTrialNum;

        @ApiModelProperty("最大执行时间")
        @NotNull(message = "最大执行时间不能为空")
        private Double maxExecDuration;

        @ApiModelProperty("trial 并发数量")
        @NotNull(message = "trial 并发数量不能为空")
        @Min(value = NumberConstant.NUMBER_1, message = "并发数量不能小于1")
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
        @NotNull(message = "最大运行时间单位不能为空")
        @EnumValue(enumClass = TimeUnitEnum.class, enumMethod = "isValid")
        private String maxExecDurationUnit;

        @ApiModelProperty("yaml")
        @NotBlank(message = "yaml不能为空")
        private String yaml;
    }


}
