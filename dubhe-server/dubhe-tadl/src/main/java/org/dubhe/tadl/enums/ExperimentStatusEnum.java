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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description 归类实验状态
 * @date 2020-05-06
 */
@Getter
public enum ExperimentStatusEnum {
    /**
     * 全部
     */
    All(0, "全部", "all"),
    /**
     * 待运行
     */
    TO_RUN_EXPERIMENT_STATE(101, "待运行","toRunExperimentState"),
    /**
     * 等待中
     */
    WAITING_EXPERIMENT_STATE(102, "等待中","waitingExperimentState"),
    /**
     * 运行中
     */
    RUNNING_EXPERIMENT_STATE(103, "运行中","runningExperimentState"),
    /**
     * 已暂停
     */
    PAUSED_EXPERIMENT_STATE(104, "已暂停","pausedExperimentState"),
    /**
     * 已完成
     */
    FINISHED_EXPERIMENT_STATE(202, "已完成","finishedExperimentState"),
    /**
     * 运行失败
     */
    FAILED_EXPERIMENT_STATE(203, "运行失败","failedExperimentState");


    /**
     * 所有状态
     */
    static Set<Integer> ALL_STATUS = new HashSet<Integer>() {{
        add(ExperimentStatusEnum.All.getValue());
    }};

    /**
     * 待运行
     */
    static Set<Integer> TO_RUN = new HashSet<Integer>() {{
        add(ExperimentStatusEnum.TO_RUN_EXPERIMENT_STATE.getValue());
    }};
    /**
     * 等待中
     */
    static Set<Integer> WAITING = new HashSet<Integer>(){{
        add(ExperimentStatusEnum.WAITING_EXPERIMENT_STATE.getValue());
    }};
    /**
     * 运行中
     */
    static Set<Integer> RUNNING = new HashSet<Integer>(){{
        add(ExperimentStatusEnum.RUNNING_EXPERIMENT_STATE.getValue());
    }};
    /**
     * 已暂停
     */
    static Set<Integer> PAUSED = new HashSet<Integer>(){{
        add(ExperimentStatusEnum.PAUSED_EXPERIMENT_STATE.getValue());
    }};
    /**
     * 已完成
     */
    static Set<Integer> FINISHED = new HashSet<Integer>(){{
        add(ExperimentStatusEnum.FINISHED_EXPERIMENT_STATE.getValue());
    }};
    /**
     * 运行失败
     */
    static Set<Integer> FAILED = new HashSet<Integer>(){{
        add(ExperimentStatusEnum.FAILED_EXPERIMENT_STATE.getValue());
    }};


    private static final Map<Integer, Set<Integer>> EXPERIMENT_STATUS_MAP = new HashMap<Integer, Set<Integer>>() {{
        put(All.value, ALL_STATUS);
        put(TO_RUN_EXPERIMENT_STATE.value,TO_RUN);
        put(WAITING_EXPERIMENT_STATE.value,WAITING);
        put(RUNNING_EXPERIMENT_STATE.value,RUNNING);
        put(PAUSED_EXPERIMENT_STATE.value,PAUSED);
        put(FINISHED_EXPERIMENT_STATE.value,FINISHED);
        put(FAILED_EXPERIMENT_STATE.value,FAILED);

    }};

    ExperimentStatusEnum(int value, String msg, String stateMachine) {
        this.value = value;
        this.msg = msg;
        this.stateMachine = stateMachine;
    }

    /**
     * 状态值
     */
    private Integer value;
    /**
     * 状态描述
     */
    private String msg;
    /**
     * 状态机
     */
    private String stateMachine;

    /**
     * 获取指定实验状态下的实验状态列表
     *
     * @param status 实验状态类型
     * @return Set 符合条件的实验状态集合
     */
    public static Set<Integer> getStatus(Integer status) {
        return EXPERIMENT_STATUS_MAP.get(status);
    }

    /**
     * 根据状态值 获取 状态机
     *
     * @param value 状态值
     * @return String 状态描述
     */
    public static String getStateMachine(Integer value) {
        if (value != null) {
            for (ExperimentStatusEnum experimentStatusEnum : ExperimentStatusEnum.values()) {
                if (experimentStatusEnum.value.equals(value)) {
                    return experimentStatusEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 获取所有文件状态值
     *
     * @return  状态码集合
     */
    public static Set<Integer> getAllValue() {
        Set<Integer> allValues = new HashSet<>();
        for (ExperimentStatusEnum experimentStatusEnum : ExperimentStatusEnum.values()) {
            allValues.add(experimentStatusEnum.value);
        }
        return allValues;
    }

    /**
     * 根据CODE 获取 状态
     *
     * @param value 文件状态编码
     * @return String 文件状态
     */
    public static ExperimentStatusEnum getState(Integer value) {
        if (value != null) {
            for (ExperimentStatusEnum experimentStatusEnum : ExperimentStatusEnum.values()) {
                if (experimentStatusEnum.value.equals(value)) {
                    return experimentStatusEnum;
                }
            }
        }
        return null;
    }

}
