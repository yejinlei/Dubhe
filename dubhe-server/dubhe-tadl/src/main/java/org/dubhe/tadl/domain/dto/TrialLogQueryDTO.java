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
package org.dubhe.tadl.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.base.BaseLogQuery;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description Trial日志 查询DTO
 * @date 2021-03-05
 */
@Data
@Accessors(chain = true)
public class TrialLogQueryDTO extends BaseLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练trial的id")
    @NotNull(message = "训练trial的id不能为空")
    private Integer trialId;

}
