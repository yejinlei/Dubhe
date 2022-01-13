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
package org.dubhe.tadl.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.tadl.domain.bo.IntermediateAccuracy;

import java.util.List;

@Data
@Accessors(chain = true)
public class TadlTrialAccuracyVO {

    @ApiModelProperty("trial名称")
    private String trialName;

    @ApiModelProperty("横坐标名称")
    private String abscissaName;

    @ApiModelProperty("trial中间精度集合")
    private List<IntermediateAccuracy> intermediateAccuracyList;
}

