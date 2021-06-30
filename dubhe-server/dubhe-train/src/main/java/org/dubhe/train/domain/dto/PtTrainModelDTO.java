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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.train.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description  训练使用模型
 * @date 2020-11-23
 */
@Data
@Accessors(chain = true)
public class PtTrainModelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("非炼知模型id")
    @Min(value = MagicNumConstant.ONE, message = "模型id必须大于1")
    private Long modelId;

    @ApiModelProperty(value = "我的模型版本对应的id")
    @Min(value = MagicNumConstant.ONE, message = "模型版本对应的id必须大于1")
    private Long modelBranchId;

    @ApiModelProperty("模型是否为预置模型（0默认模型，1预置模型, 2炼知模型" +
            "当值为0和1的时候，需要传递ModelId, 当值为2的时候传递teacherModelIds和studentModelIds")
    @NotNull(message = "模型类型不能为空")
    @Min(value = MagicNumConstant.ZERO, message = "模型来源错误")
    @Max(value = MagicNumConstant.TWO, message = "模型来源错误")
    private Integer modelResource;

    @ApiModelProperty(value = "教师模型ids,多个id之前用','隔开")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "教师模型长度不能超过255个字符")
    @Pattern(regexp = TrainUtil.REGEXP_IDS_STRING, message = "教师模型ids参数格式不正确")
    private String teacherModelIds;

    @ApiModelProperty(value = "学生模型ids,多个id之前用','隔开")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "学生模型长度不能超过255个字符")
    @Pattern(regexp = TrainUtil.REGEXP_IDS_STRING, message = "学生模型ids参数格式不正确")
    private String studentModelIds;
}
