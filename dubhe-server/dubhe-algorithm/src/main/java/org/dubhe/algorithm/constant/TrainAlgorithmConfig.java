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

package org.dubhe.algorithm.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description 算法常量
 * @date 2020-06-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "train-algorithm")
public class TrainAlgorithmConfig {

    /**
     *  是否输出训练结果
     */
    private Boolean isTrainModelOut;

    /**
     *  是否输出训练信息
     */
    private Boolean isTrainOut;

    /**
     *  是否输出可视化日志
     */
    private Boolean isVisualizedLog;

    /**
     *  设置默认算法来源(1为我的算法，2为预置算法)
     */
    private Integer algorithmSource;

    /**
     *  设置fork默认值（fork:创建算法来源）
     */
    private Boolean fork;

    /**
     *  设置inference默认值(inference:上传算法是否支持推理)
     */
    private Boolean inference;
}
