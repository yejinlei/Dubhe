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
import lombok.experimental.Accessors;;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.db.base.PageQueryBase;
import org.dubhe.train.utils.TrainUtil;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @description 任务参数查询条件
 * @date 2020-4-27
 */
@Data
@Accessors(chain = true)
public class PtTrainParamQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("任务参数名称,输入长度不能超过128个字符")
    @Length(max = MagicNumConstant.ONE_HUNDRED_TWENTY_EIGHT, message = "任务参数名称-输入长度不能超过128个字符")
    private String paramName;

    @ApiModelProperty(value = "规格类型(0为CPU, 1为GPU)")
    private Integer resourcesPoolType;

}
