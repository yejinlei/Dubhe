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
import org.dubhe.biz.db.base.PageQueryBase;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 训练任务版本查询
 * @date 2020-06-30
 */
@Data
@Accessors(chain = true)
public class PtTrainJobVersionQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "训练作业id", required = true)
    @NotNull(message = "训练作业id不能为空")
    @Min(value = MagicNumConstant.ONE, message = "训练作业id必须不小于1")
    private Long trainId;

    @ApiModelProperty("训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知,6为删除，7为创建失败")
    private Integer trainStatus;

}