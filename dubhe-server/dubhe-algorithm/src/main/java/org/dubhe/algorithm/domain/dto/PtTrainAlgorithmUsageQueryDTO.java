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
import org.dubhe.biz.db.base.PageQueryBase;

import javax.validation.constraints.NotNull;

/**
 * @description 查询算法用途条件
 * @date 2020-06-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PtTrainAlgorithmUsageQueryDTO extends PageQueryBase {

    @ApiModelProperty(value = "类型", hidden = true)
    private String type;

    @ApiModelProperty(value = "是否包含默认值  0：不包含  1包含", required = true)
    @NotNull
    private Boolean isContainDefault;

}
