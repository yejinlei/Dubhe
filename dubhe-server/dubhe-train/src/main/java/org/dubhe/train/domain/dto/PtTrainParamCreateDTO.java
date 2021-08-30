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

package org.dubhe.train.domain.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.train.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

/**
 * @description 任务参数创建条件
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainParamCreateDTO extends BaseImageDTO {

    @ApiModelProperty(value = "任务参数名称,输入长度不能超过32个字符", required = true)
    @NotBlank(message = "任务参数名称不能为空")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "任务参数名称-输入长度不能超过32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "任务参数名称支持字母、数字、汉字、英文横杠和下划线")
    private String paramName;

    @ApiModelProperty("描述，输入长度不能超过256个字符")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "任务参数描述-输入长度不能超过256个字符")
    private String description;

    @ApiModelProperty(value = "算法id,算法id不能小于1", required = true)
    @NotNull(message = "算法id不能为空")
    @Min(value = MagicNumConstant.ONE, message = "算法id不能小于1")
    private Long algorithmId;

    @ApiModelProperty("算法用途，输入长度不能超过128个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "算法用途-输入长度不能超过128个字符")
    private String algorithmUsage;

    @ApiModelProperty("验证数据集算法用途，输入长度不能超过128个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "验证数据集算法用途-输入长度不能超过128个字符")
    private String valAlgorithmUsage;

    @ApiModelProperty(value = "运行命令,输入长度不能超过128个字符", required = true)
    @NotBlank(message = "运行命令不能为空")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "运行命令-输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty(value = "数据集来源路径,输入长度不能超过127个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_SEVEN, message = "数据集来源路径-输入长度不能超过127个字符")
    private String dataSourcePath;

    @ApiModelProperty(value = "数据集来源名称,输入长度不能超过127个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_SEVEN, message = "数据集来源名称-输入长度不能超过127个字符")
    private String dataSourceName;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)")
    private JSONObject runParams;

    @ApiModelProperty(value = "类型(0为CPU，1为GPU)", required = true)
    @Min(value = MagicNumConstant.ZERO, message = "类型错误")
    @Max(value = MagicNumConstant.ONE, message = "类型错误")
    @NotNull(message = "类型(0为CPU，1为GPU)不能为空")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "规格名称", required = true)
    @NotNull(message = "规格名称不能为空")
    private String trainJobSpecsName;

    @ApiModelProperty("验证数据来源名称")
    private String valDataSourceName;

    @ApiModelProperty("验证数据来源路径")
    private String valDataSourcePath;

    @ApiModelProperty("是否验证数据集")
    private Integer valType;

    @ApiModelProperty(value = "训练类型 0：普通训练，1：分布式训练", required = true)
    @Min(value = MagicNumConstant.ZERO, message = "训练类型错误")
    @Max(value = MagicNumConstant.ONE, message = "训练类型错误")
    @NotNull(message = "训练类型(0为普通训练，1为分布式训练)不能为空")
    private Integer trainType;

    @ApiModelProperty(value = "节点个数", required = true)
    @Min(value = MagicNumConstant.ONE, message = "节点个数在1~8之间")
    @Max(value = MagicNumConstant.EIGHT, message = "节点个数在1~8之间")
    @NotNull(message = "节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty(value = "模型类型(0我的模型1预置模型2炼知模型)" +
            "当值为0和1的时候，需要传递ModelId, 当值为2的时候传递teacherModelIds和studentModelIds")
    @Min(value = MagicNumConstant.ZERO, message = "训练类型错误")
    @Max(value = MagicNumConstant.TWO, message = "训练类型错误")
    private Integer modelResource;

    @ApiModelProperty(value = "模型id")
    @Min(value = MagicNumConstant.ONE, message = "id必须大于1")
    private Long modelId;

    @ApiModelProperty(value = "我的模型版本对应的id")
    @Min(value = MagicNumConstant.ONE, message = "模型版本对应的id必须大于1")
    private Long modelBranchId;

    @ApiModelProperty(value = "教师模型ids,多个id之前用','隔开")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "教师模型长度不能超过255个字符")
    @Pattern(regexp = TrainUtil.REGEXP_IDS_STRING, message = "教师模型ids参数格式不正确")
    private String teacherModelIds;

    @ApiModelProperty(value = "学生模型ids,多个id之前用','隔开")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "学生模型长度不能超过255个字符")
    @Pattern(regexp = TrainUtil.REGEXP_IDS_STRING, message = "学生模型ids参数格式不正确")
    private String studentModelIds;

}
