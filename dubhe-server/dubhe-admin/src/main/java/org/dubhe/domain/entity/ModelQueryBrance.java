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
/**
 * @description model 模型版本
 * @date 2020-10-09
 */
@Data
@TableName("pt_model_branch")
public class ModelQueryBrance {
    @TableField(value = "model_resource")
    @ApiModelProperty(value = "模型类型")
    private Integer modelResource;
    @TableField(value = "name")
    @ApiModelProperty(value = "模型名称")
    private String  name;
    @TableField(value = "version")
    @ApiModelProperty(value = "模型版本")
    private String version;
    @TableField(value = "id")
    @ApiModelProperty(value = "模型id")
    private Integer id;
    @TableField(value = "url")
    @ApiModelProperty(value = "模型路径")
    private String url;

}
