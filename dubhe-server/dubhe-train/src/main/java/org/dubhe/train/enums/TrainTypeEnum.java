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
package org.dubhe.train.enums;

import lombok.Getter;

/**
 * @description 训练类型
 * @date 2020-08-31
 */
@Getter
public enum TrainTypeEnum {

    /**
     * 普通训练
     */
    JOB(0,"普通训练"),
    /**
     * 分布式训练
     */
    DISTRIBUTE_TRAIN(1,"分布式训练"),
    ;


    private Integer code;

    private String name;

    TrainTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 判断是否是分布式训练
     * @param trainType  训练类型
     * @return true 分布式训练，false 普通训练
     */
    public static boolean isDistributeTrain(int trainType){
        return DISTRIBUTE_TRAIN.getCode() == trainType;
    }
}
