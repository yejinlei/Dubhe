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
package org.dubhe.dcm.machine.enums;


import org.dubhe.dcm.machine.constant.DcmFileStateCodeConstant;

import java.util.HashSet;
import java.util.Set;


/**
 * @description 文件状态枚举类
 * @date 2020-08-28
 */
public enum DcmFileStateEnum {

    /**
     * 未标注
     */
    NOT_ANNOTATION_FILE_STATE(DcmFileStateCodeConstant.NOT_ANNOTATION_FILE_STATE, "notAnnotationDcmFileState","未标注"),
    /**
     * 标注中
     */
    ANNOTATION_FILE_STATE(DcmFileStateCodeConstant.ANNOTATION_FILE_STATE, "annotationFileState","标注中"),
    /**
     * 自动标注完成
     */
    AUTO_ANNOTATION_COMPLETE_FILE_STATE(DcmFileStateCodeConstant.AUTO_ANNOTATION_COMPLETE_FILE_STATE, "autoAnnotationCompleteDcmFileState","自动标注完成"),
    /**
     * 标注完成
     */
    ANNOTATION_COMPLETE_FILE_STATE(DcmFileStateCodeConstant.ANNOTATION_COMPLETE_FILE_STATE, "annotationCompleteDcmFileState","标注完成");
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

    DcmFileStateEnum(Integer code, String stateMachine , String description) {
        this.code = code;
        this.stateMachine = stateMachine;
        this.description = description;
    }

    /**
     * 根据CODE 获取 DESCRIPTION
     *
     * @param code 文件状态编码
     * @return String
     */
    public static String getStateMachine(Integer code) {
        if (code != null) {
            for (DcmFileStateEnum dcmFileStateEnum : DcmFileStateEnum.values()) {
                if (dcmFileStateEnum.getCode().equals(code)) {
                    return dcmFileStateEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 获取所有文件状态值
     *
     * @return
     */
    public static Set<Integer> getAllValue() {
        Set<Integer> allValues = new HashSet<>();
        for (DcmFileStateEnum fileStatusEnum : DcmFileStateEnum.values()) {
            allValues.add(fileStatusEnum.code);
        }
        return allValues;
    }

    /**
     * 根据CODE 获取 状态
     *
     * @param code 文件状态编码
     * @return String
     */
    public static DcmFileStateEnum getState(Integer code) {
        if (code != null) {
            for (DcmFileStateEnum dcmFileStateEnum : DcmFileStateEnum.values()) {
                if (dcmFileStateEnum.getCode().equals(code)) {
                    return dcmFileStateEnum;
                }
            }
        }
        return null;
    }

}
