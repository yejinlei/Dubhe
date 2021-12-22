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
 * @description 状态机事件常量
 * @date 2020-08-27
 */
public class ExperimentEventMachineConstant {

    /**
     * 实验状态机
     */
    private ExperimentEventMachineConstant() {
    }

    /**
     * 实验状态机
     */
    public static final String EXPERIMENT_STATE_MACHINE="experimentStateMachine";
    /**
     * 实验运行事件
     */
    public static final String RUNNING_EXPERIMENT_EVENT="runningExperimentEvent";
    /**
     * 实验暂停事件
     */
    public static final String PAUSED_EXPERIMENT_EVENT ="pausedExperimentEvent";
    /**
     * 实验完成事件
     */
    public static final String FINISHED_EXPERIMENT_EVENT="finishedExperimentEvent";
    /**
     * 实验运行失败事件
     */
    public static final String FAILED_EXPERIMENT_EVENT="failedExperimentEvent";
    /**
     * 实验重启事件
     */
    public static final String WAITING_EXPERIMENT_EVENT ="waitingExperimentEvent";
    /**
     * 删除实验事件
     */
    public static final String DELETE_EXPERIMENT_INFO_EVENT ="deleteExperimentInfoEvent";
}