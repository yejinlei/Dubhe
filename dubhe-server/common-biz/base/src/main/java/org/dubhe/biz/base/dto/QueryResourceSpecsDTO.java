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
package org.dubhe.biz.base.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.constant.MagicNumConstant;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 查询资源规格
 * @date 2021-06-02
 */
@Data
@Accessors(chain = true)
public class QueryResourceSpecsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格名称
     */
    @NotBlank(message = "规格名称不能为空")
    @Length(max = MagicNumConstant.THIRTY_TWO, message = "规格名称错误")
    private String specsName;

    /**
     * 所属业务场景(0:通用，1：dubhe-notebook，2：dubhe-train，3：dubhe-serving，4：dubhe-tadl)
     */
    @NotNull(message = "所属业务场景不能为空")
    @Min(value = MagicNumConstant.ZERO, message = "所属业务场景错误")
    @Max(value = MagicNumConstant.FOUR, message = "所属业务场景错误")
    private Integer module;
}