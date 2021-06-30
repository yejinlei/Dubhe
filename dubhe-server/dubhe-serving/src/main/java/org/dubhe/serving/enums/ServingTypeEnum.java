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

package org.dubhe.serving.enums;

import lombok.Getter;

/**
 * @description 模型部署请求类型枚举
 * @date 2020-09-15
 */
@Getter
public enum ServingTypeEnum {

    HTTP(0, "HTTP"),

    GRPC(1, "gRPC"),
    ;
    /**
     * 请求类型
     */
    private Integer type;
    /**
     * 请求名称
     */
    private String name;

    ServingTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 获取请求名称
     *
     * @param type 请求类型
     * @return
     */
    public static String getName(Integer type) {
        ServingTypeEnum[] typeEnums = values();
        for (ServingTypeEnum typeEnum : typeEnums) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum.getName();
            }
        }
        return null;
    }
}
