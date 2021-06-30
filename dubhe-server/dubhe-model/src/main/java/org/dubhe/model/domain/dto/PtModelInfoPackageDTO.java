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

package org.dubhe.model.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.utils.PtModelUtil;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @description 打包炼知模型DTO
 * @date 2020-03-24
 */
@Data
@Accessors(chain = true)
public class PtModelInfoPackageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模型ID")
    @NotNull(message = "模型ID不能为空")
    @Min(value = PtModelUtil.NUMBER_ONE, message = "模型ID不能小于1")
    private Long id;

    @ApiModelProperty("入口函数")
    @NotBlank(message = "入口函数不能为空")
    private String entryName;

    @ApiModelProperty("模型描述")
    private String readme;

    @ApiModelProperty("模型名称")
    @NotBlank(message = "模型名称不能为空")
    private String name;

    @ApiModelProperty("数据集名称")
    @NotBlank(message = "数据集名称不能为空")
    private String dataset;

    @ApiModelProperty("任务名称")
    @NotBlank(message = "任务名称不能为空")
    private String task;

    @ApiModelProperty("模型地址")
    @NotBlank(message = "模型地址不能为空")
    private String url;

    @ApiModelProperty("图像尺寸")
    @NotNull(message = "图像尺寸不能为空")
    private Integer size;

    @ApiModelProperty("均一化范围")
    @NotNull(message = "均一化范围不能为空")
    private List<Double> range;

    @ApiModelProperty("色彩空间")
    @NotBlank(message = "色彩空间不能为空")
    private String space;

    @ApiModelProperty("均值")
    @NotNull(message = "均值不能为空")
    private List<Double> mean;

    @ApiModelProperty("方差")
    @NotNull(message = "方差不能为空")
    private List<Double> std;

    @ApiModelProperty("预训练模型标识")
    @NotNull(message = "预训练模型标识不能为空")
    private Boolean entryPretrained;

    @ApiModelProperty("分类数量")
    @NotNull(message = "分类数量不能为空")
    private Integer entryNumClasses;

    @ApiModelProperty("其他分类数量")
    @NotNull(message = "其他分类数量不能为空")
    private Integer otherNumClasses;
}