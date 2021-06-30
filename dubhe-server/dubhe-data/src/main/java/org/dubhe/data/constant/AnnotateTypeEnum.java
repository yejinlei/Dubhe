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
 * @description 标注类型枚举类
 * @date 2020-05-21
 */
@Getter
public enum AnnotateTypeEnum {

    /**
     * 目标检测
     */
    OBJECT_DETECTION(1, "目标检测"),
    /**
     * 图像分类
     */
    CLASSIFICATION(2, "图像分类"),
    /**
     * 目标跟踪
     */
    OBJECT_TRACK(5, "目标跟踪"),
    /**
     * 文本分类
     */
    TEXT_CLASSIFICATION(6, "文本分类"),
    /**
     * 语义分割
     */
    SEMANTIC_CUP(7, "语义分割"),
    /**
     * 音频分类
     */
    AUDIO_CLASSIFY(8, "声音分类"),
    /**
     * 文本分词
     */
    TEXT_SEGMENTATION(9,"文本分词"),
    /**
     * 命名实体识别
     */
    NAMED_ENTITY_RECOGNITION(10,"命名实体识别"),
    /**
     * 语音识别
     */
    SPEECH_RECOGNITION(11, "语音识别"),
    /**
     * 自定义导入
     */
    AUTO_IMPORT(100, "自定义导入");


    AnnotateTypeEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    private Integer value;
    private String msg;

    /**
     * 标注类型校验 用户web端接口调用时参数校验
     *
     * @param value 标注类型Integer值
     * @return      参数校验结果
     */
    public static boolean isValid(Integer value) {
        for (AnnotateTypeEnum annotateTypeEnum : AnnotateTypeEnum.values()) {
            if (annotateTypeEnum.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据标注类型获取类型code值
     *
     * @param annotate 标注类型
     * @return         类型code值
     */
    public static Integer getConvertAnnotateType(String annotate) {
        for (AnnotateTypeEnum annotateTypeEnum : AnnotateTypeEnum.values()) {
            if (annotateTypeEnum.msg.equals(annotate)) {
                return annotateTypeEnum.value;
            }
        }
        return null;
    }

    /**
     * 获取错误提示信息
     *
     * @return 错误提示信息字符串
     */
    public static String getErrorMessage() {
        StringBuilder stringBuilder = new StringBuilder("数据标注类型参数不对, 请使用:");
        for (AnnotateTypeEnum annotateTypeEnum : AnnotateTypeEnum.values()) {
            stringBuilder.append(" ").append(annotateTypeEnum.getValue()).append("-")
                    .append(annotateTypeEnum.getMsg());
        }
        return stringBuilder.toString();
    }

}
