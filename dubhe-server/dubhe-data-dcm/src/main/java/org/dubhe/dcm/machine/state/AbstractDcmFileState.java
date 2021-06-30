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
package org.dubhe.dcm.machine.state;


import java.util.List;

/**
 * @description 文件抽象类
 * @date 2020-08-27
 */
public abstract class AbstractDcmFileState {

    /**
     * 文件事件 未标注-->自动标注文件-->标注中
     *
     * @param fileIds 文件ID
     */
    public void annotationEvent(List<Long> fileIds) {
    }

    /**
     * 文件事件 未标注-->自动标注文件-->自动标注完成
     *
     * @param fileIds 文件ID
     */
    public void autoAnnotationSaveEvent(List<Long> fileIds) {
    }

    /**
     * 文件事件 标注中-->自动标注文件-->自动标注完成
     *
     * @param fileIds 文件ID
     */
    public void autoAnnotationEvent(List<Long> fileIds) {
    }

    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->保存-->标注中
     *
     * @param fileIds 医学数据集文件ID
     */
    public void annotationSaveEvent(List<Long> fileIds){
    }

    /**
     * 文件事件 标注中/自动标注完成/完成/未标注-->完成-->标注完成
     *
     * @param fileIds 医学数据集文件ID
     */
    public void annotationCompleteEvent(List<Long> fileIds){
    }
}
