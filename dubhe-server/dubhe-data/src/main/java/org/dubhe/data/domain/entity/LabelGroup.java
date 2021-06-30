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

package org.dubhe.data.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.data.domain.dto.LabelGroupCreateDTO;

import java.io.Serializable;

/**
 * @description 标签组
 * @date 2020-09-22
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_label_group")
@ApiModel(value = "LabelGroup对象", description = "标签组信息")
public class LabelGroup extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "标签组名称")
    private String name;

    @ApiModelProperty(value = "标签组描述")
    private String remark;

    @ApiModelProperty(value = "标签组类型：0: private 私有标签组,  1:public 公开标签组")
    private Integer type;

    @ApiModelProperty(value = "资源拥有人")
    @TableField(value = "origin_user_id",fill = FieldFill.INSERT)
    private Long originUserId;

    @ApiModelProperty(value = "操作类型 1:Json编辑器操作类型 2:自定义操作类型 3:导入操作类型")
    private Integer operateType;

    @ApiModelProperty(value = "标签组类型:0:视觉,1:文本")
    private Integer labelGroupType;

    public LabelGroup(LabelGroupCreateDTO labelGroupCreateDTO) {
        this.name = labelGroupCreateDTO.getName();
        this.remark = labelGroupCreateDTO.getRemark();
        this.type = labelGroupCreateDTO.getType();
        this.originUserId = labelGroupCreateDTO.getOriginUserId();
        this.operateType = labelGroupCreateDTO.getOperateType();
        this.labelGroupType = labelGroupCreateDTO.getLabelGroupType();
    }

}
