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
package org.dubhe.tadl.enums;

import lombok.Getter;

@Getter
public enum ModelTypeEnum {

    /**
     * 图像分类
     */
    IMAGE_CLASSIFY(101, "图像分类"),
    /**
     * 文本分类
     */
    TEXT_CLASSIFY(301, "文本分类");

    ModelTypeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    private Integer value;
    private String name;

    /**
     * 标注类型校验 用户web端接口调用时参数校验
     *
     * @param value 模型类型值
     * @return      参数校验结果
     */
    public static boolean isValid(Integer value) {
        for (ModelTypeEnum modelTypeEnum : ModelTypeEnum.values()) {
            if (modelTypeEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
