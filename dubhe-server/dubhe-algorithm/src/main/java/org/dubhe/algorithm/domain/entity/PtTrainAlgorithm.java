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

package org.dubhe.algorithm.domain.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description 算法
 * @date 2020-04-29
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "pt_train_algorithm", autoResultMap = true)
@Accessors(chain = true)
public class PtTrainAlgorithm extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    /**
     * 算法名称
     */
    @TableField(value = "algorithm_name")
    private String algorithmName;

    /**
     * 算法描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 算法来源(1为我的算法，2为预置算法)
     */
    @TableField(value = "algorithm_source")
    private Integer algorithmSource;

    /**
     * 环境镜像名称
     */
    @TableField(value = "image_name")
    private String imageName;

    /**
     * 代码目录
     */
    @TableField(value = "code_dir")
    private String codeDir;

    /**
     * 运行命令
     */
    @TableField(value = "run_command")
    private String runCommand;

    /**
     * 运行参数
     */
    @TableField(value = "run_params", typeHandler = FastjsonTypeHandler.class)
    private JSONObject runParams;

    /**
     * 算法用途
     */
    @TableField(value = "algorithm_usage")
    private String algorithmUsage;

    /**
     * 算法精度
     */
    @TableField(value = "accuracy")
    private String accuracy;

    /**
     * P4推理速度（ms）
     */
    @TableField(value = "p4_inference_speed")
    private Integer p4InferenceSpeed;

    /**
     * 算法是否支持推理（1可推理，0不可推理）
     */
    @TableField(value = "inference")
    private Boolean inference;

    /**
     * 训练结果输出（1是，0否）
     */
    @TableField(value = "is_train_model_out")
    private Boolean isTrainModelOut;

    /**
     * 训练输出（1是，0否）
     */
    @TableField(value = "is_train_out")
    private Boolean isTrainOut;

    /**
     * 可视化日志（1是，0否）
     */
    @TableField(value = "is_visualized_log")
    private Boolean isVisualizedLog;

    /**
     * 算法状态
     */
    @TableField(value = "algorithm_status")
    private Integer algorithmStatus;

    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;
}
