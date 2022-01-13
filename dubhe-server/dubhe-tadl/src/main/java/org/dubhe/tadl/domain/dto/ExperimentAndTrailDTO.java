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

import lombok.Data;

import java.util.List;

/**
 * @description 实验 Trail 消息体
 * @date 2021-03-04
 */
@Data
public class ExperimentAndTrailDTO {
    /**
     * 实验ID
     */
    private Long experimentId;
    /**
     *  阶段ID
     */
    private Long stageId;
    /**
     * 并发数量
     */
    private Integer trialConcurrentNum;
    /**
     * 最大运行时间
     */
    private Long executionMaxTime;
    /**
     *  Trail 数据
     */
    private List<TrialRunParamDTO> trialRunParamDTOList;
    /**
     * 创建用户id
     */
    private Long createUserId;

}
