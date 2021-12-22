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
package org.dubhe.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description GPU资源实体类
 * @date 2021-08-20
 */
@Data
@Accessors(chain = true)
@TableName("gpu_resource")
public class GpuResource extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    /**
     * GPU类型(例如：NVIDIA)
     */
    @TableField(value = "gpu_type")
    private String gpuType;

    /**
     * GPU型号(例如：v100)
     */
    @TableField(value = "gpu_model")
    private String gpuModel;

    /**
     * k8s GPU资源标签key值(例如：nvidia.com/gpu)
     */
    @TableField(value = "k8s_label_key")
    private String k8sLabelKey;

}