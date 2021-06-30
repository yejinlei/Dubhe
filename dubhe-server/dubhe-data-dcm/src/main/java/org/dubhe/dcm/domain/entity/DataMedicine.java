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
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.data.machine.constant.DataStateCodeConstant;
import org.dubhe.dcm.domain.dto.DataMedicineCreateDTO;

import java.io.Serializable;

/**
 * @description 医学数据集
 * @date 2020-11-11
 */
@Data
@TableName("data_medicine")
@ApiModel(value = "DataMedicine 对象", description = "医学数据集")
public class DataMedicine extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    private Long id;

    @ApiModelProperty("检查号")
    private String patientId;

    @ApiModelProperty("研究实例UID")
    private String studyInstanceUid;

    @ApiModelProperty("序列实例UID")
    private String seriesInstanceUid;

    @ApiModelProperty(value = "0:未标注，1:手动标注中，2:自动标注中，3:自动标注完成，4:标注完成")
    private Integer status;

    @ApiModelProperty(value = "模式")
    private String modality;

    @ApiModelProperty(value = "部位")
    private String bodyPartExamined;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty("资源拥有人")
    private Long originUserId;

    @ApiModelProperty(value = "类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据")
    private Integer type;

    @ApiModelProperty(value = "标注类型: 1001.器官分割 2001.病灶检测之肺结节检测")
    private Integer annotateType;

    public DataMedicine() {
    }

    /**
     * 转换DataMedicine对象
     *
     * @param dataMedicineCreateDTO 创建数据集所需参数
     * @param userId 用户ID
     */
    public DataMedicine(DataMedicineCreateDTO dataMedicineCreateDTO, Long userId) {
        this.patientId =  dataMedicineCreateDTO.getPatientID();
        this.studyInstanceUid = dataMedicineCreateDTO.getStudyInstanceUID();
        this.seriesInstanceUid = dataMedicineCreateDTO.getSeriesInstanceUID();
        this.modality = dataMedicineCreateDTO.getModality();
        this.bodyPartExamined = dataMedicineCreateDTO.getBodyPartExamined();
        this.status = DataStateCodeConstant.NOT_ANNOTATION_STATE;
        this.name = dataMedicineCreateDTO.getName();
        this.remark = dataMedicineCreateDTO.getRemark();
        this.originUserId = userId;
        this.annotateType = dataMedicineCreateDTO.getAnnotateType();
    }
}
