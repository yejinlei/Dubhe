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
import org.dubhe.biz.base.utils.PtModelUtil;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * @description 模型条件查询
 * @date 2020-12-17
 */
@Data
@Accessors
public class PtModelInfoConditionQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型是否为预置模型（0默认模型，1预置模型）
     *
     */
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型类型错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "模型类型错误")
    @NotNull(message = "modelResource不能为空")
    private Integer modelResource;

    /**
     * 模型id
     */
    @NotNull(message = "id不能为空")
    private Set<Long> ids;

}