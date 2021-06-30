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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 训练日志 查询DTO
 * @date 2020-05-08
 */
@Data
@Accessors(chain = true)
public class PtTrainLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练job的id")
    @NotNull(message = "训练job的id不能为空")
    private Integer jobId;

    @ApiModelProperty(value = "起始行")
    @Min(value = MagicNumConstant.ONE, message = "startLine不能小于1")
    private Integer startLine;

    @ApiModelProperty(value = "行数")
    @Min(value = MagicNumConstant.ONE, message = "lines不能小于1")
    @Max(value = MagicNumConstant.ONE_THOUSAND, message = "lines不能大于1000")
    private Integer lines;

}
