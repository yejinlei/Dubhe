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

package org.dubhe.domain.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.base.BaseImageDTO;
import org.dubhe.utils.PtModelUtil;
import org.dubhe.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @description 创建训练任务
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainJobCreateDTO extends BaseImageDTO {

    @ApiModelProperty(value = "训练作业名, 长度在1-32个字符", required = true)
    @NotNull(message = "训练作业名不能为空")
    @Length(min = TrainUtil.NUMBER_ONE, max = TrainUtil.NUMBER_THIRTY_TWO, message = "训练作业名长度在1-32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "训练作业名称支持字母、数字、汉字、英文横杠和下划线")
    private String trainName;

    @ApiModelProperty("描述, 长度不能超过255个字符")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "描述长度不能超过255个字符")
    private String description;

    @ApiModelProperty(value = "算法来源id", required = true)
    @NotNull(message = "algorithmId不能为空")
    @Min(value = TrainUtil.NUMBER_ONE, message = "algorithmId必须不小于1")
    private Long algorithmId;

    @ApiModelProperty(value = "运行命令,输入长度不能超过128个字符", required = true)
    @NotBlank(message = "运行命令不能为空")
    @Length(max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_EIGHT, message = "运行命令-输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty(value = "数据来源名称, 长度在1-127个字符", required = true)
    @NotNull(message = "数据来源名称不能为空")
    @Length(min = TrainUtil.NUMBER_ONE, max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_SEVEN, message = "数据来源名称长度在1-127个字符")
    private String dataSourceName;

    @ApiModelProperty(value = "数据来源路径, 长度在1-127个字符", required = true)
    @NotNull(message = "数据来源路径不能为空")
    @Length(min = TrainUtil.NUMBER_ONE, max = TrainUtil.NUMBER_ONE_HUNDRED_AND_TWENTY_SEVEN, message = "数据来源路径长度在1-127个字符")
    private String dataSourcePath;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)")
    private JSONObject runParams;

    @ApiModelProperty(value = "类型(0为CPU，1为GPU)", required = true)
    @Min(value = TrainUtil.NUMBER_ZERO, message = "类型错误")
    @Max(value = TrainUtil.NUMBER_ONE, message = "类型错误")
    @NotNull(message = "类型(0为CPU，1为GPU)不能为空")
    private Integer resourcesPoolType;

    @ApiModelProperty(value = "规格名称", required = true)
    @NotNull(message = "规格名称不能为空")
    private String trainJobSpecsName;

    @ApiModelProperty(value = "规格信息", required = true)
    @NotNull(message = "规格信息不能为空")
    private String trainJobSpecsInfo;

    @ApiModelProperty("true代表保存作业参数")
    private Boolean saveParams;

    @ApiModelProperty("作业参数名称，当saveParams为true的时候传递, 输入长度不能超过32个字符")
    @Length(max = TrainUtil.NUMBER_THIRTY_TWO, message = "作业参数名称-输入长度不能超过32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "作业参数名称支持字母、数字、汉字、英文横杠和下划线")
    private String trainParamName;

    @ApiModelProperty("作业参数描述，当saveParams为true的时候传递, 长度不能超过255个字符")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "作业参数描述-输入长度不能超过255个字符")
    private String trainParamDesc;

    @ApiModelProperty(value = "训练类型 0：普通训练，1：分布式训练", required = true)
    @Min(value = TrainUtil.NUMBER_ZERO, message = "训练类型错误")
    @Max(value = TrainUtil.NUMBER_ONE, message = "训练类型错误")
    @NotNull(message = "训练类型(0为普通训练，1为分布式训练)不能为空")
    private Integer trainType;

    @ApiModelProperty(value = "节点个数", required = true)
    @Min(value = TrainUtil.NUMBER_ONE, message = "节点个数至少1个")
    @NotNull(message = "节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty("验证数据来源名称")
    private String valDataSourceName;

    @ApiModelProperty("验证数据来源路径")
    private String valDataSourcePath;

    @ApiModelProperty("是否验证数据集")
    private Integer valType;

    @ApiModelProperty(value = "训练延时启动时长，单位为小时")
    @Min(value = TrainUtil.NUMBER_ZERO, message = "训练延时启动时长不能小于0小时")
    @Max(value = TrainUtil.NUMBER_ONE_HUNDRED_AND_SIXTY_EIGHT, message = "训练延时启动时长不能大于168小时即时长不能超过一周（7*24小时）")
    private Integer delayCreateTime;

    @ApiModelProperty(value = "训练自动停止时长，单位为小时")
    @Min(value = TrainUtil.NUMBER_ZERO, message = "训练自动停止时长不能小于0小时")
    @Max(value = TrainUtil.NUMBER_ONE_HUNDRED_AND_SIXTY_EIGHT, message = "训练自动停止时长不能大于168小时即时长不能超过一周（7*24小时）")
    private Integer delayDeleteTime;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;

    @ApiModelProperty(value = "训练信息(失败信息)")
    private String trainMsg;

    @ApiModelProperty(value = "模型类型(0我的模型1预置模型2炼知模型)" +
            "当值为0和1的时候，需要传递ModelId, 当值为2的时候传递teacherModelIds和studentModelIds")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型来源错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "模型来源错误")
    private Integer modelResource;

    @ApiModelProperty(value = "模型id")
    @Min(value = TrainUtil.NUMBER_ONE, message = "模型id必须大于1")
    private Long modelId;

    @ApiModelProperty(value = "我的模型版本对应的id")
    @Min(value = TrainUtil.NUMBER_ONE, message = "模型版本对应的id必须大于1")
    private Long modelBranchId;

    @ApiModelProperty(value = "教师模型ids,多个id之前用','隔开")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "教师模型长度不能超过255个字符")
    @Pattern(regexp = "^([1-9][0-9]*,)*[1-9][0-9]*$", message = "教师模型ids参数格式不正确")
    private String teacherModelIds;

    @ApiModelProperty(value = "学生模型ids,多个id之前用','隔开")
    @Length(max = TrainUtil.NUMBER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "学生模型长度不能超过255个字符")
    @Pattern(regexp = "^([1-9][0-9]*,)*[1-9][0-9]*$", message = "学生模型ids参数格式不正确")
    private String studentModelIds;
}
