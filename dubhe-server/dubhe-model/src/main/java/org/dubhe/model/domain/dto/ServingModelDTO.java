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
package org.dubhe.model.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.utils.PtModelUtil;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 查询能提供服务的模型请求实体
 * @date 2021-04-06
 */
@Data
public class ServingModelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模型是否为预置模型（0默认模型，1预置模型, 2炼知模型）")
    @NotNull(message = "模型类型不能为空")
    @Min(value = PtModelUtil.NUMBER_ZERO, message = "模型来源错误")
    @Max(value = PtModelUtil.NUMBER_TWO, message = "模型来源错误")
    private Integer modelResource;
}