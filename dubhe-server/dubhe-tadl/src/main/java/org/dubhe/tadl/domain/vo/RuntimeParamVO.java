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

import java.io.Serializable;

@Data
public class RuntimeParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("最大执行时间")
    private Double maxExecDuration;

    @ApiModelProperty("trial 并发数")
    private Integer trialConcurrentNum;

    @ApiModelProperty("最大 trial 数")
    private Integer maxTrialNum;

    @ApiModelProperty("已运行时间")
    private Long runTime;

    @ApiModelProperty("已执行 trial 数")
    private Integer trialNum;

    @ApiModelProperty("最大执行时间单位")
    private String maxExecDurationUnit;

}
