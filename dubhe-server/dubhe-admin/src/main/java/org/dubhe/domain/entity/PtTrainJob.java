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

package org.dubhe.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.base.BaseEntity;

/**
 * @description 训练作业job
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "pt_train_job", autoResultMap = true)
public class PtTrainJob extends BaseEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 训练作业id
     */
    @TableField(value = "train_id")
    private Long trainId;

    /**
     * job版本
     */
    @TableField(value = "train_version")
    private String trainVersion;

    /**
     * job父版本
     */
    @TableField(value = "parent_train_version")
    private String parentTrainVersion;

    /**
     * 任务名称
     */
    @TableField(value = "job_name")
    private String jobName;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 数据来源路径
     */
    @TableField(value = "data_source_path")
    private String dataSourcePath;


    /**
     * 数据来源名称
     */
    @TableField(value = "data_source_name")
    private String dataSourceName;

    /**
     * 训练时长
     */
    @TableField(value = "runtime")
    private String runtime;

    /**
     * 训练输出位置
     */
    @TableField(value = "out_path")
    private String outPath;

    /**
     * 作业日志路径
     */
    @TableField(value = "log_path")
    private String logPath;

    /**
     * 规格名称
     */
    @TableField(value = "train_job_specs_name")
    private String trainJobSpecsName;

    /**
     * 类型(0为CPU，1为GPU)
     */
    @TableField(value = "resources_pool_type")
    private Integer resourcesPoolType;

    /**
     * 规格
     */
    @TableField(value = "resources_pool_specs")
    private String resourcesPoolSpecs;

    /**
     * 节点个数
     */
    @TableField(value = "resources_pool_node")
    private Integer resourcesPoolNode;

    /**
     * 训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知，6为删除，7为创建失败
     */
    @TableField(value = "train_status")
    private Integer trainStatus;

    /**
     * 可视化日志路径
     */
    @TableField(value = "visualized_log_path")
    private String visualizedLogPath;

    /**
     * k8s返回的job名称
     */
    @TableField(value = "k8s_job_name")
    private String k8sJobName;

    /**
     * 训练类型,0:普通训练，1：分布式训练
     */
    @TableField(value = "train_type")
    private Integer trainType;

    /**
     * 验证数据集来源名称
     */
    @TableField(value = "val_data_source_name")
    private String valDataSourceName;

    /**
     * 验证数据集来源路径
     */
    @TableField(value = "val_data_source_path")
    private String valDataSourcePath;

    /**
     * 是否验证数据集
     */
    @TableField(value = "val_type")
    private Integer valType;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;

    /**
     * 模型来源
     */
    @TableField(value = "model_resource")
    private Integer modelResource;

    /**
     * 模型id
     */
    @TableField(value = "model_id")
    private Long modelId;

    /**
     * 模型对应版本id
     */
    @TableField(value = "model_branch_id")
    private Long modelBranchId;

    /**
     * 训练信息(失败信息)
     */
    @TableField(value = "train_msg")
    private String trainMsg;

    @TableField(value = "teacher_model_ids")
    private String teacherModelIds;

    @TableField(value = "student_model_ids")
    private String studentModelIds;

}
