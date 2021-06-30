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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.db.entity.BaseEntity;

import java.io.Serializable;

/**
 * @description 医学数据文件
 * @date 2020-11-11
 */
@Data
@TableName("data_medicine_file")
@ApiModel(value = "DataMedicineFile 对象", description = "医学数据文件")
public class DataMedicineFile extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("医学数据集ID")
    private Long medicineId;

    @ApiModelProperty("医学数据集名称")
    private String name;

    @ApiModelProperty("文件地址")
    private String url;

    @ApiModelProperty("资源拥有人")
    private Long originUserId;

    @ApiModelProperty("文件状态")
    private Integer status;

    @ApiModelProperty("instanceNumber")
    private Integer instanceNumber;

    @ApiModelProperty("sopInstanceUid")
    private String sopInstanceUid;

    @ApiModelProperty("imagePositionPatient")
    private Double imagePositionPatient;

    public DataMedicineFile() {

    }
}
