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

package org.dubhe.data.constant;

import lombok.Getter;

/**
 * @description 任务状态
 * @date 2020-04-10
 */
@Getter
public enum TaskStatusEnum {

    /**
     * 未处理
     */
    INIT(0, "未处理"),
    /**
     * 进行中
     */
    ING(1, "进行中"),
    /**
     * 已完成
     */
    FINISHED(2, "已完成"),
    /**
     * 失败
     */
    FAIL(3, "失败"),
    ;

    TaskStatusEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private int value;
    private String msg;

}
