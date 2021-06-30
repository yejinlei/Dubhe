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

package org.dubhe.model.domain.enums;

import lombok.Getter;

/**
 * @description 模型类型
 * @date 2021-01-06
 */
@Getter
public enum ModelResourceEnum {

    /**
     * 我的模型
     */
    MINE(0),

    /**
     * 预置模型
     */
    PRE(1),

    /**
     * 炼知模型
     */
    ATLAS(2);

    private Integer code;

    ModelResourceEnum(Integer code) {
        this.code = code;
    }

    public static ModelResourceEnum get(Integer code) {
        for (ModelResourceEnum m : values()) {
            if (m.getCode().equals(code)) {
                return m;
            }
        }
        return MINE;
    }
}
