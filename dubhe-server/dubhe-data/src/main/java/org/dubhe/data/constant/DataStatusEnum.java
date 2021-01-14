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
 * @description 数据(文件)状态(是否删除状态)
 * @date 2020-06-03
 */
@Getter
public enum DataStatusEnum {

    /**
     * 新增文件
     */
    ADD(0, " 新增"),
    /**
     * 已删除文件
     */
    DELETE(1, "删除"),

    /**
     * 正常文件
     */
    NORMAL(2, "正常"),
    ;

    DataStatusEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private int value;
    private String msg;

}
