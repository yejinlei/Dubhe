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
 * @description 数据集类型
 * @date 2020-11-25
 */
@Getter
public enum DatasetTypeEnum {

    /**
     * 私有数据
     */
    PRIVATE(0, "私有数据"),
    /**
     * 团队数据
     */
    TEAM(1, "团队数据"),
    /**
     * 公开数据
     */
    PUBLIC(2, "公开数据");

    DatasetTypeEnum(Integer value, String msg) {
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
        for (DatasetTypeEnum datasetTypeEnum : DatasetTypeEnum.values()) {
            if (datasetTypeEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
