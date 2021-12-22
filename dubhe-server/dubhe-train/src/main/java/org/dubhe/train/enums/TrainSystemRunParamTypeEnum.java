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

package org.dubhe.train.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * @description  训练系统参数类型枚举类
 * @date 2021-09-22
 */
public enum TrainSystemRunParamTypeEnum {
    /**
     * 普通系统参数
     */
    NORMAL("normal"),
    /**
     * 输出类型的系统参数
     */
    OUT("out"),

    /**
     * 文件挂载类型的系统参数
     */
    MOUNT("mount"),;

    @Getter
    @Setter
    private String type;

    TrainSystemRunParamTypeEnum(String type) {
        this.type = type;
    }
}
