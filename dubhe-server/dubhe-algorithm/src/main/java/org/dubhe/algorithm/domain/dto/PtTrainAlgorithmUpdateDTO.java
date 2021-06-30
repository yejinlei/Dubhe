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

package org.dubhe.algorithm.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.algorithm.utils.TrainUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @description 修改算法
 * @date 2020-06-19
 */
@Data
@Accessors(chain = true)
public class PtTrainAlgorithmUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id", required = true)
    @NotNull(message = "id不能为null")
    @Min(value = TrainUtil.NUMBER_ONE, message = "id必须大于1")
    private Long id;

    @ApiModelProperty(value = "算法名称，输入长度不能超过32个字符")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "算法名称-输入长度不能超过32个字符")
    @Pattern(regexp = TrainUtil.REGEXP, message = "算法名称支持字母、数字、汉字、英文横杠和下划线")
    private String algorithmName;

    @ApiModelProperty("算法描述，输入长度不能超过256个字符")
    @Length(max = MagicNumConstant.INTEGER_TWO_HUNDRED_AND_FIFTY_FIVE, message = "算法描述-输入长度不能超过256个字符")
    private String description;

    @ApiModelProperty("算法用途，输入长度不能超过128个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "算法用途-输入长度不能超过128个字符")
    private String algorithmUsage;

    @ApiModelProperty("是否输出训练信息")
    private Boolean isTrainOut;

    @ApiModelProperty("是否输出可视化日志")
    private Boolean isVisualizedLog;

}
