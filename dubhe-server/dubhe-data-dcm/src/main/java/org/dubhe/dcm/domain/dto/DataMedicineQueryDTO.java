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

package org.dubhe.dcm.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dubhe.biz.db.annotation.Query;
import org.dubhe.biz.db.base.PageQueryBase;

/**
 * @description 数据集查询
 * @date 2020-04-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel
public class DataMedicineQueryDTO extends PageQueryBase {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    public String id;

    @ApiModelProperty("名称")
    public String name;

    @ApiModelProperty("序列实例UID")
    @Query(type = Query.Type.EQ, propName = "series_instance_uid")
    public String seriesInstanceUID;

    @ApiModelProperty("检查号")
    @Query(type = Query.Type.LIKE, propName = "patient_id")
    public String patientID;

    @ApiModelProperty("研究实例UID")
    @Query(type = Query.Type.EQ, propName = "study_instance_uid")
    public String studyInstanceUID;

    @ApiModelProperty("状态")
    @Query(type = Query.Type.EQ, propName = "status")
    public Integer status;

    @ApiModelProperty("检查类型(模式)")
    @Query(type = Query.Type.EQ, propName = "modality")
    public String modality;

    @ApiModelProperty("检查身体部位")
    @Query(type = Query.Type.EQ, propName = "body_part_examined")
    public String bodyPartExamined;

    @ApiModelProperty(value = "类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据")
    @Query(type = Query.Type.EQ, propName = "type")
    private Integer type;

    @ApiModelProperty(value = "标注类型: 1001.器官分割 2001.病灶检测之肺结节检测")
    private Integer annotateType;

}
