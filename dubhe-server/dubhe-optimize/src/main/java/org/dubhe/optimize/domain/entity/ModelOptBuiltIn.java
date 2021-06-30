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

package org.dubhe.optimize.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description 内置模型
 * @date 2020-05-22
 */
@Data
@TableName("model_opt_built_in")
public class ModelOptBuiltIn extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 算法类型：0-剪枝，1-蒸馏，2-量化
     */
    @TableField("type")
    private Integer type;
    /**
     * 算法名称
     */
    @TableField("algorithm")
    private String algorithm;
    /**
     * 算法路径
     */
    @TableField("algorithm_path")
    private String algorithmPath;
    /**
     * 数据集名称
     */
    @TableField("dataset")
    private String dataset;
    /**
     * 数据集路径
     */
    @TableField("dataset_path")
    private String datasetPath;
    /**
     * 模型名称
     */
    @TableField("model")
    private String model;
    /**
     * 模型路径
     */
    @TableField(value = "model_address")
    private String modelAddress;
}
