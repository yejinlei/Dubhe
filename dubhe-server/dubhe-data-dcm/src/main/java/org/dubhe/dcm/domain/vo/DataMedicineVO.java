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
package org.dubhe.dcm.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.dcm.domain.entity.DataMedicine;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 医学数据集详情VO
 * @date 2020-11-17
 */
@Data
public class DataMedicineVO  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("医学数据集id")
    private Long id;

    @ApiModelProperty(value = "0:未标注，1:手动标注中，2:自动标注中，3:自动标注完成，4:标注完成")
    private Integer status;

    @ApiModelProperty("检查号")
    private String patientID;

    @ApiModelProperty("研究实例UID")
    private String studyInstanceUID;

    @ApiModelProperty("序列实例UID")
    private String seriesInstanceUID;

    @ApiModelProperty(value = "模式")
    private String modality;

    @ApiModelProperty(value = "部位")
    private String bodyPartExamined;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "标注类型")
    private Integer annotateType;

    /**
     * 医学数据集 转化 医学数据集详情VO 方法
     *
     * @param dataMedicine 医学数据集实体
     * @return  医学数据集详情VO
     */
    public static DataMedicineVO from(DataMedicine dataMedicine){
        DataMedicineVO dataMedicineVO = new DataMedicineVO();
        dataMedicineVO.setId(dataMedicine.getId());
        dataMedicineVO.setStatus(dataMedicine.getStatus());
        dataMedicineVO.setPatientID(dataMedicine.getPatientId());
        dataMedicineVO.setStudyInstanceUID(dataMedicine.getStudyInstanceUid());
        dataMedicineVO.setSeriesInstanceUID(dataMedicine.getSeriesInstanceUid());
        dataMedicineVO.setModality(dataMedicine.getModality());
        dataMedicineVO.setBodyPartExamined(dataMedicine.getBodyPartExamined());
        dataMedicineVO.setCreateTime(dataMedicine.getCreateTime());
        dataMedicineVO.setUpdateTime(dataMedicine.getUpdateTime());
        dataMedicineVO.setName(dataMedicine.getName());
        dataMedicineVO.setRemark(dataMedicine.getRemark());
        dataMedicineVO.setAnnotateType(dataMedicine.getAnnotateType());
        return dataMedicineVO;
    }
}
