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
 * @description 优化算法类型
 * @date 2021-01-06
 */
@Getter
public enum OptimizeTypeEnum {

    SLIMMING(0, "剪枝", "/ChannelSlimming"),

    DISTILL(1, "蒸馏", "/distil"),

    QUANTIFY(2, "量化", "/quantization"),
    ;

    /**
     * 类型值
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 代码目录
     */
    private String codeDir;

    OptimizeTypeEnum(Integer type, String name, String codeDir) {
        this.type = type;
        this.name = name;
        this.codeDir = codeDir;
    }


    public static String getCodeDirByType(Integer type) {
        for (OptimizeTypeEnum optimizeTypeEnum : OptimizeTypeEnum.values()) {
            if (optimizeTypeEnum.getType().equals(type)) {
                return optimizeTypeEnum.getCodeDir();
            }
        }
        return "";
    }
}
