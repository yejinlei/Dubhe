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

package org.dubhe.data.constant;

import lombok.Getter;

/**
 * @description 数据类型
 * @date 2020-05-21
 */
@Getter
public enum DatatypeEnum {

    /**
     * 图片
     */
    IMAGE(0, "图片"),
    /**
     * 视频
     */
    VIDEO(1, "视频"),
    /**
     * 文本
     */
    TEXT(2, "文本"),
    /**
     * 表格
     */
    TABLE(3, "表格"),
    /**
     * 音频
     */
    AUDIO(4, "语音"),
    /**
     * 自定义导入
     */
    AUTO_IMPORT(100, "自定义导入");

    DatatypeEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private Integer value;
    private String msg;

    /**
     * 数据类型校验 用户web端接口调用时参数校验
     *
     * @param value 数据类型
     * @return      参数校验结果
     */
    public static boolean isValid(Integer value) {
        for (DatatypeEnum datatypeEnum : DatatypeEnum.values()) {
            if (datatypeEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取数据类型枚举
     *
     * @param value 获取数据类型枚举值
     * @return  数据类型枚举
     */
    public static DatatypeEnum getEnumValue(Integer value) {
        switch (value) {
            case 0:
                return IMAGE;
            case 1:
                return VIDEO;
            case 2:
                return TEXT;
            case 3:
                return TABLE;
            case 4:
                return AUDIO;
            default:
                return IMAGE;
        }
    }

    /**
     * 获取错误提示信息
     *
     * @return 错误提示信息字符串
     */
    public static String getErrorMessage() {
        StringBuilder stringBuilder = new StringBuilder("数据类型参数不对,请使用:");
        for (DatatypeEnum datatypeEnum : DatatypeEnum.values()) {
            stringBuilder.append(" ").append(datatypeEnum.getValue()).append("-").append(datatypeEnum.getMsg());
        }
        return stringBuilder.toString();
    }

}
