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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import org.dubhe.biz.db.entity.BaseEntity;

/**
 * @description 模型优化任务
 * @date 2020-05-22
 */
@Data
@TableName(value = "model_opt_task", autoResultMap = true)
public class ModelOptTask extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 任务名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 任务描述
     */
    @TableField(value = "description")
    private String description;
    /**
     * 是否内置
     */
    @TableField(value = "is_built_in")
    private Boolean isBuiltIn;
    /**
     * 模型名称
     */
    @TableField(value = "model_name")
    private String modelName;
    /**
     * 模型id
     */
    @TableField(value = "model_id")
    private Long modelId;
    /**
     * 模型id
     */
    @TableField(value = "model_branch_id")
    private Long modelBranchId;
    /**
     * 模型路径
     */
    @TableField(value = "model_address")
    private String modelAddress;
    /**
     * 算法选择类型
     */
    @TableField(value = "algorithm_type")
    private Integer algorithmType;
    /**
     * 优化算法id
     */
    @TableField(value = "algorithm_id")
    private Long algorithmId;
    /**
     * 优化算法
     */
    @TableField(value = "algorithm_name")
    private String algorithmName;
    /**
     * 算法路径
     */
    @TableField(value = "algorithm_path")
    private String algorithmPath;
    /**
     * 数据集id
     */
    @TableField(value = "dataset_id")
    private Long datasetId;
    /**
     * 数据集名称
     */
    @TableField(value = "dataset_name")
    private String datasetName;
    /**
     * 数据集路径
     */
    @TableField(value = "dataset_path")
    private String datasetPath;
    /**
     * 运行命令
     */
    @TableField(value = "command")
    private String command;
    /**
     * 运行参数
     */
    @TableField(value = "params", typeHandler = FastjsonTypeHandler.class)
    private JSONObject params;
    /**
     * 资源拥有者ID
     */
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;

    public void copy(ModelOptTask source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
