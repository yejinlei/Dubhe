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
package org.dubhe.data.machine.constant;

/**
 * @description 状态机事件常量
 * @date 2020-08-27
 */
public class DataStateMachineConstant {

    private DataStateMachineConstant() {
    }


    /**
     * 数据集状态
     */
    public static final String DATA_STATE_MACHINE = "dataStateMachine";
    /**
     * 采样事件   未采样/未标注/自动标注完成/标注中/目标跟踪完成-->调用采集图片程序-->采样中
     */
    public static final String DATA_SAMPLED_EVENT = "sampledEvent";
    /**
     * 采样事件   未采样-->调用采集图片程序-->采样失败
     */
    public static final String DATA_SAMPLING_FAILURE_EVENT = "samplingFailureEvent";
    /**
     * 采样事件   采样中-->调用采集图片程序-->未标注
     */
    public static final String DATA_SAMPLING_EVENT = "samplingEvent";
    /**
     * 自动标注事件  手动标注中/未标注-->点击自动标注-->自动标注中
     */
    public static final String DATA_AUTO_ANNOTATIONS_EVENT = "autoAnnotationsEvent";
    /**
     * 手动标注保存 自动标注完成-->手动进行标注，点击保存-->标注中
     */
    public static final String DATA_MANUAL_ANNOTATION_SAVE_EVENT = "manualAnnotationSaveEvent";
    /**
     * 标注完成事件 未标注/手动标注中/自动标注完成/目标跟踪完成-->点击完成-->标注完成
     */
    public static final String DATA_FINISH_MANUAL_EVENT = "finishManualEvent";
    /**
     * 自动标注完成事件 自动标注完成-->调用增强算法-->增强中
     */
    public static final String DATA_STRENGTHENING_EVENT = "strengthenEvent";
    /**
     * 标注完成事件  标注完成-->调用增强算法-->增强中
     */
    public static final String DATA_COMPLETE_STRENGTHENING_EVENT = "completeStrengthenEvent";
    /**
     * 增强中状态事件  增强中-->标注完成-->标注完成
     */
    public static final String DATA_STRENGTHENING_COMPLETE_EVENT = "strengtheningCompleteEvent";
    /**
     * 增强中状态事件  增强中-->自动标注完成-->自动标注完成
     */
    public static final String DATA_STRENGTHENING_AUTO_COMPLETE_EVENT = "strengtheningAutoCompleteEvent";
    /**
     * 数据集目标跟踪完成事件
     */
    public static final String DATA_TARGET_COMPLETE_EVENT = "targetCompleteEvent";
    /**
     * 清除标注事件 标注完成/跟踪完成/自动标注完成/-->清除标注-->未标注
     */
    public static final String DATA_DELETE_ANNOTATING_EVENT = "deleteAnnotatingEvent";
    /**
     * 自动标注中事件 自动标注中-->自动标注完成-->自动标注完成
     */
    public static final String DATA_DO_FINISH_AUTO_ANNOTATION_EVENT = "doFinishAutoAnnotationEvent";
    /**
     * 目标跟踪失败事件 目标跟踪中-->>目标跟踪失败事件-->>目标跟踪失败
     */
    public static final String DATA_AUTO_TRACK_FAIL_EVENT = "autoTrackFailEvent";
    /**
     * 目标跟踪事件 目标跟踪失败/自动标注完成/标注完成/目标跟踪完成-->目标跟踪-->目标跟踪中
     */
    public static final String DATA_TRACK_EVENT ="trackEvent";
    /**
     * 删除文件事件
     */
    public static final String DATA_DELETE_FILES_EVENT = "deleteFilesEvent";
    /**
     * 上传文件事件
     */
    public static final String DATA_UPLOAD_FILES_EVENT = "uploadFilesEvent";
    /**
     * 增强完成事件
     */
    public static final String DATA_ENHANCE_FINISH_EVENT = "enhanceFinishEvent";
    /**
     * 表格导入
     */
    public static final String TABLE_IMPORT_EVENT = "tableImportEvent";
    /**
     * 表格导入完成
     */
    public static final String TABLE_IMPORT_FINISH_EVENT = "tableImportFinishEvent";

}