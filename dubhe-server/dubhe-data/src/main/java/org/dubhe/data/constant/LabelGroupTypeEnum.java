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
 * @description 标签组数据类型
 * @date 2020-12-11
 */
@Getter
public enum LabelGroupTypeEnum {

    /**
     * 视觉
     */
    VISUAL(0, "视觉"),
    /**
     * 文本
     */
    TXT(1, "文本"),
    /**
     * 表格
     */
    TABLE(2, "表格"),
    /**
     * 音频
     */
    AUDIO(4, "音频");

    LabelGroupTypeEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private Integer value;
    private String msg;


    /**
     * 标签组类型转换
     *
     * @param datatypeEnum 数据类型
     * @return 标签组类型
     */
    public static LabelGroupTypeEnum convertGroup(DatatypeEnum datatypeEnum){
        LabelGroupTypeEnum labelGroupTypeEnum;
        switch (datatypeEnum){
            case TEXT:
                labelGroupTypeEnum = LabelGroupTypeEnum.TXT;
                break;
            case TABLE:
                labelGroupTypeEnum = LabelGroupTypeEnum.TABLE;
                break;
            case AUDIO:
                labelGroupTypeEnum = LabelGroupTypeEnum.AUDIO;
                break;
            default:
                labelGroupTypeEnum = LabelGroupTypeEnum.VISUAL;
                break;
        }
        return labelGroupTypeEnum;
    }

}
