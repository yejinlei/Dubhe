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

/**
 * @description Trial状态事件常量
 * @date 2020-08-31
 */
public class TrialEventMachineConstant {

    /**
     * trial状态机
     */
    private TrialEventMachineConstant() {
    }
    /**
     * trial实验状态机
     */
    public static final String TRIAL_STATE_MACHINE="trialStateMachine";
    /**
     * trial 待运行状态事件
     */
    public static final String TO_RUN_TRIAL_EVENT ="toRunTrialEvent";
    /**
     * trial 批量待运行状态事件
     */
    public static final String TO_RUN_BATCH_TRIAL_EVENT ="toRunBatchTrialEvent";
    /**
     * trial 等待中状态事件
     */
    public static final String WAITING_TRIAL_EVENT="waitingTrialEvent";
    /**
     * trial运行状态事件
     */
    public static final String RUNNING_TRIAL_EVENT = "runningTrialEvent";
    /**
     *trial已完成事件
     */
    public static final String FINISHED_TRIAL_EVENT="finishedTrialEvent";
    /**
     * trial运行失败事件
     */
    public static final String FAILED_TRIAL_EVENT="failedTrialEvent";
    /**
     * trial未知事件
     */
    public static final String UNKNOWN_TRIAL_EVENT="unknownTrialEvent";

}