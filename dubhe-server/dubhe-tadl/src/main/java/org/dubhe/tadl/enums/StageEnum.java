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
package org.dubhe.tadl.enums;

import lombok.Getter;

@Getter
public enum StageEnum {

    /**
     * base
     */
    BASE(0, "base"),
    /**
     * train
     */
    TRAIN(1, "train"),
    /**
     * select
     */
    SELECT(2, "select"),
    /**
     * retrain
     */
    RETRAIN(3, "retrain");

    StageEnum(Integer stageOrder, String name) {
        this.stageOrder = stageOrder;
        this.name = name;
    }

    private Integer stageOrder;
    private String name;
    /**
     * 阶段 用户web端接口调用时参数校验
     *
     * @param stageOrder 算法阶段Integer值
     * @return      参数校验结果
     */
    public static boolean isValid(Integer stageOrder) {
        for (StageEnum stageEnum : StageEnum.values()) {
            if (stageEnum.stageOrder.equals(stageOrder)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取算法阶段排序
     *
     * @param name 阶段名称
     * @return 阶段排序
     */
    public static Integer getStageOrder(String name) {
        for (StageEnum stageEnum : StageEnum.values()) {
            if (stageEnum.name.equals(name)) {
                return stageEnum.stageOrder;
            }
        }
        return null;
    }

    /**
     * 获取算法阶段名称
     *
     * @param stageOrder 阶段排序
     * @return 阶段名称
     */
    public static String getStageName(Integer stageOrder) {
        for (StageEnum stageEnum : StageEnum.values()) {
            if (stageEnum.stageOrder.equals(stageOrder)) {
                return stageEnum.name;
            }
        }
        return null;
    }


    /**
     * 获取算法阶段
     *
     * @param stageOrder 阶段排序
     * @return 阶段
     */
    public static StageEnum getStage(Integer stageOrder) {
        for (StageEnum stageEnum : StageEnum.values()) {
            if (stageEnum.stageOrder.equals(stageOrder)) {
                return stageEnum;
            }
        }
        return null;
    }
}
