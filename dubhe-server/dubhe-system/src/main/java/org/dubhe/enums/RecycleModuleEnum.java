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
package org.dubhe.enums;

/**
 * @description 垃圾回收模块枚举
 * @date 2020-09-17
 */
public enum RecycleModuleEnum {

    BIZ_TRAIN(1, "训练任务管理"),
    BIZ_DATASET(2, "数据集管理"),
    BIZ_NOTEBOOK(3, "notebook"),
    BIZ_ALGORITHM(4, "算法管理"),
    BIZ_IMAGE(5, "镜像管理"),
    BIZ_MODEL_OPT(6, "模型优化"),
    BIZ_MODEL(7, "模型管理"),
    BIZ_SERVING(8, "云端Serving"),
    BIZ_DATAMEDICINE(9, "医学影像");

    private Integer value;

    private String desc;

    RecycleModuleEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
