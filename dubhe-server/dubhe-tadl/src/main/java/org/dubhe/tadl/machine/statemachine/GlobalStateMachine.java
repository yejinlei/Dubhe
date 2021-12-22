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
package org.dubhe.tadl.machine.statemachine;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 全局状态机
 * @date 2020-08-27
 */
@Data
@Component
public class GlobalStateMachine {

    /**
     * 实验状态机
     */
    @Resource
    private ExperimentStateMachine experimentStateMachine;
    /**
     * 实验阶段状态机
     */
    @Resource
    private ExperimentStageStateMachine experimentStageStateMachine;
    /**
     * trial状态机
     */
    @Resource
    private TrialStateMachine trialStateMachine;

}
