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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.data.domain.entity.LabelGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @description 标签组创建DTO
 * @date 2020-10-16
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LabelGroupCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签组name
     */
    @ApiModelProperty(value = "标签组name")
    @NotNull(message = "名称不能为空", groups = Create.class)
    @Size(min = 1, max = 50, message = "name长度范围1~50", groups = Create.class)
    private String name;

    @ApiModelProperty(notes = "备注信息")
    private String remark;

    @ApiModelProperty(notes = "类型 0: private 私有标签组,  1:public 公开标签组")
    @NotNull(message = "类型不能为空", groups = Create.class)
    private Integer type;

    @ApiModelProperty(value = "标签组所含标签信息")
    private String labels;

    @ApiModelProperty(value = "资源拥有人")
    private Long originUserId;

    @ApiModelProperty(value = "操作类型 1:Json编辑器操作类型 2:自定义操作类型 3:导入操作类型")
    @NotNull(message = "操作类型不能为空", groups = Create.class)
    private Integer operateType;

    @ApiModelProperty(value = "标签组类型:0:视觉,1:文本")
    @NotNull(message = "标签组类型不能为空", groups = Create.class)
    private Integer labelGroupType;

    public @interface Create {
    }

    /**
     * 创建标签组
     *
     * @param labelGroupCreateDTO 创建标签组DTO
     * @return LabelGroup 标签组
     */
    public static LabelGroup from(LabelGroupCreateDTO labelGroupCreateDTO) {
        LabelGroup labelGroup = new LabelGroup(labelGroupCreateDTO);
        return labelGroup;
    }

}
