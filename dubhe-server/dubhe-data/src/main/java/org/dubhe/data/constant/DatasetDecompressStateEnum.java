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
 * @description 数据集解压状态枚举类
 * @date 2020-07-28
 */
@Getter
public enum DatasetDecompressStateEnum {

    /**
     * 未解压
     */
    NOT_DECOMPRESSED(0, "未解压"),
    /**
     * 解压中
     */
    DECOMPRESSING(1, "解压中"),
    /**
     * 解压完成
     */
    DECOMPRESS_COMPLETE(2, "解压完成"),
    /**
     * 解压失败
     */
    DECOMPRESS_FAIL(3, "解压失败")
    ;

    private Integer value;
    private String msg;

    DatasetDecompressStateEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

}
