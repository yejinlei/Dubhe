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

package org.dubhe.algorithm.enums;

import lombok.Getter;

/**
 * @description 算法枚举类
 * @date 2020-05-12
 */
@Getter
public enum AlgorithmSourceEnum {

    /**
     * MINE 算法来源  我的算法
     */
    MINE(1, "MINE"),
    /**
     * PRE  算法来源  预置算法
     */
    PRE(2,"PRE");

    private Integer status;

    private String message;

    AlgorithmSourceEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
