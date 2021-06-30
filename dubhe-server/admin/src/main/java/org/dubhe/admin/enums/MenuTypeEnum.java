/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.admin.enums;

/**
 * @description 菜单类型
 * @date 2020-06-01
 */
public enum MenuTypeEnum {

    /**
     * DIR_TYPE 目录
     */
    DIR_TYPE(0, "目录"),

    /**
     * PAGE_TYPE 页面
     */
    PAGE_TYPE(1, "页面"),

    /**
     * ACTION_TYPE 操作(权限)
     */
    ACTION_TYPE(2, "操作"),

    /**
     * LINK_TYPE 外链
     */
    LINK_TYPE(3, "外链"),

    /**
     * OTHER_TYPE 其他
     */
    OTHER_TYPE(-1, "其他"),

    ;

    private Integer value;

    private String desc;

    MenuTypeEnum(Integer value, String desc) {
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

    public static MenuTypeEnum getEnumValue(Integer value) {
        switch (value) {
            case 0:
                return DIR_TYPE;
            case 1:
                return PAGE_TYPE;
            case 2:
                return ACTION_TYPE;
            case 3:
                return LINK_TYPE;
            default:
                return OTHER_TYPE;
        }
    }

    public static boolean isExist(Integer value) {
        for (MenuTypeEnum itm : MenuTypeEnum.values()) {
            if (value.compareTo(itm.getValue())==0) {
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
