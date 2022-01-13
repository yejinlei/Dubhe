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
package org.dubhe.tadl.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description trial运行参数
 * @date 2021-03-16
 */
@Data
public class TrialRunParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 实验id
     */
    private Long experimentId;
    /**
     * 实验名称
     */
    private String name;
    /**
     * trial id
     */
    private Long trialId;
    /**
     * 阶段id
     */
    private Long stageId;
    /**
     * 当前trial所在阶段数据集路径
     */
    private String datasetPath;
    /**
     * 算法拷贝路径
     */
    private String algorithmPath;
    /**
     * 日志路径
     */
    private String logPath;
    /**
     * 当前trial输出路径
     */
    private String trialPath;
    /**
     * 当前实验输出路径
     */
    private String experimentPath;
    /**
     * GPU数量,1代表使用一张显卡
     */
    private Integer gpuNum;
    /**
     * 节点类型(0为CPU，1为GPU)
     */
    private Integer resourcesPoolType;
    /**
     * 内存数量，单位Mi
     */
    private Integer memNum;
    /**
     * cpu用量 单位:m 1个核心=1000m
     */
    private Integer cpuNum;
    /**
     * 运行参数
     */
    private String command;
    /**
     * Redis Stream 生成的ID
     */
    private String redisStreamRecodeId;
    /**
     * k8s 资源命名空间名称
     */
    private String namespace;

}
