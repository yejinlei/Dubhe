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
 * @description 数据集转换状态
 * @date 2020-06-17
 */
@Getter
public enum ConversionStatusEnum {

    /**
     * 转换状态：未复制
     */
    NOT_COPY(0, "未复制"),
    /**
     * 转换状态:未转换
     */
    NOT_CONVERSION(1, "未转换"),
    /**
     * 转换状态：已转换
     */
    IS_CONVERSION(2, "已转换"),

    /**
     * 转换状态：无法转换
     */
    UNABLE_CONVERSION(3, "无法转换"),

    /**
     * 转换状态：发布中
     */
    PUBLISHING(4,"发布中");

    ConversionStatusEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private Integer value;
    private String msg;

    /**
     * 数据转换类型校验 用户web端接口调用时参数校验
     *
     * @param value 数据转换类型
     * @return      参数校验结果
     */
    public static boolean isValid(Integer value) {
        for (ConversionStatusEnum conversionStatusEnum : ConversionStatusEnum.values()) {
            if (conversionStatusEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
