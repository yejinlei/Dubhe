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

package org.dubhe.terminal.enums;


import org.dubhe.k8s.enums.PodPhaseEnum;


/**
 * @description terminal 状态枚举
 * @date 2020-07-15
 */
public enum TerminalInfoStatusEnum {
    FAILED(0, "异常",PodPhaseEnum.FAILED.getPhase()),

    UNKNOWN(0, "异常",PodPhaseEnum.UNKNOWN.getPhase()),

    PENDING(1, "调度中",PodPhaseEnum.PENDING.getPhase()),

    RUNNING(2, "运行中",PodPhaseEnum.RUNNING.getPhase()),

    DELETED(3, "已停止",PodPhaseEnum.DELETED.getPhase()),

    SUCCEEDED(3, "已停止",PodPhaseEnum.SUCCEEDED.getPhase());
    /**
     * 编码
     */
    private Integer code;
    /**
     * 描述
     */
    private String description;
    /**
     * k8s pod状态
     */
    private String phase;

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getPhase(){
        return phase;
    }

    TerminalInfoStatusEnum(int code, String description, String phase) {
        this.code = code;
        this.description = description;
        this.phase = phase;
    }

    public static String getDescription(Integer code) {
        if (code != null) {
            for (TerminalInfoStatusEnum en : TerminalInfoStatusEnum.values()) {
                if (en.getCode().equals(code)) {
                    return en.getDescription();
                }
            }
        }
        return null;
    }

    public static Integer getCode(String phase) {
        if (phase != null) {
            for (TerminalInfoStatusEnum en : TerminalInfoStatusEnum.values()) {
                if (en.getPhase().equals(phase)) {
                    return en.getCode();
                }
            }
        }
        return null;
    }
}
