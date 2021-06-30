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

/**
 * @description 是否开关枚举
 * @date 2020-06-01
 */
public enum SwitchEnum {
    /**
     * OFF 否
     */
    OFF(0, "否"),

    /**
     * ON 否
     */
    ON(1, "是"),

    ;

    private Integer value;

    private String desc;

    SwitchEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static SwitchEnum getEnumValue(Integer value) {
        switch (value) {
            case 0:
                return OFF;
            case 1:
                return ON;
            default:
                return OFF;
        }
    }

    public static Boolean getBooleanValue(Integer value) {
        switch (value) {
            case 1:
                return true;
            case 0:
                return false;
            default:
                return false;
        }
    }

    public static boolean isExist(Integer value) {
        for (SwitchEnum itm : SwitchEnum.values()) {
            if (value.compareTo(itm.getValue()) == 0) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "[" + this.value + "]" + this.desc;
    }

}
