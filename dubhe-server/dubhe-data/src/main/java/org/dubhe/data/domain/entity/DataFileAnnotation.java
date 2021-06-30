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

package org.dubhe.data.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.dubhe.biz.db.entity.BaseEntity;

import java.io.Serializable;

/**
 * @description 数据集文件标注
 * @date 2021-01-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@TableName("data_file_annotation")
@ApiModel(value = "DataFileAnnotation对象", description = "")
public class DataFileAnnotation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty("数据集ID")
    private Long datasetId;

    @ApiModelProperty("标签ID")
    private Long labelId;

    @ApiModelProperty("版本文件ID")
    private Long versionFileId;

    @ApiModelProperty("预测值")
    private Double prediction;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("标注状态")
    private Integer status;

    @ApiModelProperty("版本标注")
    private Integer invariable;

    public DataFileAnnotation(Long datasetId, Long labelId, Long versionFileId, Double prediction, String fileName) {
        this.datasetId = datasetId;
        this.labelId = labelId;
        this.versionFileId = versionFileId;
        this.prediction = prediction;
        this.fileName = fileName;
    }

}
