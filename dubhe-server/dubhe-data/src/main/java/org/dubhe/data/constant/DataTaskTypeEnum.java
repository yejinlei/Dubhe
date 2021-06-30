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
 * @description  数据集任务类型
 * @date 2020-08-27
 */
@Getter
public enum DataTaskTypeEnum {

    /**
     * 自动标注
     */
    ANNOTATION(0, "自动标注"),
    /**
     * ofrecord格式转换
     */
    OFRECORD(1, "ofrecord格式转换"),
    /**
     * imageNet
     */
    IMAGE_NET(2, "imageNet"),
    /**
     * 数据增强
     */
    ENHANCE(3, "数据增强"),
    /**
     * 目标跟踪
     */
    TARGET_TRACK(4, "目标跟踪"),
    /**
     * 视频采样
     */
    VIDEO_SAMPLE(5, "视频采样"),
    /**
     * 医学标注
     */
    MEDICINE_ANNOTATION(6,"医学标注"),
    /**
     * 文本分类
     */
    TEXT_CLASSIFICATION(7, "文本分类"),
    /**
     * 重新自动标注
     */
    AGAIN_ANNOTATION(8, "重新自动标注"),
    /**
     * csv导入
     */
    CSV_IMPORT(10, "csv导入")
    ;

    DataTaskTypeEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private Integer value;
    private String msg;

}
