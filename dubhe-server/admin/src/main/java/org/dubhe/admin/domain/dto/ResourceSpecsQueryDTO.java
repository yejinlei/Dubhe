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
package org.dubhe.admin.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.dubhe.biz.db.base.PageQueryBase;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * @description 查询资源规格
 * @date 2021-05-27
 */
@Data
@Accessors(chain = true)
public class ResourceSpecsQueryDTO extends PageQueryBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "多GPU，true：GPU数大于1核，false:GPU数等于1核")
    private Boolean multiGpu;

    @ApiModelProperty("规格名称")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "规格名称错误")
    private String specsName;

    @ApiModelProperty("规格类型(0为CPU, 1为GPU)")
    private Boolean resourcesPoolType;

    @ApiModelProperty("所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving,4：dubhe-tadl)")
    @Min(value = MagicNumConstant.ZERO, message = "所属业务场景错误")
    @Max(value = MagicNumConstant.FOUR, message = "所属业务场景错误")
    private Integer module;
}