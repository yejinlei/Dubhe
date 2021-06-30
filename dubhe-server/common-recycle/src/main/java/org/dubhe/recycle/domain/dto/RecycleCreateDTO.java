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
import org.dubhe.recycle.domain.entity.Recycle;
import org.dubhe.recycle.domain.entity.RecycleDetail;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 创建垃圾回收任务DTO
 * @date 2021-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RecycleCreateDTO extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty(value = "回收模块", required = true)
    @NotNull(message = "回收模块不能为空")
    private Integer recycleModule;

    @ApiModelProperty(value = "回收延迟时间,以天为单位")
    private Integer recycleDelayDate;

    @ApiModelProperty(value = "回收定制化方式")
    private String recycleCustom;

    @ApiModelProperty(value = "回收说明")
    private String recycleNote;

    @ApiModelProperty(value = "回收备注")
    private String remark;

    @ApiModelProperty(value = "还原定制化方式")
    private String restoreCustom;

    @ApiModelProperty(value = "回收任务详情")
    @NotEmpty(message = "回收任务详情不能为空")
    private List<RecycleDetailCreateDTO> detailList;

    /**
     * 添加 RecycleDetailCreateDTO
     * @param detailCreateDTO RecycleDetailCreateDTO
     */
    public void addRecycleDetailCreateDTO(RecycleDetailCreateDTO detailCreateDTO){
        if (detailList == null){
            detailList = new ArrayList<>();
        }
        detailList.add(detailCreateDTO);
    }

    /**
     * 创建 RecycleCreateDTO
     * @param recycle 回收任务
     * @return RecycleCreateDTO
     */
    public static RecycleCreateDTO recycleTaskCreateDTO(Recycle recycle){
        RecycleCreateDTO instance = new RecycleCreateDTO();
        if (recycle != null){
            BeanUtils.copyProperties(recycle, instance);
        }
        return instance;
    }

    /**
     * 添加任务详情
     * @param recycleDetailList 任务详情
     */
    public void setDetailList(List<RecycleDetail> recycleDetailList) {
        detailList = new ArrayList<>();
        for (RecycleDetail recycleDetail:recycleDetailList){
            recycleDetail.setUpdateUserId(this.getUpdateUserId());
            detailList.add(RecycleDetailCreateDTO.recycleDetailCreateDTO(recycleDetail));
        }
    }

}

