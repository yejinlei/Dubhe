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

import org.dubhe.dcm.machine.constant.DcmDataStateCodeConstant;

/**
 * @description 数据集状态类
 * @date 2020-08-28
 */
public enum DcmDataStateEnum {
    /**
     * 未标注
     */
    NOT_ANNOTATION_STATE(DcmDataStateCodeConstant.NOT_ANNOTATION_STATE, "notAnnotationDcmState","未标注"),
    /**
     * 标注中
     */
    ANNOTATION_DATA_STATE(DcmDataStateCodeConstant.ANNOTATION_DATA_STATE, "annotationDataState","标注中"),
    /**
     * 自动标注中
     */
    AUTOMATIC_LABELING_STATE(DcmDataStateCodeConstant.AUTOMATIC_LABELING_STATE, "automaticLabelingDcmState","自动标注中"),
    /**
     * 自动标注完成
     */
    AUTO_ANNOTATION_COMPLETE_STATE(DcmDataStateCodeConstant.AUTO_ANNOTATION_COMPLETE_STATE, "autoAnnotationCompleteDcmState","自动标注完成"),
    /**
     * 标注完成
     */
    ANNOTATION_COMPLETE_STATE(DcmDataStateCodeConstant.ANNOTATION_COMPLETE_STATE, "annotationCompleteDcmState","标注完成");

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

    DcmDataStateEnum(Integer code, String stateMachine , String description) {
        this.code = code;
        this.stateMachine = stateMachine;
        this.description = description;
    }

    /**
     * 根据CODE 获取 DESCRIPTION
     *
     * @param code 数据集状态编码
     * @return String
     */
    public static String getStateMachine(Integer code) {
        if (code != null) {
            for (DcmDataStateEnum dcmDataStateEnum : DcmDataStateEnum.values()) {
                if (dcmDataStateEnum.getCode().equals(code)) {
                    return dcmDataStateEnum.getStateMachine();
                }
            }
        }
        return null;
    }

    /**
     * 根据CODE 获取 DataStateEnum
     *
     * @param code 数据集状态编码
     * @return String
     */
    public static DcmDataStateEnum getState(Integer code) {
        if (code != null) {
            for (DcmDataStateEnum dcmDataStateEnum : DcmDataStateEnum.values()) {
                if (dcmDataStateEnum.getCode().equals(code)) {
                    return dcmDataStateEnum;
                }
            }
        }
        return null;
    }

}
