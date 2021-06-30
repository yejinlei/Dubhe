/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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
package org.dubhe.model.domain.enums;

import lombok.Getter;
import org.dubhe.biz.base.utils.StringUtils;

/**
 * @description 模型转换响应枚举
 * @date 2021-5-28
 */
@Getter
public enum ModelConvertEnum {
    /**
     * MODEL_PATH_ERROR
     */
    MODEL_PATH_ERROR(501, "模型路径错误"),
    /**
     * OUTPUT_PATH_ERROR
     */
    OUTPUT_PATH_ERROR(502, "模型输出路径错误"),
    /**
     * NOT_EXIST_ERROR
     */
    NOT_EXIST_ERROR(503, "SaveModel模型不存在"),
    /**
     * MODEL_CONVERT_ERROR
     */
    MODEL_CONVERT_ERROR(504, "模型转换失败"),
    /**
     * CONVERT_SERVER_ERROR
     */
    CONVERT_SERVER_ERROR(505,"模型转换服务异常")
    ;

    ModelConvertEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;

    public static ModelConvertEnum getModelConvertEnum(int code){
        for (ModelConvertEnum modelConvertEnum : ModelConvertEnum.values()) {
            if (modelConvertEnum.getCode() == code){
                return modelConvertEnum;
            }
        }
        return CONVERT_SERVER_ERROR;
    }
}
