/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.datasetutil.common.enums;

import lombok.Getter;
import lombok.ToString;

/**
 * @Description 预置数据集类型枚举
 * @Date 2020-11-03
 */
@ToString
@Getter
public enum PresetDatasetEnum {
    /**
     * COCO2017-val
     */
    COCO2017Val("1", "COCO2017-val"),

    /**
     * Caltech-256
     */
    Caltech256("2", "Caltech-256"),

    /**
     * COCO2017-train
     */
    COCO2017Train("3", "COCO2017-train"),

    /**
     * Object-Tracking
     */
    ObjectTracking("4", "Object-Tracking"),

    /**
     * Data-Augment
     */
    DataAugment("5", "Data-Augment"),

    /**
     * IMDB_DATASET
     */
    ImdbDataset("101", "NLP_IMDB"),

    ;

    /**
     * 预置数据集类型
     */
    private  String type;

    /**
     * 操作类型备注
     */
    private  String desc;

    PresetDatasetEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
