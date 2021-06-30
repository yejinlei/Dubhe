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

package org.dubhe.train.enums;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 训练任务枚举类
 * @date 2020-04-27
 */
@Getter
public enum TrainJobStatusEnum {

    /**
     * PENDING
     */
    PENDING(0, "PENDING"),
    /**
     * RUNNING
     */
    RUNNING(1, "RUNNING"),
    /**
     * SUCCEEDED
     */
    SUCCEEDED(2, "SUCCEEDED"),
    /**
     * FAILED
     */
    FAILED(3, "FAILED"),
    /**
     * STOP
     */
    STOP(4, "STOP"),
    /**
     * UNKNOWN
     */
    UNKNOWN(5, "UNKNOWN"),
    /**
     * DELETED
     */
    DELETED(6, "DELETED"),

    /**
     * CREATE_FAILED
     */
    CREATE_FAILED(7, "CREATE_FAILED");

    private Integer status;

    private String message;

    TrainJobStatusEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * 根据信息获取枚举类对象
     *
     * @param msg 信息
     * @return 枚举类对象
     */
    public static TrainJobStatusEnum getByMessage(String msg) {
        for (TrainJobStatusEnum statusEnum : values()) {
            if (statusEnum.message.equalsIgnoreCase(msg)) {
                return statusEnum;
            }
        }
        return UNKNOWN;
    }

    /**
     * 回调状态转换  若是DELETED则转换为STOP，避免状态不统一
     * @param phase k8s pod phase
     * @return
     */
    public static TrainJobStatusEnum transferStatus(String phase) {
        TrainJobStatusEnum enums = getByMessage(phase);
        if (enums != DELETED) {
            return enums;
        }
        return STOP;
    }

    /**
     * 根据状态获取枚举类对象
     *
     * @param status 状态
     * @return 枚举类对象
     */
    public static TrainJobStatusEnum getByStatus(Integer status) {
        for (TrainJobStatusEnum statusEnum : values()) {
            if (statusEnum.status.equals(status)) {
                return statusEnum;
            }
        }
        return UNKNOWN;
    }


    /**
     * 结束状态枚举集合
     */
    public static final Set<TrainJobStatusEnum> END_TRAIN_JOB_STATUS;

    static {
        END_TRAIN_JOB_STATUS = new HashSet<>();
        END_TRAIN_JOB_STATUS.add(SUCCEEDED);
        END_TRAIN_JOB_STATUS.add(FAILED);
        END_TRAIN_JOB_STATUS.add(STOP);
        END_TRAIN_JOB_STATUS.add(CREATE_FAILED);
        END_TRAIN_JOB_STATUS.add(DELETED);
    }

    public static boolean isEnd(String msg) {
        return END_TRAIN_JOB_STATUS.contains(getByMessage(msg));
    }

    public static boolean isEnd(Integer status) {
        return END_TRAIN_JOB_STATUS.contains(getByStatus(status));
    }

    public static boolean checkStopStatus(Integer num) {
        return isEnd(num);
    }

    public static boolean checkRunStatus(Integer num) {
        return PENDING.getStatus().equals(num) ||
                RUNNING.getStatus().equals(num);
    }
}
