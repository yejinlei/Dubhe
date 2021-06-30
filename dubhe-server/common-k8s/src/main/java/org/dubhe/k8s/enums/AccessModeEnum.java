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
 * @description AccessModes contains the desired access modes the volume should have
 * @date 2020-06-19
 */
public enum AccessModeEnum {
    /**
     * 单路读写
     */
    READ_WRITE_ONCE("ReadWriteOnce"),
    /**
     * 多路只读
     */
    READ_ONLY_MANY("ReadOnlyMany"),
    /**
     * 多路读写
     */
    READ_WRITE_MANY("ReadWriteMany"),
    ;

    private String type;

    AccessModeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
