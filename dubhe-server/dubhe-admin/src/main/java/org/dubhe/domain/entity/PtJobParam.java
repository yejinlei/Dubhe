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

import java.sql.Timestamp;

/**
 * @description job运行参数及结果
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "pt_job_param", autoResultMap = true)
public class PtJobParam extends BaseEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 训练作业jobId
     */
    @TableField(value = "train_job_id")
    private Long trainJobId;

    /**
     * 算法来源id
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
     * 运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)
     */
    @TableField(value = "run_params", typeHandler = FastjsonTypeHandler.class)
    private JSONObject runParams;

    /**
     * F1值
     */
    @TableField(value = "param_f1")
    private String paramF1;

    /**
     * 召回率
     */
    @TableField(value = "param_callback")
    private String paramCallback;

    /**
     * 精确率
     */
    @TableField(value = "param_precise")
    private String paramPrecise;

    /**
     * 准确率
     */
    @TableField(value = "param_accuracy")
    private String paramAccuracy;

    /**
     *训练延时启动时间
     */
    @TableField(value = "delay_create_time")
    private Timestamp delayCreateTime;

    /**
     *训练自动停止时间
     */
    @TableField(value = "delay_delete_time")
    private Timestamp delayDeleteTime;

}
