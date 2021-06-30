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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

/**
 * @description 可视化训练查询请求实体
 * @date 2021-04-19
 */
@Data
@Accessors(chain = true)
public class VisualTrainQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练作业名称")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "训练作业名称错误")
    private String trainName;

    @ApiModelProperty("训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知，6为删除，7为创建失败")
    @Min(value = MagicNumConstant.ZERO, message = "trainStatus错误")
    @Max(value = MagicNumConstant.SEVEN, message = "trainStatus错误")
    private Integer trainStatus;

    @ApiModelProperty("创建时间(毫秒值)")
    private List<Long> createTime;
}