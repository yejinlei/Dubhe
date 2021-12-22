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
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @description  训练系统参数枚举类
 * @date 2021-09-22
 */
public enum TrainSystemRunParamEnum {
    /**
     * 训练数据集路径
     */
    data_url("dataUrl", "mount", false),

    /**
     * 验证数据集路径
     */
    val_data_url("valDataUrl", "mount", false),

    /**
     * 断点续训或加载已有模型时，用于接收训练模型路径
     */
    model_load_dir("modelLoadDir", "mount", false),

    /**
     * 分布式训练的节点个数
     */
    num_nodes("numNodes", null, true),

    /**
     * 分布式训练节点ip列表
     */
    node_ips("nodeIps", null, true),

    /**
     * 训练的模型输出路径
     */
    train_model_out("trainModelOut", "out", false),

    /**
     * 训练的文件输出路径
     */
    train_out("trainOut", "out", false),

    /**
     * 训练的可视化日志路径
     */
    train_visualized_log("trainVisualizedLog", "out", false),

    /**
     * 每节点的gpu数量
     */
    gpu_num_per_node("gpuNumPerNode", "normal", false),
    ;


    TrainSystemRunParamEnum(String inputParam, String paramType, boolean distributeTrainOnly) {
        this.inputParam = inputParam;
        this.paramType = paramType;
        this.distributeTrainOnly = distributeTrainOnly;
    }

    @Getter
    @Setter
    private String inputParam;

    @Getter
    @Setter
    private String paramType;

    @Getter
    @Setter
    private boolean distributeTrainOnly;


    /**
     *
     * @param inputParam
     * @return
     */
    public static TrainSystemRunParamEnum to(String inputParam) {
        for (TrainSystemRunParamEnum systemRunParamEnum : TrainSystemRunParamEnum.values()) {
            if (systemRunParamEnum.getInputParam().equals(inputParam)) {
                return systemRunParamEnum;
            }
        }
        return null;
    }

    /**
     * 获取普通训练的参数
     *
     * @return
     */
    public static List<TrainSystemRunParamEnum> getNormalTrainParams() {
        List<TrainSystemRunParamEnum> normalTrainParams = new ArrayList<>();
        for (TrainSystemRunParamEnum systemRunParamEnum : TrainSystemRunParamEnum.values()) {
            if (!systemRunParamEnum.distributeTrainOnly) {
                normalTrainParams.add(systemRunParamEnum);
            }
        }
        return normalTrainParams;
    }

}
