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

/**
 * @description 垃圾回收状态
 * @date 2020-09-17
 */
public enum RecycleStatusEnum {

    PENDING(0, "待回收"),
    SUCCEEDED(1, "已回收"),
    FAILED(2, "回收失败");


    /**
     * 编码
     */
    private Integer code;

    /**
     * 描述
     */
    private String description;

    RecycleStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
