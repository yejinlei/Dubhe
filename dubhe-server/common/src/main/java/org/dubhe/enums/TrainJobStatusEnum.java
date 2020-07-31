/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

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

    public static TrainJobStatusEnum get(String msg) {
        for (TrainJobStatusEnum statusEnum : values()) {
            if (statusEnum.message.equalsIgnoreCase(msg)) {
                return statusEnum;
            }
        }
        return UNKNOWN;
    }

    public static boolean isEnd(String msg) {
        List<String> endList = Arrays.asList("SUCCEEDED", "FAILED", "STOP", "CREATE_FAILED");
        return endList.stream().anyMatch(s -> s.equalsIgnoreCase(msg));
    }

    public static boolean isEnd(Integer num) {
        List<Integer> endList = Arrays.asList(2, 3, 4, 7);
        return endList.stream().anyMatch(s -> s.equals(num));
    }

    public static boolean checkStopStatus(Integer num) {
        return SUCCEEDED.getStatus().equals(num) ||
                FAILED.getStatus().equals(num) ||
                STOP.getStatus().equals(num) ||
                CREATE_FAILED.getStatus().equals(num) ||
                DELETED.getStatus().equals(num);
    }
}
