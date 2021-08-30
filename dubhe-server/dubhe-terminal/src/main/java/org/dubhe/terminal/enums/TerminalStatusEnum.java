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

/**
 * @description terminal 状态枚举
 * @date 2020-07-15
 */
public enum TerminalStatusEnum {
    FAILED(0, "异常"),

    SAVING(1, "保存中"),

    RUNNING(2, "运行中"),

    DELETED(3, "已停止");
    /**
     * 编码
     */
    private Integer code;
    /**
     * 描述
     */
    private String description;

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    TerminalStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(Integer code) {
        if (code != null) {
            for (TerminalStatusEnum en : TerminalStatusEnum.values()) {
                if (en.getCode().equals(code)) {
                    return en.getDescription();
                }
            }
        }
        return null;
    }
}
