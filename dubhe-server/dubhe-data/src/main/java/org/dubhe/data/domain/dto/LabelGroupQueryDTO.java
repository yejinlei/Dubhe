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
package org.dubhe.data.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 标签组列表DTO
 * @date 2020-12-11
 */
@Data
public class LabelGroupQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签组类型
     */
    @ApiModelProperty(value = "标签组类型(0: private 私有标签组,  1:public 公开标签组)")
    @NotNull(message = "标签组类型不能为空")
    private Integer type;

    /**
     * 数据集数据类型
     */
    @ApiModelProperty(value = "数据类型:0图片，1视频，2文本")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    /**
     * 数据集标注类型
     */
    @ApiModelProperty(value = "标注类型：1目标检测,2分类,5目标跟踪,7语义分割")
    @NotNull(message = "标注类型不能为空")
    private Integer annotateType;
}
