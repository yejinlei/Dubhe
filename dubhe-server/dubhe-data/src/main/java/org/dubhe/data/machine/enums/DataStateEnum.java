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

import org.dubhe.data.machine.constant.DataStateCodeConstant;

/**
 * @description 数据集状态类
 * @date 2020-08-28
 */
public enum DataStateEnum {
    /**
     * 未标注
     */
    NOT_ANNOTATION_STATE(DataStateCodeConstant.NOT_ANNOTATION_STATE, "notAnnotationState","未标注"),
    /**
     * 手动标注中
     */
    MANUAL_ANNOTATION_STATE(DataStateCodeConstant.MANUAL_ANNOTATION_STATE, "manualAnnotationState","手动标注中"),
    /**
     * 自动标注中
     */
    AUTOMATIC_LABELING_STATE(DataStateCodeConstant.AUTOMATIC_LABELING_STATE, "automaticLabelingState","自动标注中"),
    /**
     * 自动标注完成
     */
    AUTO_TAG_COMPLETE_STATE(DataStateCodeConstant.AUTO_TAG_COMPLETE_STATE, "autoTagCompleteState","自动标注完成"),
    /**
     * 标注完成
     */
    ANNOTATION_COMPLETE_STATE(DataStateCodeConstant.ANNOTATION_COMPLETE_STATE, "annotationCompleteState","标注完成"),
    /**
     * 未采样
     */
    NOT_SAMPLED_STATE(DataStateCodeConstant.NOT_SAMPLED_STATE, "notSampledState","未采样"),
    /**
     * 采样中
     */
    SAMPLING_STATE(DataStateCodeConstant.SAMPLING_STATE, "samplingState","采样中"),
    /**
     * 采样失败
     */
    SAMPLED_FAILURE_STATE(DataStateCodeConstant.SAMPLED_FAILURE_STATE, "sampledFailureState","采样失败"),
    /**
     * 目标跟踪完成
     */
    TARGET_COMPLETE_STATE(DataStateCodeConstant.TARGET_COMPLETE_STATE, "targetCompleteState","目标跟踪完成"),
    /**
     * 目标跟踪中
     */
    TARGET_FOLLOW_STATE(DataStateCodeConstant.TARGET_FOLLOW_STATE, "targetFollowState","目标跟踪中"),
    /**
     * 目标跟踪失败
     */
    TARGET_FAILURE_STATE(DataStateCodeConstant.TARGET_FAILURE_STATE, "targetFailureState","目标跟踪失败"),
    /**
     * 增强中
     */
    STRENGTHENING_STATE(DataStateCodeConstant.STRENGTHENING_STATE, "strengtheningState","增强中"),
    /**
     * 导入中
     */
    IN_THE_IMPORT_STATE(DataStateCodeConstant.IN_THE_IMPORT_STATE, "isTheImportState", "导入中");

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

    DataStateEnum(Integer code, String stateMachine , String description) {
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
            for (DataStateEnum dataStateEnum : DataStateEnum.values()) {
                if (dataStateEnum.getCode().equals(code)) {
                    return dataStateEnum.getStateMachine();
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
    public static DataStateEnum getState(Integer code) {
        if (code != null) {
            for (DataStateEnum dataStateEnum : DataStateEnum.values()) {
                if (dataStateEnum.getCode().equals(code)) {
                    return dataStateEnum;
                }
            }
        }
        return null;
    }

}
