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


import org.dubhe.dcm.domain.entity.DataMedicine;

/**
 * @description 数据集状态类
 * @date 2020-08-27
 */
public abstract class AbstractDataMedicineState {

    /**
     * 数据集事件 未标注-->标注数据集-->标注中
     *
     * @param medical 医学数据集对象
     */
    public void annotationSaveEvent(DataMedicine medical) {
    }

    /**
     * 数据集事件 未标注-->自动标注-->自动标注中
     *
     * @param medical 医学数据集对象
     */
    public void autoAnnotationSaveEvent(DataMedicine medical) {
    }

    /**
     * 数据集事件 标注中-->自动标注-->自动标注中
     *
     * @param primaryKeyId 业务ID
     */
    public void autoAnnotationEvent(Long primaryKeyId) {
    }

    /**
     * 数据集事件 自动标注中-->自动标注-->自动标注完成
     *
     * @param primaryKeyId 业务ID
     */
    public void autoAnnotationCompleteEvent(Long primaryKeyId) {
    }

    /**
     * 数据集事件 自动标注完成-->标注-->标注完成
     *
     * @param medical 医学数据集对象
     */
    public void annotationCompleteEvent(DataMedicine medical) {
    }




}