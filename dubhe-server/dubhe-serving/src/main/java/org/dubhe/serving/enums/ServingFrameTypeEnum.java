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
 * @description 模型部署框架枚举
 * @date 2020-09-15
 */
@Getter
public enum ServingFrameTypeEnum {

    ONE_FLOW(1, "oneflow"),

    TENSOR_FLOW(2, "tensorflow"),

    PY_TORCH(3, "pytorch"),

    KERAS(4, "keras"),
    ;
    /**
     * 框架类型
     */
    private Integer frameType;
    /**
     * 框架名称
     */
    private String frameName;

    ServingFrameTypeEnum(Integer frameType, String frameName) {
        this.frameType = frameType;
        this.frameName = frameName;
    }

    /**
     * 获取框架名称
     *
     * @param frameType 框架类型
     * @return
     */
    public static String getFrameName(Integer frameType) {
        ServingFrameTypeEnum[] frameTypeEnums = values();
        for (ServingFrameTypeEnum frameTypeEnum : frameTypeEnums) {
            if (frameTypeEnum.getFrameType().equals(frameType)) {
                return frameTypeEnum.getFrameName();
            }
        }
        return null;
    }
}
