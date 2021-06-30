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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 删除训练任务
 * @date 2020-04-27
 */
@Data
@Accessors(chain = true)
public class PtTrainJobDeleteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "trainId,如果只传递trainId,代表删除该trainId下的所有job", required = true)
    @NotNull(message = "trainId不能为空")
    @Min(value = MagicNumConstant.ONE, message = "trainId数值不合法")
    private Long trainId;

    @ApiModelProperty(value = "jobId")
    @Min(value = MagicNumConstant.ONE, message = "jobId数值不合法")
    private Long id;

}
