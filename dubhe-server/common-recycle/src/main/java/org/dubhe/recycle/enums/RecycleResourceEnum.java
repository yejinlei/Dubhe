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

package org.dubhe.recycle.enums;

import lombok.Getter;

/**
 * @description 资源回收枚举类
 * @date 2020-10-10
 */
@Getter
public enum RecycleResourceEnum {

    /**
     * 数据集文件回收
     */
    DATASET_RECYCLE_FILE("datasetRecycleFile", "数据集文件回收"),

    /**
     * 医学数据集文件回收
     */
    DATAMEDICINE_RECYCLE_FILE("dataMedicineRecycleFile", "数据集文件回收"),

    /**
     * 数据集版本文件回收
     */
    DATASET_RECYCLE_VERSION_FILE("datasetRecycleVersionFile", "数据集版本文件回收"),

    /**
     * 云端Serving在线服务输入文件回收
     */
    SERVING_RECYCLE_FILE("servingRecycleFile", "云端Serving在线服务文件回收"),
    /**
     * 云端Serving批量服务输入文件回收
     */
    BATCH_SERVING_RECYCLE_FILE("batchServingRecycleFile", "云端Serving批量服务文件回收"),

    /**
     * tadl算法文件回收
     */
    TADL_ALGORITHM_RECYCLE_FILE("tadlAlgorithmRecycleFile", "tadl算法文件回收"),
    /**
     * tadl实验文件回收
     */
    TADL_EXPERIMENT_RECYCLE_FILE("tadlExperimentRecycleFile","tadl实验文件回收"),

    /**
     * 标签组文件回收
     */
    LABEL_GROUP_RECYCLE_FILE("labelGroupRecycleFile", "标签组文件回收"),

    /**
     * 度量文件回收
     */
    MEASURE_RECYCLE_FILE("measureRecycleFile", "度量文件回收"),

    /**
     * 镜像回收
     */
    IMAGE_RECYCLE_FILE("imageRecycleFile", "镜像回收"),

    /**
     * 算法文件回收
     */
    ALGORITHM_RECYCLE_FILE("algorithmRecycleFile", "算法文件回收"),

    /**
     * 模型文件回收
     */
    MODEL_RECYCLE_FILE("modelRecycleFile", "模型文件回收"),
    /**
     * 模型优化文件回收
     */
    MODEL_OPT_RECYCLE_FILE("modelOptRecycleFile","模型优化文件回收"),
    ;

    private String className;

    private String message;

    RecycleResourceEnum(String className, String message) {
        this.className = className;
        this.message = message;
    }


}
