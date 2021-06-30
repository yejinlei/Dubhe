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

package org.dubhe.serving.enums;

import lombok.Getter;

/**
 * @description 在线服务状态枚举
 * @date 2020-08-26
 */
@Getter
public enum ServingStatusEnum {

    EXCEPTION("0", "运行失败"),

    IN_DEPLOYMENT("1", "部署中"),

    WORKING("2", "运行中"),

    STOP("3", "已停止"),

    COMPLETED("4", "已完成"),

    UNKNOWN("5", "未知");

    private String status;

    private String message;

    ServingStatusEnum(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
