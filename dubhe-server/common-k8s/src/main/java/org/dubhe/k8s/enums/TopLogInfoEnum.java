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

package org.dubhe.k8s.enums;

import lombok.Getter;
import org.dubhe.biz.base.utils.StringUtils;

/**
 * @description TopLogInfoEnum枚举类
 * @date 2020-06-30
 */
@Getter
public enum TopLogInfoEnum {

    /**
     * Succeeded
     */
    SUCCESSED("Succeeded","任务日志为空"),
    /**
     * Pending
     */
    PENDING("Pending","No log or log file has expired"),
    /**
     * Running
     */
    RUNNING("Running",""),
    /**
     * Failed
     */
    FAILED("Failed","No log or log file has expired"),
    /**
     * Unknown
     */
    UNKNOWN("Unknown","No log or log file has expired"),
    /**
     * NotExist
     */
    NOT_EXIST("NotExist","No log or log file has expired")

    ;
    private String status;
    private String info;

    TopLogInfoEnum(String status,String info) {
        this.status = status;
        this.info = info;
    }

    public static TopLogInfoEnum getTopLogInfoEnum(String status){
        for (TopLogInfoEnum topLogInfoEnum : TopLogInfoEnum.values()) {
            if (StringUtils.equals(status,topLogInfoEnum.getStatus())){
                return topLogInfoEnum;
            }
        }
        return NOT_EXIST;

    }
}
