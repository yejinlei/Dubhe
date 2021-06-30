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
 * @description 操作类型枚举
 * @date 2020-11-25
 */
@Getter
public enum OperationTypeEnum {
    /**
     * SELECT 查询类型
     */
    SELECT("select", "查询"),

    /**
     * UPDATE 修改类型
     */
    UPDATE("update", "修改"),

    /**
     * DELETE 删除类型
     */
    DELETE("delete", "删除"),

    /**
     * LIMIT 禁止操作类型
     */
    LIMIT("limit", "禁止操作"),

    /**
     * INSERT 新增类型
     */
    INSERT("insert", "新增类型"),

    ;

    /**
     * 操作类型值
     */
    private  String type;

    /**
     * 操作类型备注
     */
    private  String desc;

    OperationTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
