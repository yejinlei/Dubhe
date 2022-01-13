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

/**
 * @description 时间单位枚举
 * @date 2020-04-10
 */
@Getter
public enum TimeUnitEnum {

    /**
     *
     */
    DAY("day", "日"),
    HOUR("hour", "小时"),
    MIN("min", "分钟");

    TimeUnitEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    private String value;
    private String name;


    /**
     * 获取时间枚举
     *
     * @param unit val 值
     * @return 阶段
     */
    public static TimeUnitEnum getTimeUnit(String unit) {
        for (TimeUnitEnum unitEnum : TimeUnitEnum.values()) {
            if (unitEnum.value.equals(unit)) {
                return unitEnum;
            }
        }
        return null;
    }

    /**
     * 标注类型校验 用户web端接口调用时参数校验
     *
     * @param value 时间单位
     * @return      参数校验结果
     */
    public static boolean isValid(String value) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取时间单位
     *
     * @param name 时间单位名称
     * @return 时间单位
     */
    public static String getValue(String name) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.name.equals(name)) {
                return timeUnitEnum.value;
            }
        }
        return null;
    }

    /**
     * 获取时间单位名称
     *
     * @param value 时间单位
     * @return 阶段名称
     */
    public static String getName(String value) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.value.equals(value)) {
                return timeUnitEnum.name;
            }
        }
        return null;
    }

}