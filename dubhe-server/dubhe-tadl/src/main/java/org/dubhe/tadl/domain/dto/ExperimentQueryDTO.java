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
package org.dubhe.tadl.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;
import org.dubhe.tadl.enums.ExperimentStatusEnum;

import java.io.Serializable;
import java.util.Set;


/**
 * 实验查询
 * @date 2021-03-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel
public class ExperimentQueryDTO extends PageQueryBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("实验id")
    @Query(propName = "id")
    private Long id;

    @Query(type = Query.Type.LIKE, propName = "name")
    @ApiModelProperty("id或名称")
    public String name;


    @ApiModelProperty("状态")
    @Query(type = Query.Type.IN, propName = "status")
    public Set<Integer> status;

    @ApiModelProperty("模型类型")
    @Query(type = Query.Type.EQ, propName = "model_type")
    public String modelType;


    public void setStatus(Integer status){
        this.status= ExperimentStatusEnum.getStatus(status);
    }

}