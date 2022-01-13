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

import java.util.HashSet;
import java.util.Set;


@Getter
public enum TrialStatusEnum {

    /**
     *
     */
    TO_RUN(101, "待运行", "toRunTrialState"),

    WAITING(102, "等待中", "waitingTrialState"),

    RUNNING(103, "运行中", "runningTrialState"),

    FINISHED(201, "已完成", "finishedTrialState"),

    FAILED(202, "运行失败", "failedTrialState"),

    UNKNOWN(203, "未知", "unknownTrialState");

    /**
     * 状态值
     */
    private Integer val;
    /**
     * 状态信息
     */
    private String msg;
    /**
     * 状态机
     */
    private String stateMachine;

    TrialStatusEnum(Integer val, String msg, String stateMachine) {
        this.val = val;
        this.msg = msg;
        this.stateMachine = stateMachine;
    }

    /**
     * 获取 trial 状态
     *
     * @param val 状态
     * @return 状态枚举
     */
    public static TrialStatusEnum getStage(Integer val) {
        for (TrialStatusEnum trialStatusEnum : TrialStatusEnum.values()) {
            if (trialStatusEnum.val.equals(val)) {
                return trialStatusEnum;
            }
        }
        return null;
    }

    /**
     * 根据val 获取 stateMachine
     *
     * @param val 状态值
     * @return String 状态机
     */
    public static String getStateMachine(Integer val) {
        if (val != null) {
            for (TrialStatusEnum trialStatusEnum : TrialStatusEnum.values()) {
                if (trialStatusEnum.val.equals(val)) {
                    return trialStatusEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 获取状态值
     *
     * @return  状态码集合
     */
    public static Set<Integer> getAllValue() {
        Set<Integer> allValues = new HashSet<>();
        for (TrialStatusEnum trialStatusEnum : TrialStatusEnum.values()) {
            allValues.add(trialStatusEnum.val);
        }
        return allValues;
    }
}
