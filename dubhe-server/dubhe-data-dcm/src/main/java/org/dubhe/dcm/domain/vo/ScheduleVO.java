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
package org.dubhe.dcm.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 医学数据集状态VO
 * @date 2021-01-13
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ScheduleVO {

    @ApiModelProperty("未标注")
    private Integer unfinished;

    @ApiModelProperty("标注完成")
    private Integer finished;

    @ApiModelProperty("自动标注完成")
    private Integer autoFinished;

    @ApiModelProperty("标注中")
    private Integer manualAnnotating;
}
