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
package org.dubhe.data.machine.enums;

import org.dubhe.data.machine.constant.FileStateCodeConstant;

import java.util.HashSet;
import java.util.Set;

/**
 * @description 文件状态枚举类
 * @date 2020-08-28
 */
public enum FileStateEnum {

    /**
     * 未标注
     */
    NOT_ANNOTATION_FILE_STATE(FileStateCodeConstant.NOT_ANNOTATION_FILE_STATE, "notAnnotationFileState","未标注"),
    /**
     * 手动标注中
     */
    MANUAL_ANNOTATION_FILE_STATE(FileStateCodeConstant.MANUAL_ANNOTATION_FILE_STATE, "manualAnnotationFileState","手动标注中"),
    /**
     * 自动标注完成
     */
    AUTO_TAG_COMPLETE_FILE_STATE(FileStateCodeConstant.AUTO_TAG_COMPLETE_FILE_STATE, "autoTagCompleteFileState","自动标注完成"),
    /**
     * 标注完成
     */
    ANNOTATION_COMPLETE_FILE_STATE(FileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE, "annotationCompleteFileState","标注完成"),
    /**
     * 标注未识别
     */
    ANNOTATION_NOT_DISTINGUISH_FILE_STATE(FileStateCodeConstant.ANNOTATION_NOT_DISTINGUISH_FILE_STATE, "annotationNotDistinguishFileState","标注未识别"),
    /**
     * 目标跟踪完成
     */
    TARGET_COMPLETE_FILE_STATE(FileStateCodeConstant.TARGET_COMPLETE_FILE_STATE, "targetCompleteFileState","目标跟踪完成");
    /**
     * 编码
     */
    private Integer code;
    /**
     * 状态机
     */
    private String stateMachine;
    /**
     * 描述
     */
    private String description;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(String stateMachine) {
        this.stateMachine = stateMachine;
    }

    FileStateEnum(Integer code, String stateMachine , String description) {
        this.code = code;
        this.stateMachine = stateMachine;
        this.description = description;
    }

    /**
     * 根据CODE 获取 DESCRIPTION
     *
     * @param code 文件状态编码
     * @return String 文件状态描述
     */
    public static String getStateMachine(Integer code) {
        if (code != null) {
            for (FileStateEnum fileStateEnum : FileStateEnum.values()) {
                if (fileStateEnum.getCode().equals(code)) {
                    return fileStateEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 获取所有文件状态值
     *
     * @return  状态码集合
     */
    public static Set<Integer> getAllValue() {
        Set<Integer> allValues = new HashSet<>();
        for (FileStateEnum fileStatusEnum : FileStateEnum.values()) {
            allValues.add(fileStatusEnum.code);
        }
        return allValues;
    }

    /**
     * 根据CODE 获取 状态
     *
     * @param code 文件状态编码
     * @return String 文件状态
     */
    public static FileStateEnum getState(Integer code) {
        if (code != null) {
            for (FileStateEnum fileStateEnum : FileStateEnum.values()) {
                if (fileStateEnum.getCode().equals(code)) {
                    return fileStateEnum;
                }
            }
        }
        return null;
    }

}
