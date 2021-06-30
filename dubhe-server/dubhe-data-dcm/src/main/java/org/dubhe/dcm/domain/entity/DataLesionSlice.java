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
package org.dubhe.dcm.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dubhe.biz.db.entity.BaseEntity;

import java.io.Serializable;

/**
 * @description 病灶层面信息
 * @date 2020-12-22
 */
@Data
@RequiredArgsConstructor
@TableName("data_lesion_slice")
@ApiModel(value = "DataLesionSlice 对象", description = "病灶层面信息")
public class DataLesionSlice extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("病灶序号")
    private Integer lesionOrder;

    @ApiModelProperty("病灶层面")
    private String sliceDesc;

    @ApiModelProperty("数据集ID")
    private Long medicineId;

    @ApiModelProperty("标注信息")
    private String drawInfo;

    @ApiModelProperty(value = "资源拥有人id")
    private Long originUserId;

    public DataLesionSlice(Integer lesionOrder, String sliceDesc, Long medicalId, String drawInfo, Long originUserId) {
        this.lesionOrder = lesionOrder;
        this.sliceDesc = sliceDesc;
        this.medicineId = medicalId;
        this.drawInfo = drawInfo;
        this.originUserId = originUserId;
    }
}
