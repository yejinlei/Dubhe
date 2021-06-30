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

package org.dubhe.optimize.domain.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.dubhe.biz.base.vo.BaseVO;

import java.sql.Timestamp;
import java.util.List;

/**
 * @description 模型优化任务实例
 * @date 2020-05-22
 */
@Data
public class ModelOptTaskInstanceQueryVO extends BaseVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 是否内置
     */
    private Boolean isBuiltIn;

    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型路径
     */
    private String modelAddress;
    /**
     * 算法选择类型
     */
    private Integer algorithmType;
    /**
     * 优化算法
     */
    private String algorithmName;
    /**
     * 优化算法id
     */
    private Long algorithmId;
    /**
     * 优化算法路径
     */
    private String algorithmPath;
    /**
     * 提交时间
     */
    private Timestamp startTime;
    /**
     * 完成时间
     */
    private Timestamp endTime;
    /**
     * 日志路径
     */
    private String logPath;
    /**
     * 任务状态
     */
    private String status;
    /**
     * 状态对应的详情信息
     */
    private String statusDetail;
    /**
     * 数据集id
     */
    private Long datasetId;
    /**
     * 数据集名称
     */
    private String datasetName;
    /**
     * 数据集路径
     */
    private String datasetPath;
    /**
     * 运行命令
     */
    private String command;
    /**
     * 运行参数
     */
    private JSONObject params;
    /**
     * 容器名称
     */
    private String podName;
    /**
     * 模型输出路径
     */
    private String outputModelDir;
    /**
     * 优化结果
     */
    private List<ModelOptResultQueryVO> optResult;
    /**
     * 命名空间
     */
    String namespace;

}
