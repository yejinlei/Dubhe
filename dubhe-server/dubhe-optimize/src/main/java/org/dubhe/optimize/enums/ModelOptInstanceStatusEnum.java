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

package org.dubhe.optimize.enums;

import lombok.Getter;

/**
 * @description 模型优化常量
 * @date 2020-05-22
 */
@Getter
public enum ModelOptInstanceStatusEnum {

    /**
     * 模型优化任务状态
     */
    WAITING("-1", "等待中"),
    RUNNING("0", "进行中"),
    COMPLETED("1", "已完成"),
    CANCELED("2", "已取消"),
    EXEC_FAILED("3", "执行失败"),

    ;
    private String value;
    private String msg;

    ModelOptInstanceStatusEnum(String value, String msg) {
        this.value = value;
        this.msg = msg;
    }
}
