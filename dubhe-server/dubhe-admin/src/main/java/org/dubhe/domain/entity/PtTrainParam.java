/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.base.BaseEntity;

/**
 * @descrption 任务参数
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
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
     * 输出路径
     */
    @TableField(value = "out_path")
    private String outPath;

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
     * 日志输出路径
     */
    @TableField(value = "log_path")
    private String logPath;

    /**
     * 规格Id
     */
    @TableField(value = "train_job_specs_id")
    private Integer trainJobSpecsId;

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
}
