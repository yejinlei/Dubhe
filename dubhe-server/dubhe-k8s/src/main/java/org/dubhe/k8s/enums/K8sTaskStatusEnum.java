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

/**
 * @description k8s_task 状态枚举类
 * @date 2020-09-01
 */
public enum  K8sTaskStatusEnum {
    /**
     * 成功
     */
    NONE(0, "无需操作"),
    UNEXECUTED(1, "待执行"),
    EXECUTED(2, "已完成"),
    ;

    private Integer status;
    private String message;
    K8sTaskStatusEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }



    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
