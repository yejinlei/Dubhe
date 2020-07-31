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

package org.dubhe.data.constant;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 文件状态
 * @date 2020-04-10
 */
@Getter
public enum FileStatusEnum {
    /**
     * 未标注
     */
    INIT(0, "未标注"),
    /**
     * 标注中
     */
    ANNOTATING(1, "标注中"),
    /**
     * 自动标注完成
     */
    AUTO_ANNOTATION(2, "自动标注完成"),
    /**
     * 已标注完成
     */
    FINISHED(3, "标注完成"),
    /**
     * 目标追踪完成
     */
    FINISH_AUTO_TRACK(4, "目标追踪完成"),
    ;

    FileStatusEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private int value;
    private String msg;

    /**
     * 获取所有文件状态值
     *
     * @return
     */
    public static Set<Integer> getAllValue() {
        Set<Integer> allValues = new HashSet<>();
        for (FileStatusEnum fileStatusEnum : FileStatusEnum.values()) {
            allValues.add(fileStatusEnum.value);
        }
        return allValues;
    }

}
