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
package org.dubhe.model.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;

import javax.validation.constraints.NotNull;

/**
 * @description 模型格式
 * @date 2021-04-26
 */
@Data
@Accessors(chain = true)
@TableName("pt_model_type")
public class PtModelType extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    /**
     * 框架类型(OneFlow、TensorFlow、Pytroch、Keras、Caffe、Blade ......)
     */
    @TableField(value = "frame_type")
    private Integer frameType;

    /**
     * 模型格式(SavedModel、Frozen Pb、Keras H5、Caffe Prototxt、ONNX、BladeModel、PMML ......)
     */
    @TableField(value = "model_type")
    private String modelType;
}