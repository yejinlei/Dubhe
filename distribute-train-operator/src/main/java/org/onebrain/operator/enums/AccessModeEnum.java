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

package org.onebrain.operator.enums;

/**
 * @description  pvc的访问模式
 * @date 2020-09-24
 */
public enum AccessModeEnum {

    /**
     * RWO是最基本的方式，可读可写，但只支持被单个Pod挂载
     */
    RWO("ReadWriteOnce"),

    /**
     * 可以以只读的方式被多个Pod挂载
     */
    ROX("ReadOnlyMany"),

    /****/
    /**
     * 这种存储可以以读写的方式被多个Pod共享。
     * 不是每一种存储都支持这三种方式，像共享方式，目前支持的还比较少，比较常用的是NFS。
     * 在PVC绑定PV时通常根据两个条件来绑定，一个是存储的大小，另一个就是访问模式。
     */
    RWX("ReadWriteMany");

    /**
     * 模式
     */
    private final String mode;

    AccessModeEnum(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
