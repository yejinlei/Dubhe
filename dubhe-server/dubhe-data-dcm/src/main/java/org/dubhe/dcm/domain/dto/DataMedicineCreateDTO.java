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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.dcm.domain.entity.DataMedicine;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @description 医学数据集创建DTO
 * @date 2020-11-16
 */
@Data
public class DataMedicineCreateDTO  implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("检查号")
    @NotBlank(message = "检查号不能为空", groups = Create.class)
    private String patientID;

    @ApiModelProperty("研究实例UID")
    @NotBlank(message = "研究实例UID不能为空", groups = Create.class)
    private String studyInstanceUID;

    @ApiModelProperty("序列实例UID")
    @NotBlank(message = "序列实例UID不能为空", groups = Create.class)
    private String seriesInstanceUID;

    @ApiModelProperty(value = "模式")
    @NotBlank(message = "模式不能为空", groups = Create.class)
    private String modality;

    @ApiModelProperty(value = "部位")
    private String bodyPartExamined;

    @ApiModelProperty(value = "名称")
    @NotBlank(message = "名称不能为空", groups = Create.class)
    private String name;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "标注类型")
    private Integer annotateType;

    public @interface Create {
    }

    /**
     * 实体装换方法
     *
     * @param dataMedicineCreateDTO 医学数据集创建DTO
     * @param userId                用户ID
     * @return  医学数据集实体
     */
    public static DataMedicine from(DataMedicineCreateDTO dataMedicineCreateDTO,Long userId) {
        DataMedicine dataMedicine = new DataMedicine(dataMedicineCreateDTO,userId);
        return dataMedicine;
    }

}
