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
 * @description 规格类型
 * @date 2020-07-15
 */
public enum ResourcesPoolTypeEnum {

    CPU(0, "CPU"),
    GPU(1, "GPU");


    /**
     * 编码
     */
    private Integer code;

    /**
     * 描述
     */
    private String description;

    ResourcesPoolTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 是否是GPU编码
     * @param code
     * @return true 是 ，false 否
     */
    public static boolean isGpuCode(Integer code){
        return GPU.getCode().equals(code);
    }

}
