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

package org.dubhe.datasetutil.common.enums;

import lombok.Getter;

/**
 * @description 数据类型
 * @date 2020-11-23
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
    TXT(2, "文本");

    DatatypeEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    /**
     * 数据类型
     */
    private Integer value;

    /**
     * 数据描述
     */
    private String msg;


}
