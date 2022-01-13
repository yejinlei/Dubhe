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

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;

@Builder
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TrialDTO  extends PageQueryBase {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id或名称")
    public String searchParam;

    @ApiModelProperty("实验id")
    @Query(type = Query.Type.EQ, propName = "experiment_id")
    private Long experimentId;

    @ApiModelProperty("阶段排序")
    private Integer stageOrder;

    @ApiModelProperty("trial名称")
    @Query(type = Query.Type.EQ, propName = "name")
    private String name;

    @ApiModelProperty("状态")
    @Query(type = Query.Type.EQ, propName = "status")
    private Integer status;


}