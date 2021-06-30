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
package org.dubhe.dcm.machine.constant;

/**
 * @description 文件状态事件常量
 * @date 2020-08-31
 */
public class DcmFileStateMachineConstant {

    private DcmFileStateMachineConstant() {
    }

    /**
     * 文件状态
     */
    public static final String DCM_FILE_STATE_MACHINE = "dcmFileStateMachine";

    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->保存-->标注中
     */
    public static final String ANNOTATION_SAVE_EVENT = "annotationSaveEvent";

    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->完成-->标注完成
     */
    public static final String ANNOTATION_COMPLETE_EVENT = "annotationCompleteEvent";

    /**
     * 文件事件 未标注-->自动标注文件-->自动标注完成
     */
    public static final String AUTO_ANNOTATION_EVENT = "autoAnnotationEvent";
    /**
     * 文件事件 未标注-->自动标注文件-->自动标注完成
     */
    public static final String AUTO_ANNOTATION_SAVE_EVENT = "autoAnnotationSaveEvent";

}