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
package org.dubhe.tadl.enums;

import lombok.Getter;

/**
 * @description 数据集状态类
 * @date 2020-08-28
 */
@Getter
public enum ExperimentStageStateEnum {


    /**
     * 待运行
     */
    TO_RUN_EXPERIMENT_STAGE_STATE(101, "toRunExperimentStageState","待运行"),
    /**
     * 运行中
     */
    RUNNING_EXPERIMENT_STAGE_STATE(102, "runningExperimentStageState","运行中"),
    /**
     * 已完成
     */
    FINISHED_EXPERIMENT_STAGE_STATE(201, "finishedExperimentStageState","已完成"),
    /**
     * 运行失败
     */
    FAILED_EXPERIMENT_STAGE_STATE(202, "failedExperimentStageState","运行失败");

    /**
     * 编码
     */
    private Integer code;
    /**
     * 状态机
     */
    private String stateMachine;
    /**
     * 描述
     */
    private String description;


    ExperimentStageStateEnum(Integer code, String stateMachine , String description) {
        this.code = code;
        this.stateMachine = stateMachine;
        this.description = description;
    }

    /**
     * 根据CODE 获取 DESCRIPTION
     *
     * @param code 数据集状态编码
     * @return String
     */
    public static String getStateMachine(Integer code) {
        if (code != null) {
            for (ExperimentStageStateEnum experimentStageStateEnum : ExperimentStageStateEnum.values()) {
                if (experimentStageStateEnum.getCode().equals(code)) {
                    return experimentStageStateEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 根据CODE 获取 DataStateEnum
     *
     * @param code 数据集状态编码
     * @return String
     */
    public static ExperimentStageStateEnum getState(Integer code) {
        if (code != null) {
            for (ExperimentStageStateEnum experimentStageStateEnum : ExperimentStageStateEnum.values()) {
                if (experimentStageStateEnum.getCode().equals(code)) {
                    return experimentStageStateEnum;
                }
            }
        }
        return null;
    }

}
