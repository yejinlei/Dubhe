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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.base.BaseEntity;

/**
 * @description notebook 模板
 * @date 2020-06-01
 */
@Data
@TableName("notebook_model")
public class NoteBookModel extends BaseEntity {

    @TableField(value = "model_type")
    private String modelType;

    @TableField(value = "cpu_num")
    @ApiModelProperty("cpu数量")
    private Integer cpuNum;

    @TableField(value = "gpu_num")
    @ApiModelProperty("gpu数量")
    private Integer gpuNum;

    @TableField(value = "mem_num")
    @ApiModelProperty("内存大小")
    private Integer memNum;

    @TableField(value = "spec")
    @ApiModelProperty("GPU规格")
    private String spec;

    @TableField(value = "disk_mem_num")
    @ApiModelProperty("硬盘内存大小")
    private Integer diskMemNum;

    @TableField(value = "default_status")
    @ApiModelProperty("默认值状态 0 - 非默认  1- 默认")
    private Integer defaultStatus;

}
