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

package org.dubhe.train.domain.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @descrption 任务参数
 * @date 2020-04-27
 */
@Data
@Accessors(chain = true)
@TableName(value = "pt_train_param", autoResultMap = true)
public class PtTrainParam extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务参数名称
     */
    @TableField(value = "param_name")
    private String paramName;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 算法id
     */
    @TableField(value = "algorithm_id")
    private Long algorithmId;

    /**
     * 算法用途
     */
    @TableField(value = "algorithm_usage")
    private String algorithmUsage;

    /**
     * 验证数据集算法用途
     */
    @TableField(value = "val_algorithm_usage")
    private String valAlgorithmUsage;

    /**
     * 运行命令
     */
    @TableField(value = "run_command")
    private String runCommand;

    /**
     * 镜像名称
     */
    @TableField(value = "image_name")
    private String imageName;

    /**
     * 数据集来源名称
     */
    @TableField(value = "data_source_name")
    private String dataSourceName;

    /**
     * 数据集来源路径
     */
    @TableField(value = "data_source_path")
    private String dataSourcePath;

    /**
     * 模型输出路径
     */
    @TableField(value = "model_path")
    private String modelPath;

    /**
     * 运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)
     */
    @TableField(value = "run_params", typeHandler = FastjsonTypeHandler.class)
    private JSONObject runParams;

    /**
     * 算法来源(1为我的算法，2为预置算法)
     */
    @TableField(value = "algorithm_source")
    private Integer algorithmSource;

    /**
     * 输出路径
     */
    @TableField(value = "out_path")
    private String outPath;

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
     * 节点个数
     */
    @TableField(value = "resources_pool_node")
    private Integer resourcesPoolNode;

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
     * 模型id
     */
    @TableField(value = "model_id")
    private Long modelId;

    /**
     * 模型版本对应id
     */
    @TableField(value = "model_branch_id")
    private Long modelBranchId;

    /**
     * 模型来源
     */
    @TableField(value = "model_resource")
    private Integer modelResource;

    /**
     * 训练类型,0:普通训练，1：分布式训练
     */
    @TableField(value = "train_type")
    private Integer trainType;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;
    /**
     * 教师模型id集合
     */
    @TableField(value = "teacher_model_ids")
    private String teacherModelIds;

    /**
     * 学生模型id集合
     */
    @TableField(value = "student_model_ids")
    private String studentModelIds;

}

