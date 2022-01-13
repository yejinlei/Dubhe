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
package org.dubhe.tadl.machine.constant;

public class ExperimentStageEventMachineConstant {

    /**
     * 实验阶段状态机
     */
    private ExperimentStageEventMachineConstant() {
    }

    /**
     * 实验状态机
     */
    public static final String EXPERIMENT_STAGE_STATE_MACHINE = "experimentStageStateMachine";
    /**
     * 实验阶段运行中事件
     */
    public static final String RUNNING_EXPERIMENT_STAGE_EVENT = "runningExperimentStageEvent";
    /**
     * 实验阶段已完成事件
     */
    public static final String FINISHED_EXPERIMENT_STAGE_EVENT = "finishedExperimentStageEvent";
    /**
     * 实验阶段运行失败事件
     */
    public static final String FAILED_EXPERIMENT_STAGE_EVENT = "failedExperimentStageEvent";
    /**
     * 实验阶段待运行事件
     */
    public static final String TO_RUN_EXPERIMENT_STAGE_EVENT = "toRunExperimentStageEvent";
    /**
     * 实验阶段超时事件
     */
    public static final String TIMEOUT_EXPERIMENT_STAGE_EVENT="timeoutExperimentStageEvent";
    /**
     * 实验阶段批量待运行事件
     */
    public static final String TO_RUN_BATCH_EXPERIMENT_STAGE_EVENT = "toRunBatchExperimentStageEvent";


}
