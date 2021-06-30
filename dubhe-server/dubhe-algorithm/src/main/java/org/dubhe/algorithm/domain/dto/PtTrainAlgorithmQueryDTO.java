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
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.algorithm.utils.TrainUtil;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * @description 查询算法条件
 * @date 2020-04-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainAlgorithmQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "算法来源(1为我的算法, 2为预置算法)")
    @Min(value = TrainUtil.NUMBER_ONE, message = "算法来源错误")
    @Max(value = TrainUtil.NUMBER_TWO, message = "算法来源错误")
    @Query(propName = "algorithm_source", type = Query.Type.EQ)
    private Integer algorithmSource;

    @ApiModelProperty(value = "算法名称或者id")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "算法名称或者id有误")
    private String algorithmName;

    @ApiModelProperty(value = "算法用途")
    private String algorithmUsage;

    @ApiModelProperty(value = "上传算法是否支持推理(true:可推理，false：不可推理)")
    private Boolean inference;
}
