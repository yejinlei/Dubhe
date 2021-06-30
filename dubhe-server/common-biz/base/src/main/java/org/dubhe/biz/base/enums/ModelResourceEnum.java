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

package org.dubhe.biz.base.enums;

import lombok.Getter;

/**
 * @description 模型资源枚举类
 * @date 2020-11-19
 */
@Getter
public enum ModelResourceEnum {

    /**
     * 我的模型
     */
    MINE(0, "我的模型"),
    /**
     * 预置模型
     */
    PRESET(1, "预置模型"),
    /**
     * 炼知模型
     */
    ATLAS(2, "炼知模型");

    private Integer type;

    private String description;

    ModelResourceEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * 根据类型获取枚举类对象
     *
     * @param type 类型
     * @return 枚举类对象
     */
    public static ModelResourceEnum getType(Integer type) {
        for (ModelResourceEnum modelResourceEnum : values()) {
            if (modelResourceEnum.getType().compareTo(type) == 0) {
                //获取指定的枚举
                return modelResourceEnum;
            }
        }
        return null;
    }
}