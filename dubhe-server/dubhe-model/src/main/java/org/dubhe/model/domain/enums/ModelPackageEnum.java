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
 * @description 模型打包状态
 * @date 2021-01-06
 */
@Getter
public enum ModelPackageEnum implements ModelEnumInterface{

    /**
     * 未打包
     */
    UN_PACKAGE(0),

    /**
     * 已打包
     */
    PACKAGED(1);

    private Integer code;


    ModelPackageEnum(Integer code) {
        this.code = code;
    }

    /**
     * 校验是否为有效的code
     * @param code     code码
     */
    public static boolean isValid(Integer code) {
        for (ModelPackageEnum m : values()) {
            if(m.getCode().equals(code)){
                return true;
            }
        }
        return false;
    }
}
