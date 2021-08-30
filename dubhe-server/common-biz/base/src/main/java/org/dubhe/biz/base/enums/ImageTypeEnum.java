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

import java.util.HashMap;
import java.util.Map;

/**
 * @description 镜像项目枚举类
 * @date 2020-12-11
 */
@Getter
public enum ImageTypeEnum {

    /**
     * notebook镜像
     */
    NOTEBOOK("notebook镜像", "notebook", 0),

    /**
     * 训练镜像
     */
    TRAIN("训练镜像", "train", 1),

    /**
     * Serving镜像
     */
    SERVING("Serving镜像", "serving", 2),

    /**
     * terminal镜像
     */
    TERMINAL("terminal镜像", "terminal", 3)
    ;

    /**
     * 镜像项目名称
     */
    private String name;
    /**
     * 镜像项目代码
     */
    private String code;
    /**
     * 镜像项目类型
     */
    private Integer type;

    ImageTypeEnum(String name, String code, Integer type) {
        this.name = name;
        this.code = code;
        this.type = type;
    }

    private static final Map<Integer, ImageTypeEnum> RESOURCE_ENUM_MAP = new HashMap<Integer, ImageTypeEnum>() {
        {
            for (ImageTypeEnum enums : ImageTypeEnum.values()) {
                put(enums.getType(), enums);
            }
        }
    };

    /**
     * 根据type获取ImageTypeEnum
     * @param type
     * @return 镜像项目枚举对象
     */
    public static ImageTypeEnum getType(int type) {
        return RESOURCE_ENUM_MAP.get(type);
    }

    /**
     * 根据type获取code
     *
     * @param type  镜像项目类型
     * @return String 镜像项目代码
     */
    public static String getType(Integer type) {
        for (ImageTypeEnum typeEnum : ImageTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum.getCode();
            }
        }
        return null;
    }
}
