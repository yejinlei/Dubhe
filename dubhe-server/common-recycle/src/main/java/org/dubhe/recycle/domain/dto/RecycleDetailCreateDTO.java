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
package org.dubhe.recycle.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @description 创建垃圾回收任务详情DTO
 * @date 2021-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RecycleDetailCreateDTO extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long recycleId;

    @ApiModelProperty(value = "回收类型(0文件，1数据库表数据)", required = true)
    @NotNull(message = "回收类型不能为空")
    private Integer recycleType;

    @ApiModelProperty(value = "回收条件(回收表数据sql、回收文件绝对路径)", required = true)
    @NotBlank(message = "回收条件不能为空")
    private String recycleCondition;

    @ApiModelProperty(value = "回收说明")
    private String recycleNote;

    @ApiModelProperty(value = "回收备注")
    private String remark;

    @ApiModelProperty(value = "回收状态")
    private Integer recycleStatus;


    /**
     * 创建 RecycleDetailCreateDTO
     * @param recycleDetail 回收任务详情
     * @return RecycleDetailCreateDTO
     */
    public static RecycleDetailCreateDTO recycleDetailCreateDTO(RecycleDetail recycleDetail){
        RecycleDetailCreateDTO instance = new RecycleDetailCreateDTO();
        if (recycleDetail != null){
            BeanUtils.copyProperties(recycleDetail, instance);
        }
        return instance;
    }

}

