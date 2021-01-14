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
 * @description 资源缺乏枚举类
 * @date 2020-06-22
 */
public enum LackOfResourcesEnum {
    /**
     * 资源充足
     */
    ADEQUATE(0, "资源充足"),
    /**
     * cpu不足
     */
    LACK_OF_CPU(1, "cpu不足"),
    /**
     * 内存不足
     */
    LACK_OF_MEM(2, "内存不足"),
    /**
     * gpu不足
     */
    LACK_OF_GPU(3, "gpu不足"),
    /**
     * 没有可调度节点
     */
    LACK_OF_NODE(4, "没有可调度节点"),
    ;

    LackOfResourcesEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
